(ns iced.nrepl.namespace
  (:require [orchard.namespace :as o.ns]))

(defn project-ns-list []
  (o.ns/project-namespaces))
