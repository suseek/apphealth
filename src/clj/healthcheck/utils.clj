(ns healthcheck.utils
  (:require [clojure.string :refer [blank? starts-with? ends-with?]]))

(defn json? [json]
  (and (not(blank? json)) (or (and (starts-with? json "[") (ends-with? json "]")) (and (starts-with? json "{") (ends-with? json "}")))))