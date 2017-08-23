(ns healthcheck.single-app
  (:require [reagent.core :refer [atom]]
            [healthcheck.common :refer [status-icon]]))

(defn get-version [app-info]
  (if (:app-version app-info)
    (:app-version app-info)
    "N/A"))

(defn mark-app-status [status]
  (fn [status]
    (if status
      (if (= status "UP")
        [:div.status.up [:i.material-icons "check"]]
        [:div.status.down [:i.material-icons "priority_high"]]))))

(defn header [app-info]
  (fn [app-info]
    [:span.general
     [:p.app-title [:a {:href (:url app-info) :target "_blank"} (:name app-info)]]
     [:p "Version: " (get-version app-info)]]))

(defn detail->label [detail]
  [:span {:style {:padding "10px"}} detail])

(defn parse-map-details [map-elements]
  [:ul.app-details-list
   (->> map-elements
        (filter #(not= "status" (name (key %))))
        (map (fn [[element-name value]]
               ^{:key (str "detail-item-value-" element-name)}
               [:li {:class (if-not (map? value) "element-name")}
                (if (map? value) [:span.element-name [:i.material-icons "keyboard_arrow_right"](name element-name)]
                                 [:span {:style {:padding "5px" :background-color "#49B1B7" :border-radius "2px" :color "#fff"}} (name element-name)])
                (if (map? value) (parse-map-details value)
                                 [detail->label value])])))])

(defn render-details [title app-info]
  (cond
    (map? app-info) [parse-map-details app-info]
    (vector? app-info) (into [:ul.app-detail-list] (->> app-info
                                                        (map (fn [app-info-item]
                                                               [:li [parse-map-details app-info-item]]))))
    :else [:ul [:li [:span [detail->label app-info]]]]))

(defn small-button [url icon color desc]
  (let [hover? (atom false)]
    (fn [url icon color desc]
      [:div
       [:a.small-button {:on-mouse-over  #(reset! hover? true)
                         :on-mouse-leave #(reset! hover? false)
                         :href           url
                         :target         "_blank"
                         :style          {:background-color color}} [:i.material-icons icon]]
       [:div.desc
        {:style {:opacity (if @hover? 1 0) :visibility
                          (if @hover? "visible" "hidden")
                 :margin-left (str "-" (* (count desc) 2.5) "px")}} desc]])))

(defn info-buttons [app-info]
  (let [show-info (atom false)
        has-info? (if (some app-info [:docs :repo]) true false)]
    (fn [app-info]
      (if has-info?
        [:div.more-info {:on-mouse-leave #(reset! show-info false)}
         [:div.icons {:style {:opacity (if @show-info 1 0) :visibility
                                       (if @show-info "visible" "hidden")}}
          (if (:docs app-info) [small-button (:docs app-info) "description" "#F77F00" "Docs"])
          (if (:repo app-info) [small-button (:repo app-info) "storage" "#6734BA" "Repo"])
          (if (:jenkins app-info) [small-button (:jenkins app-info) "build" "#1194F6" "Jenkins"])]
         [:div.more-info-button {:on-mouse-over #(reset! show-info true)}
          [:i.material-icons "more_horiz"]]]))))

(defn info-row [element-name properties]
  (let [showing-details? (atom false)]
    (fn [element-name properties]
      (let [dropdown-properties (if (map? properties) (dissoc properties :status) properties)]
      (if-not (empty? dropdown-properties) [:li.collection-item
                                           [:div.header {:style {:cursor "pointer"}
                                                         :on-mouse-down #(reset! showing-details? (not @showing-details?))}
                                            [mark-app-status (:status properties)]
                                            element-name
                                            [:i.material-icons.arrow-down {:class (if @showing-details? "opened" "")} "keyboard_arrow_down"]]
                                           [:div.content {:class (if @showing-details? "active" "")}
                                            [render-details element-name dropdown-properties]]]
                                           [:li.collection-item
                                           [:div.header {:disabled "disabled"}
                                            [mark-app-status (:status properties)]
                                            element-name]])))))

(defn focused-app-view [app-info]
    (fn [app-info]
      [:div.panel.app.animate-fade-in
       [:ul
        [:li.collection-item.header
         [status-icon (:status app-info)]
         [header app-info]
         [info-buttons app-info]]
          (if (:info app-info)
            (->> (:info app-info)
                 (filter #(not= "status" (name (key %))))
                  (map (fn [[property value]]
                         ^{:key property}
                         [info-row property value]))))
        (if (:mem-free app-info)
          [info-row "Metrics" (:mem-free app-info)])]]))
