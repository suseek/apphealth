(ns healthcheck.main-page
  (:require [re-frame.core :refer [subscribe]]
            [healthcheck.common :refer [mark-env list-environments]]
            [healthcheck.routes :refer [navigate]]))

(defn main-page []
  (let [env-health (subscribe [:env-statuses])]
    (fn []
      [:div.container
       [:div.main-page.animate-fade-in
        [:h1 "The best place to check your app's health."]
        (if (empty? (filter #(= false (val %)) @env-health))
          [:p.lead "Looks like all our apps are up now!"]
          [:p.lead "Oops! Not all our apps are up right now!"])
        [list-environments "env-list"]]])))
