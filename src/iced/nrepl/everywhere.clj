(ns iced.nrepl.everywhere
  (:require [iced.nrepl.namespace :as i.n.ns]
            [orchard.namespace :as o.ns]))

(defn- namespace-candidate [ns-sym]
  {:sym ns-sym
   :file (o.ns/ns-path ns-sym)
   :name (str ns-sym)
   :type "namespace"
   :line 1
   :column 1})

(defn- function-candidates [{:keys [sym file]} current-filepath]
  (for [[_ v] (try (ns-interns sym) (catch Exception _ []))
        :let [current-file? (= file current-filepath)]]
    (-> (meta v)
        (select-keys [:line :column :name])
        (update :name #(if current-file?  (str %) (str sym "/" %)))
        (assoc :file file :type "function"))))

(defn candidates [& [current-filepath]]
  (let [namespaces (i.n.ns/project-namespaces)
        ns-candidates (map namespace-candidate namespaces)
        fn-candidates (mapcat #(function-candidates % current-filepath) ns-candidates)]
    (concat ns-candidates
            fn-candidates)))
