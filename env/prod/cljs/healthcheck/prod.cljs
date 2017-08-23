(ns healthcheck.prod
  (:require [healthcheck.infrastructure.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
