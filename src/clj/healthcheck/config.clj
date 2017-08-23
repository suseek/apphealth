(ns healthcheck.config)

(def host "http://www.mocky.io/v2/")
(def times {:5s 5000 :10s 10000 :1-day 86400000})
(def mail-alert {:host ""
                 :from "me@example.com"
                 :send-to "all@example.com"})
(def api-docs "api-docs")
(def min-free-memory 15000)

(def sources {:dev  {1   {:name   "Sweet Child O'Mine"
                          :url host
                          :health-page "599d91212500000f01d3021e"
                          :repo "http://google.com/"
                          :jenkins "http://apple.com/"
                          :docs "http://github.com/"}
                     2   {:name   "Money for nothing"
                          :url host
                          :health-page "599d8a79250000cb00d301fb"
                          :repo "http://google.com/"
                          :jenkins "http://apple.com/"
                          :docs "http://github.com/"}
                     3   {:name   "Enter Sandman"
                          :url host
                          :health-page "599d8b06250000ea00d301fe"
                          :repo "http://google.com/"
                          :docs "http://github.com/"}
                     4   {:name   "My home page"
                          :url "http://szyszko.me/"
                          :repo "http://google.com/"
                          :jenkins "http://apple.com/"}
                     5   {:name   "Expenses app - EngageTech"
                          :url "http://engagetech-expenses.herokuapp.com/expenses/"
                          :health-page "health"
                          :repo "https://github.com/suseek/backend-coding-challenge/tree/backend-challenge/"
                          :jenkins "http://apple.com/"}
                     }
              })


