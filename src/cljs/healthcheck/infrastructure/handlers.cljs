(ns healthcheck.infrastructure.handlers
  (:require [re-frame.core :refer [reg-event-db]]
            [healthcheck.infrastructure.websockets :as ws]
            [clojure.walk :refer [keywordize-keys]]
            [healthcheck.infrastructure.db :refer [init-db]]
            [re-frame.core :refer [dispatch
                                   reg-event-db
                                   subscribe]]))

(reg-event-db
  :update-websocket
  (fn
    [db [_ status]]
    (update-in db [:app-status] conj status)))

(defn update-messages! [{:keys [message]}]
  (dispatch [:update-websocket (keywordize-keys message)]))

(reg-event-db
  :initialize
  (fn
    [db _]
    (js/console.log "init!")
    (ws/make-websocket! (str "ws://" (.-host js/location) "/ws") update-messages!)
    (merge db init-db)))

(reg-event-db
  :scroll-top
  (fn
    [db [_ position]]
    (assoc db :scroll-position position)))

(reg-event-db
  :change-env
  (fn
    [db [_ new-env]]
    (assoc db :focus {:env new-env})))

(reg-event-db
  :change-sorting
  (fn
    [db [_ env reverse?]]
    (assoc-in db [:sorting env :reverse] reverse?)))

(reg-event-db
  :change-focusing-app
  (fn
    [db [_ new-env new-id]]
    (assoc db :focus {:env new-env :app new-id})))

(reg-event-db
  :without-focus
  (fn
    [db _]
    (assoc db :focus {:env nil :app nil})))

(reg-event-db
  :search-on
  (fn
    [db [_ value]]
    (assoc db :search-on value)))

(reg-event-db
  :type-ahead
  (fn
    [db [_ value]]
    (assoc db :type-ahead value)))

(reg-event-db
  :current-page
  (fn
    [db [_ env page-number]]
    (assoc-in db [:paging env] page-number)))

(reg-event-db
  :show-only-failed
  (fn
    [db [_ show-failed?]]
    (assoc db :show-only-failed show-failed?)))
