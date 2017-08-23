(ns healthcheck.side-bar
  (:require [re-frame.core :refer [dispatch
                                   subscribe]]
            [healthcheck.common :refer [mark-env list-environments]]))

(defn side-bar []
    (fn []
      [:div.menu.animate-slide-in
       [:span.title "Menu"]
       [:hr]
       [list-environments "sidebar-buttons"]]))
