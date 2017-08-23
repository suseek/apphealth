(ns healthcheck.core.server
  (:require [healthcheck.core.handler :refer [app]]
            [environ.core :refer [env]]
            [taoensso.timbre :as t])
  (:gen-class)
  (:use org.httpkit.server))

 (defn -main [& args]
   (let [port (Integer/parseInt (or (env :port) "3000"))]
     (t/info (str "Starting app in " (keys env) "mode"))
     (run-server app {:port port :join? false})))
