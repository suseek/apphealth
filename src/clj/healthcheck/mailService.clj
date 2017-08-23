(ns healthcheck.mailService
  (:require [healthcheck.config :as config]
            [clojure.core.cache :as cache]
            [environ.core :refer [env]]
            [postal.core :refer [send-message]]
            [taoensso.timbre :as t]
            [clojure.string :as s]))

(def error-apps (atom (cache/ttl-cache-factory {} :ttl (:1-day config/times))))
(def ?send-mails (atom true))

(defn send-email [subject, message]
  (if (or
        (env :dev)
        (not @?send-mails))
    (t/info "(just a mail alert-log) with: "
            "subject: " subject
            ", messsage: " message)
    (send-message {:host (:host config/mail-alert)}
                  {:from (:from config/mail-alert)
                   :to (:send-to config/mail-alert)
                   :subject subject
                   :body message})))

(defn send-failure-mail! [env [id {:keys [name url]} app-info] ttl]
  (let [app-cache-id (str env "-" id)]
    (if (or (not (cache/has? @error-apps app-cache-id)) (< (:times (get @error-apps app-cache-id)) ttl))
      (do
        (if-not (cache/has? @error-apps app-cache-id)
          (swap! error-apps #(cache/miss % app-cache-id {:times 1}))
          (swap! error-apps #(cache/miss % app-cache-id {:times (inc (:times (get @error-apps app-cache-id)))})))
        (t/info (str "App " name " is down, but we won't send e-mail just yet!")))
      (if (= (:times (get @error-apps app-cache-id)) 3)
        (do
          (send-email (str " [AppHealth] " name " on " env " is down!")
                      (str "App " name " on " env
                           " which should be under " url
                           " is not working right now."))
          (t/info (str "Mail sent - " name " app is down for more than 3 rolls!"))
          (swap! error-apps #(cache/miss % app-cache-id {:times (+ ttl 1)})))))))

(defn send-working-mail! [env [id {:keys [name url]} app-info] ttl]
  (let [app-cache-id (str env "-" id)]
    (if (cache/has? @error-apps app-cache-id)
      (do
        (if (<= (:times (get @error-apps app-cache-id)) ttl)
          (t/debug "App" name "is working - cleaning cache instead of sending mail!")
          (do (send-email (str " [AppHealth] " name " on " env " is up and running!")
                          (str "App " name " on " env
                               " which is under " url
                               " is working again!."))
              (t/debug "Mail for app " name "on env" env " has just been sent. App is working again!")))
        (swap! error-apps #(cache/evict % app-cache-id))))))

(defn send-mail [[env apps] failure?]
  (doseq [app apps]
    (let [ttl (get (val app) :ttl 3)]
      (if failure?
        (send-failure-mail! env app ttl)
        (send-working-mail! env app ttl)))))

(defn send-mails [?should-send-mails]
  (reset! ?send-mails (Boolean/valueOf ?should-send-mails)))