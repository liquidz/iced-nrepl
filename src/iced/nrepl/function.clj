(ns iced.nrepl.function
  (:require [iced.nrepl.namespace :as i.n.ns]))

(defn project-functions [& [prefix]]
  (mapcat (fn [ns-sym]
            (->> (try (ns-publics ns-sym) (catch Exception _ []))
                 (map (comp #(subs % 2) str second))))
          (i.n.ns/project-namespaces prefix)))
