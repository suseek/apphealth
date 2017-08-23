(ns healthcheck.nav-bar
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch
                                   subscribe]]
            [healthcheck.common :refer [status-icon]]
            [healthcheck.routes :refer [navigate]]))

(defn mark-navbar-env [is-ok]
  (if is-ok "nav-bar-type-a-working" "nav-bar-type-a-error"))

(defn type-ahead-row [env]
  (let [env-statuses (subscribe [:env-statuses])]
    (fn [env]
      [:li
       [:div.header {:class (mark-navbar-env ((key env) @env-statuses))} (key env)]
       (into [:div.content]
             (->> (take 2 (val env))
                  (map (fn [[app-id app-details]]
                         [:div.app {:on-mouse-down #(do
                                                      (dispatch [:search-on false])
                                                      (dispatch [:type-ahead ""])
                                                      (navigate (name (key env)) (name app-id)))}
                          [status-icon (:status app-details)]
                          (:name app-details)]))))])))

(defn nav-bar []
  (let [search-on? (subscribe [:search-on])
        type-ahead-text (subscribe [:type-ahead])
        found-apps (subscribe [:search-result])
        stop #(do
                (dispatch [:search-on false])
                (dispatch [:type-ahead ""]))]
    (fn []
      [:div.nav-bar
       [:i.material-icons.logo-icon "healing"]
       [:span.logo {:on-mouse-down #(navigate)}
        [:text "App"] [:text.logo-red "Health"]]
       (if @search-on?
         [:div.search-wrapper
          [:div.search-on
           [:i.material-icons "search"]
           [:input {:value       @type-ahead-text
                    :on-blur     stop
                    :placeholder "find your app..."
                    :on-change   #(dispatch [:type-ahead (-> % .-target .-value)])
                    :on-key-down #(case (.-which %)
                                    27 (stop)
                                    nil)
                    :auto-focus  true}]]

          (if (not-empty @type-ahead-text)
            (into [:ul.type-ahead]
                  (->> @found-apps
                       (map (fn [env] [type-ahead-row env])))))]
         [:div.search-wrapper [:div.search-off {:on-click #(dispatch [:search-on true])}
                               [:i.material-icons "search"] [:input {:placeholder "find your app..." :value @type-ahead-text}]]])])))
