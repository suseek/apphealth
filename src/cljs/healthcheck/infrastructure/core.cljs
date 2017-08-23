(ns healthcheck.infrastructure.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [clojure.string :as str]
            [re-frame.core :refer [dispatch
                                   dispatch-sync
                                   subscribe]]
            [healthcheck.single-app :refer [focused-app-view get-version]]
            [healthcheck.nav-bar :refer [nav-bar]]
            [healthcheck.common :refer [mark-env status-icon]]
            [healthcheck.side-bar :refer [side-bar]]
            [healthcheck.infrastructure.handlers]
            [healthcheck.infrastructure.subs]
            [healthcheck.routes :refer [navigate]]
            [healthcheck.main-page :refer [main-page]]))

(defn render-row [env [app-id app-details]]
  (fn [env [app-id app-details]]
    [:div.row {:on-mouse-down #(navigate (name env) (name app-id))}
     [status-icon (:status app-details)]
     [:span.name (:name app-details)]
     [:span.version (get-version app-details)]]))

(defn sorting [name env key]
    (fn [name env key]
      (let [reverse? (subscribe [:sorting-direction env])]
      [:span.option {:on-mouse-down #(dispatch [:change-sorting env (not @reverse?)])}
        name [:i.material-icons.arrow-down {:class (if @reverse? "opened" "")} "arrow_drop_down"]])))

(defn paging [env pages-count]
  (let [current-page (subscribe [:current-page env])]
    (fn [env pages-count]
      [:span.pagination
      [:ul
       (->> (range pages-count)
            (map (fn [num]
                   (let [visible-page-num (+ num 1)
                         change-page #(dispatch [:current-page env num])]
                     ^{:key (str "-page-" num)}
                     [:li.page
                   (if (not= @current-page num)
                     [:a.num {:on-click change-page } visible-page-num]
                     [:a.num.active [:strong visible-page-num]]
                     )])))
            doall)]])))

(defn apps-per-page [limit]
  [:span.pagination-range "Apps per page: "
   [:input {:value   @limit
            :min 2
            :on-change   #(reset! limit (-> % .-target .-value))
            :on-blur     #(if (= (-> % .-target .-value) "")
                           (reset! limit "10"))}]])

(defn show-failed-switch [env show-failed?]
  [:div.switch
   [:label [:text (if (not @show-failed?) {:class "green"}) "All apps"]
    [:input {:checked @show-failed? :type "checkbox"
             :onChange #(do (dispatch [:current-page env 0])
                            (dispatch [:show-only-failed (not @show-failed?)]))}]
    [:span.lever]
    [:text (if @show-failed? {:class "red"}) "Inoperative apps"]]])

(defn ->limit [limit-value]
  (let [starting-with-zeros? (not (empty? (re-find #"0+" @limit-value)))]
  (if (or starting-with-zeros? (contains? #{"" "1" "0"} @limit-value)) "10" @limit-value)))

(defn environment-view [env title]
  (let [sorted-apps (subscribe [:current-env-apps])
        current-env-page (subscribe [:current-page])
        show-failed? (subscribe [:show-only-failed])
        limit-value (atom "10")
        ]
    (fn [env title]
      (let [limit (js/parseInt (->limit limit-value))
            partitioned-apps (partition limit limit nil @sorted-apps)
            pages-count (count partitioned-apps)]
      [:div.panel.animate-fade-in
       [:header
        [:span.title title]]
        [:div.panel-options
         [sorting "Name" env :name]
         [apps-per-page limit-value]
         [show-failed-switch env show-failed?]
         [paging env pages-count]]
       (into [:div.main-content]
             (if (not (empty? partitioned-apps))
             (->> (nth partitioned-apps @current-env-page)
                  (map (fn [app]
                         [render-row env app])))))]))))

(defn focused-content []
  (let [focussed-env (subscribe [:focusing-env])
        focussed-app (subscribe [:focussed-app])
        scroll-position (subscribe [:scroll-position])]
    (fn []
        [:div.content-wrapper {:id "main-content"}
         [:div.breadcrumbs
          ;;(if (>= @scroll-position 20) {:class "fix"})
          [:a.breadcrumb {:on-mouse-down #(navigate)} "AppHealth"]
          [:a.breadcrumb {:on-mouse-down #(navigate (name @focussed-env))}
           (str (str/capitalize (name @focussed-env)) " environment")]
          (if @focussed-app [:a.breadcrumb (:name @focussed-app)])]
         (if @focussed-app
           [focused-app-view @focussed-app]
           [environment-view @focussed-env
            (str (str/capitalize (name @focussed-env)) " environment")])])))

(defn main-component []
  (let [is-focused? (subscribe [:focusing-env])
        search-on? (subscribe [:search-on])]
    (fn []
      [:div
       [nav-bar]
        [:div.cover (if @search-on? {:class "state--show"})]
       (if @is-focused?
         [:div.container [side-bar] [focused-content]]
         [main-page])])))

(defn current-page []
  (let [page-loaded? (subscribe [:loaded])]
    (fn []

      (if @page-loaded? [:div [(session/get :current-page)]]
                        [:div.spinner]))))

(.addEventListener js/document "scroll" #(dispatch [:scroll-top (->> (.-body js/document) .-scrollTop)]))

(secretary/defroute "/" []
                    (session/put! :current-page #'main-component)
                    (dispatch [:without-focus]))

(secretary/defroute "/health/:env/:id" {env :env id :id}
                    (session/put! :current-page #'main-component)
                    (dispatch [:change-focusing-app (keyword env) (keyword id)]))

(secretary/defroute "/health/:env" {env :env}
                    (session/put! :current-page #'main-component)
                    (dispatch [:change-env (keyword env)]))
;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (dispatch-sync [:initialize])
  (dispatch-sync [:scroll-top])
  (mount-root))
