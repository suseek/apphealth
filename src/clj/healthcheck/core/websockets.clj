(ns healthcheck.core.websockets
  (:require [compojure.core :refer [GET defroutes]]
            [org.httpkit.server :refer [send! with-channel on-close on-receive]]
            [taoensso.timbre :as t]
            [clojure.data.json :as json]))

(defonce channels (atom #{}))
(defonce last-message (atom "[]"))

(defn notify-clients [msg]
  (reset! last-message msg)
  (doseq [channel @channels]
    (send! channel msg)))

(defn notify-with-message [status]
  (notify-clients (json/write-str ["^ " "~:message" status])))

(defn send-last-message [channel]
  (send! channel @last-message))

(defn connect! [channel]
  (t/info "channel open")
  (swap! channels conj channel)
  (send-last-message channel))

(defn disconnect! [channel status]
  (t/info "channel closed:" status)
  (swap! channels #(remove #{channel} %)))

(defn ws-handler [request]
  (with-channel request channel
                (connect! channel)
                (on-close channel (partial disconnect! channel))
                (on-receive channel #(notify-clients %))))

