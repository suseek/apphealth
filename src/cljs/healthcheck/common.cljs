(ns healthcheck.common
  (:require [re-frame.core :refer [subscribe]]
            [healthcheck.routes :refer [navigate]]))

(defn mark-env [is-ok]
  (if is-ok "green-background" "red-background"))


(defn status-icon [status]
      (if (= "UP" status)
        [:i.material-icons.status-icon.green "check"]
        [:i.material-icons.status-icon.red "report"]))

(defn list-environments [style]
  (let [env-statuses (subscribe [:env-statuses])
        pick-env #(navigate (name %))]
    (fn [style]
      [:div {:class style}
       [:a.menu-item {:on-click #(pick-env :dev)
                      :class    (mark-env (:dev @env-statuses))}
        [:i.material-icons "build"] "Dev"]
       [:a.menu-item {:on-click #(pick-env :test)
                      :class    (mark-env (:test @env-statuses))}
        [:i.material-icons "bug_report"] "Test"]
       [:a.menu-item {:on-click #(pick-env :production)
                      :class    (mark-env (:production @env-statuses))}
        [:i.material-icons "public"] "Production"]])))
