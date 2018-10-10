(ns iced.nrepl
  (:require [iced.nrepl
             [core :as core]
             [format :as format]
             [function :as function]
             [grimoire :as grimoire]
             [lint :as lint]
             [namespace :as namespace]
             [spec :as spec]
             [system :as system]]
            [iced.nrepl.refactor.thread :as refactor.thread]))

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

(def ^:private send-list-limit 50)

(defn- version-reply [_]
   {:version (core/version)})

(defn- lint-file-reply [msg]
  (let [{:keys [transport file env linters]} msg
        env (or env "clj")]
    (try
      (let [res (lint/lint-file file env linters)]
        (doseq [ls (partition-all send-list-limit res)]
          (transport/send transport (response-for msg {:lint-warnings ls})))
        (transport/send transport (response-for msg {:status :done}))
        nil)
      (catch Throwable ex
        {:lint-warnings [] :error (.getMessage ex)}))))

(defn- grimoire-reply [msg]
  (let [{platform :platform ns-name :ns sym :symbol} msg
        res (grimoire/search platform ns-name sym)]
    (if (= 200 (:status res))
      {:content (:body res)}
      {:status #{:done :failed} :http-status (:status res)})))

(defn- project-namespaces-reply [msg]
  (let [{:keys [transport prefix]} msg
        result (namespace/project-namespaces prefix)]
    (doseq [ls (partition-all send-list-limit result)]
      (transport/send transport (response-for msg {:namespaces ls})))
    (transport/send transport (response-for msg {:status :done})))
  nil)

(defn- related-namespaces-reply [msg]
  (let [{:keys [transport ns]} msg
        result (namespace/related-namespaces ns)]
    (doseq [ls (partition-all send-list-limit result)]
      (transport/send transport (response-for msg {:related-namespaces ls})))
    (transport/send transport (response-for msg {:status :done})))
  nil)

(defn- set-indentation-rules-reply [msg]
  (let [{:keys [rules]} msg]
    (format/set-indentation-rules! rules)
    {:status #{:done}}))

(defn- format-code-with-indents-reply [msg]
  (let [{:keys [code alias-map]} msg]
    (format/code code alias-map)))

(defn- project-functions-reply [msg]
  (let [{:keys [transport prefix]} msg
        result (function/project-functions prefix)]
    (doseq [ls (partition-all send-list-limit result)]
      (transport/send transport (response-for msg {:functions ls})))
    (transport/send transport (response-for msg {:status :done})))
  nil)

(defn- ns-aliases-reply [msg]
  (let [{:keys [env code]} msg]
    (try
      {:aliases (namespace/aliases env code)}
      (catch Exception ex
        {:aliases {} :error (.getMessage ex)}))))

(defn- refactor-thread-first-reply [msg]
  (try
    {:code (refactor.thread/thread-first (:code msg))}
    (catch Exception ex
      {:status #{:done :failed} :error (.getMessage ex)})))

(defn- refactor-thread-last-reply [msg]
  (try
    {:code (refactor.thread/thread-last (:code msg))}
    (catch Exception ex
      {:status #{:done :failed} :error (.getMessage ex)})))

(defn- spec-check-reply [msg]
  (let [{sym :symbol num-tests :num-tests} msg]
    (spec/check (symbol sym) num-tests)))

(def iced-nrepl-ops
  {"iced-version" version-reply
   "lint-file" lint-file-reply
   "grimoire" grimoire-reply
   "system-info" (fn [_msg] (system/info))
   "project-namespaces" project-namespaces-reply
   "related-namespaces" related-namespaces-reply
   "project-functions" project-functions-reply
   "ns-aliases" ns-aliases-reply
   "set-indentation-rules" set-indentation-rules-reply
   "format-code-with-indents" format-code-with-indents-reply
   "refactor-thread-first" refactor-thread-first-reply
   "refactor-thread-last" refactor-thread-last-reply
   "spec-check" spec-check-reply})

(defn wrap-iced [handler]
  (fn [{:keys [op transport] :as msg}]
    (if-let [f (get iced-nrepl-ops op)]
      (when-let [res (f msg)]
        (transport/send transport (response-for msg (merge {:status :done} res))))
      (handler msg))))

(set-descriptor!
 #'wrap-iced
 {:requires #{}
  :expects #{}
  :handles (zipmap (keys iced-nrepl-ops) (repeat {:doc "See README" :requires {} :returns {}}))})
