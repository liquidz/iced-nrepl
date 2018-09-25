(ns iced.util.namespace)

(defn extract-ns-sym [s]
  (when-let [[[_ res]] (re-seq #"\(ns[ \r\n]+([a-z0-9-.]+)" s)]
    (symbol res)))
