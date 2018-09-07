(ns iced.nrepl.namespace
  (:require [orchard.namespace :as o.namespace]))

(defn project-namespaces []
  (sort (o.namespace/project-namespaces)))
