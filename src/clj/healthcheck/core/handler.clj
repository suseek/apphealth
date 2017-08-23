(ns healthcheck.core.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [healthcheck.middleware :refer [wrap-middleware]]
            [environ.core :refer [env]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults api-defaults]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.response :refer [response header redirect]]
            [healthcheck.checkService :as checkService]
            [healthcheck.mailService :as mailService]
            [healthcheck.config :as config]
            [org.httpkit.client :as http]
            [ring.util.codec :as url]
            [clojure.data.json :as json]
            [healthcheck.core.websockets :refer [ws-handler notify-clients]]
            [compojure.handler :as handler]))

(def checking? (atom true))

(def mount-target
  [:div#app
   [:div.spinner]])

(def loading-page
  (html5
   [:head
    [:title "AppHealth"]
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
    [:link {:rel "shortcut icon" :type "image/png" :href "/health.png"}]
     (include-css "/css/main.css")
     (include-css "/css/animations.css")
     (include-css "https://fonts.googleapis.com/icon?family=Material+Icons")

    [:body
     mount-target
     (include-js "/js/app.js")]]))

(defn json-response [body]
  (-> (response body)
      (header "Content-Type" "application/json")))

(defn start-checking []
  (if-not @checking?
    (do (reset! checking? true)
        (future (loop []
                  (Thread/sleep (:10s config/times))
                  (checkService/check)
                  (when @checking? (recur))))))
  {:checking "true"})

(defn stop-checking []
  (if @checking? (reset! checking? false))
  {:checking "false"})

(defroutes routes
  (GET "/" [] loading-page)
  (GET "/health/:env" [env] loading-page)
  (GET "/health/:env/:id" [env id] loading-page)
  (GET "/api/check/all" [] (json-response (checkService/present-current-status)))
  (GET "/api/check/start" [] (json-response (start-checking)))
  (GET "/api/check/stop" [] (json-response (stop-checking)))
  (GET "/api/check/:env" [env] (json-response (checkService/present-current-status env)))
  (GET "/api/send-mails/:val" [val] (json-response (str (mailService/send-mails val))))
  (GET "/ws" request (ws-handler request))

  (resources "/")
  (not-found "Not Found"))

(def app
  (let []
    (if (env :dev)
      (-> (handler/site routes)
          wrap-reload
          wrap-keyword-params
          wrap-json-params
          wrap-json-response)
      (-> (handler/site routes)
          wrap-keyword-params
          wrap-json-params
          wrap-json-response))))
