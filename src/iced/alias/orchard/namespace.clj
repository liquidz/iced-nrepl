(ns iced.alias.orchard.namespace
  "mranderson will change `orchard.namespace` namespace name.
  This ns is an alias for `orchard.namespace`."
  (:require
   [orchard.namespace :as o.ns]))

(def project-namespaces o.ns/project-namespaces)
