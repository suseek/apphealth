(ns healthcheck.infrastructure.subs
  (:require  [re-frame.core :refer [reg-sub
                                    subscribe]]))


(defn reg-simple [key]
  (reg-sub
    key
    (fn
      [db _]
      (key db))))

(reg-sub
  :focusing-env
  (fn [db _]
    (get-in db [:focus :env])))

(reg-sub
  :focusing-app-id
  (fn [db _]
    (get-in db [:focus :app])))

(reg-sub
  :focussed-app
  (fn [db _]
    (let [env (subscribe [:focusing-env])
          app-id (subscribe [:focusing-app-id])]
      (get-in db [:app-status @env @app-id]))))

(reg-sub
  :sorting-direction
  (fn [db [_ env]]
    (get-in db [:sorting env :reverse])))

(reg-sub
  :current-env-apps
  (fn [db _]
    (let [env (subscribe [:focusing-env])
          apps (get-in db [:app-status @env])
          reverse? (subscribe [:sorting-direction @env])
          show-failed? (subscribe [:show-only-failed])
          sort-key (get-in db [:sorting @env :key] :name)
          initially-sorted-apps (sort-by #(->> %
                                     val
                                     sort-key
                                     .toLowerCase) apps)
          sorted-apps (if @reverse? (rseq initially-sorted-apps) initially-sorted-apps)
          failed-apps (filter #(= (->> % val :status .toLowerCase) "down") sorted-apps)]
      (if @show-failed? failed-apps sorted-apps))))

(reg-sub
  :env-statuses
  (fn
    [db _]
    (let [app-statuses (:app-status db)]
      (into {} (->> (keys app-statuses)
                    (map (fn [env] [env (env app-statuses)]))
                    (map (fn [[env env-apps]]
                           [env
                            (->> env-apps
                                 (map #(:status (val %)))
                                 (map #(not= "DOWN" %))
                                 (reduce #(and %1 %2)))])))))))

(reg-sub
  :search-result
  (fn
    [db _]
    (let [type-ahead-text (:type-ahead db)]
      (into {} (->> (keys (:app-status db))
                    (map (fn [env] [env (env (:app-status db))]))
                    (map (fn [[env env-apps]]
                           [env
                            (into {} (->> env-apps
                                          (filter #(re-find (re-pattern (str "(?i)" type-ahead-text))
                                                            (:name (val %))))))]))
                    (filter (fn [[env apps]] (not-empty apps))))))))

(reg-sub
  :loaded
  (fn [db _]
    (not (empty? (get-in db [:app-status :dev])))))

(reg-sub
  :current-page
  (fn [db _]
    (let [current-env (subscribe [:focusing-env])]
    (get-in db [:paging @current-env]))))

(reg-simple :app-status)
(reg-simple :search-on)
(reg-simple :type-ahead)
(reg-simple :scroll-position)
(reg-simple :show-only-failed)
