(ns iced.nrepl
  (:require [clojure.data.json :as json]
            [iced.nrepl.core :as core]
            [iced.nrepl.format :as format]
            [iced.nrepl.grimoire :as grimoire]
            [iced.nrepl.lint :as lint]
            [iced.nrepl.namespace :as namespace]
            [iced.nrepl.refactor.thread :as refactor.thread]
            [iced.nrepl.spec :as spec]))

(if (find-ns 'clojure.tools.nrepl)
  (do
    (require
     '[clojure.tools.nrepl.middleware :refer [set-descriptor!]]
     '[clojure.tools.nrepl.misc :refer [response-for]]
     '[clojure.tools.nrepl.transport :as transport])
    (import 'clojure.tools.nrepl.transport.Transport))
  (do
    (require
     '[nrepl.middleware :refer [set-descriptor!]]
     '[nrepl.misc :refer [response-for]]
     '[nrepl.transport :as transport])
    (import 'nrepl.transport.Transport)))

(def ^:private send-list-limit 50)

(defn- version-reply [_]
   {:version (core/version)})

(defn- lint-file-reply [msg]
  (let [{:keys [transport file env opt]} msg
        env (or env "clj")]
    (try
      (let [res (lint/lint-file file env opt)]
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
   "iced-lint-file" lint-file-reply
   "iced-grimoire" grimoire-reply
   "iced-related-namespaces" related-namespaces-reply
   "iced-ns-aliases" ns-aliases-reply
   "iced-set-indentation-rules" set-indentation-rules-reply
   "iced-format-code-with-indents" format-code-with-indents-reply
   "iced-refactor-thread-first" refactor-thread-first-reply
   "iced-refactor-thread-last" refactor-thread-last-reply
   "iced-spec-check" spec-check-reply})

(defn json-value-reply
  [{:keys [transport] :as msg} response]
  (let [data (try (json/write-str (:value response))
                  (catch Exception ex ex))]
    (transport/send transport (response-for msg (if (instance? Exception data)
                                                  {:json_error (.getMessage data)}
                                                  {:json data})))))

(defn json-value-transport
  [{:keys [^Transport transport] :as msg}]
  (reify Transport
    (recv [this] (.recv transport))
    (recv [this timeout] (.recv transport timeout))
    (send [this response]
      (when (contains? response :value)
        (json-value-reply msg response))
      (.send transport (dissoc response :value)))))

(defn wrap-iced [handler]
  (fn [{:keys [op transport] :as msg}]
    (if-let [f (get iced-nrepl-ops op)]
      (when-let [res (f msg)]
        (transport/send transport (response-for msg (merge {:status :done} res))))
      (handler (if (and (:json msg) (= "eval" op))
                 (assoc msg :transport (json-value-transport msg))
                 msg)))))

(when (resolve 'set-descriptor!)
  (set-descriptor!
   #'wrap-iced
   {:requires #{}
    :expects #{}
    :handles (zipmap (keys iced-nrepl-ops) (repeat {:doc "See README" :requires {} :returns {}}))}))
