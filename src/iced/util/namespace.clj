(ns iced.util.namespace)

(defn extract-ns-sym [s]
  (when-let [[[_ res]] (re-seq #"\(ns[ \r\n]+([a-z-.]+)" s)]
    (symbol res)))
