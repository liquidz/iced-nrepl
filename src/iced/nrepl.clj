(ns iced.nrepl
  (:require [iced.nrepl
             [core :as core]
             [format :as format]
             [grimoire :as grimoire]
             [lint :as lint]
             [namespace :as namespace]]))

(when-not (resolve 'set-descriptor!)
  (if (find-ns 'clojure.tools.nrepl)
    (require
     '[clojure.tools.nrepl.middleware :refer [set-descriptor!]]
     '[clojure.tools.nrepl.misc :refer [response-for]]
     '[clojure.tools.nrepl.transport :as transport])
    (require
     '[nrepl.middleware :refer [set-descriptor!]]
     '[nrepl.misc :refer [response-for]]
     '[nrepl.transport :as transport])))

(defn- version-reply [_]
   {:version (core/version)})

(defn- lint-ns-reply [msg]
  (let [{ns-name :ns linters :linters} msg]
    (try
      (let [res (lint/lint-by-eastwood (symbol ns-name) linters)]
        {:lint-warnings res})
      (catch Exception ex
        {:lint-warnings [] :error (.getMessage ex)}))))

(defn- grimoire-reply [msg]
  (let [{platform :platform ns-name :ns sym :symbol} msg
        res (grimoire/search platform ns-name sym)]
    (if (= 200 (:status res))
      {:content (:body res)}
      {:status #{:done :failed} :http-status (:status res)})))

(defn- project-namespaces-reply [_msg]
  {:namespaces (namespace/project-namespaces)})

(defn- format-code-with-indents-reply [msg]
  (let [{:keys [code indents]} msg]
    {:formatted (format/code code indents)}))

(def iced-nrepl-ops
  {"iced-version" version-reply
   "lint-ns" lint-ns-reply
   "grimoire" grimoire-reply
   "project-namespaces" project-namespaces-reply
   "format-code-with-indents" format-code-with-indents-reply})

(defn wrap-iced [handler]
  (fn [{:keys [op transport] :as msg}]
    (if-let [f (get iced-nrepl-ops op)]
      (let [res (merge {:status :done} (f msg))]
        (transport/send transport (response-for msg res)))
      (handler msg))))

(set-descriptor!
 #'wrap-iced
 {:requires #{}
  :expects #{}
  :handles (zipmap (keys iced-nrepl-ops) (repeat {:doc "See README" :requires {} :returns {}}))})
