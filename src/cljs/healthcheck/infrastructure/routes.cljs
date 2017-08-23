(ns healthcheck.routes
  (:require [accountant.core :as accountant]))


(defn navigate
  ([env app] (accountant/navigate! (str "/health/" env "/" app)))
  ([env] (accountant/navigate! (str "/health/" env)))
  ([] (accountant/navigate! "/")))
