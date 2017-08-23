(ns healthcheck.checkService
  (:require [healthcheck.config :as config]
            [org.httpkit.client :as http]
            [clojure.data :refer [diff]]
            [healthcheck.core.websockets :refer [notify-with-message]]
            [healthcheck.mailService :refer [send-mail]]
            [clojure.data.json :as json]
            [healthcheck.utils :refer [json?]]))

(def current-status (atom {}))
(add-watch current-status :watcher
           (fn [key atom old-state new-state]
             (if-not (= old-state new-state) (notify-with-message new-state))))

(defn json-body->data [response]
  (if (json? (:body response))
    (json/read-str (:body response) :key-fn keyword)))

(defn is-up? [app-info]
  (and (= 200 (:code app-info))
       (not (.contains (->> (tree-seq #(or (map? %) (vector? %)) identity app-info)
                  (filter #(if (and (map? %) (:status %)) true  false))
                  (map :status)
                  set
                  vec) "DOWN"))))

(defn free-memory->mem-response [mem-free]
  (if mem-free
  {:status (if (< mem-free config/min-free-memory) "DOWN" "UP")
   :available-memory mem-free}))

(defn source->app-health [source]
  (let [app-env (json-body->data @(http/get (str (:url source) config/api-docs) {:timeout (:5s config/times)}))
        metrics-data (if (:metrics source)
                       (json-body->data @(http/get (str (:url source) (:metrics source)) {:timeout (:5s config/times)})))
        app-info-url (if (:health-page source)
                       (str (:url source) (:health-page source))
                       (str (:url source)))
        info (http/get app-info-url {:timeout (:5s config/times)})]
    {:info (json-body->data @info)
     :code (:status @info)
     :app-version (:apiVersion app-env)
     :mem-free (free-memory->mem-response (:mem.free metrics-data))}))

(defn check-sources-by-env [env]
  (into {} (->> (get config/sources env)
                (map (fn [source]
                       [source (source->app-health (val source))]))
                (map (fn [[[key val] app-info]]
                       [key (into val (into app-info (cond
                                                       (is-up? app-info) [[:status "UP"]]
                                                       :else [[:status "DOWN"]])))])))))
(defn health-check [sources]
  (into {} (->> (keys sources)
                (map (fn [env] {(keyword env) (check-sources-by-env (keyword env))})))))

(defn find-all [state failure?]
  (into {} (->> state
                (into {} (map (fn [[env apps]]
                                [env (into {} (->> apps
                                                   (filter (fn [[app-id val]]
                                                             ((if failure? = not=) "DOWN" (:status val))))
                                                   (map (fn [[app-id val]] [app-id val]))))]))))))

(defn save-state! [state]
  (reset! current-status state))

(defn check []
  (let [state (health-check config/sources)
        failed-env-apps (find-all state true)
        working-env-apps (find-all state false)]
    (doseq [failed-env failed-env-apps]
      (send-mail failed-env true))
    (doseq [working-env working-env-apps]
      (send-mail working-env false))
    (save-state! state)))

(defn present-current-status
  ([] @current-status)
  ([env] (@current-status (keyword env))))
