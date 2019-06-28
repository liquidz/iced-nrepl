(ns iced.nrepl.namespace
  (:require [orchard.namespace :as o.ns]))

(defn ^{:doc "Returns all namespaces in the project."
        :requires {}
        :optional {}
        :returns {"project-ns-list" "Namespace names."
                  "status" "done"}}
  iced-project-ns-list [_msg]
  {:project-ns-list (o.ns/project-namespaces)})
