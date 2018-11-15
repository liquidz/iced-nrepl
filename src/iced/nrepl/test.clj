(ns iced.nrepl.test
  (:require [medley.core :as medley]))

(defn test-vars [ns-sym]
  (->> (ns-publics ns-sym)
       (medley/filter-vals (comp fn? :test meta))
       keys
       (map str)))
