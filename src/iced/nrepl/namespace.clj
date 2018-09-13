(ns iced.nrepl.namespace
  (:require [orchard.namespace :as o.ns]))

(defn project-namespaces [& [prefix]]
  (let [ls (-> (o.ns/project-namespaces)
               sort
               distinct)]
    (cond->> ls
      prefix (filter #(.startsWith (str %) prefix)))))
