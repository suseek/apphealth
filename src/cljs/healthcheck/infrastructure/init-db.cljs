(ns healthcheck.infrastructure.db)

(def init-db
  {:app-status {:dev {} :test {} :production {}}
   :focus      {:env nil :app nil}
   :search-on false
   :type-ahead ""
   :sorting {:dev {:key :name :reverse false}
             :test {:key :name :reverse false}
             :production {:key :name :reverse false}}
   :scroll-position 0
   :paging {:dev 0
            :test 0
            :production 0}
   :show-only-failed false})
