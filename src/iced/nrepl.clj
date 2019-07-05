(ns iced.nrepl
  (:require [iced.nrepl.core :as core]
            [iced.nrepl.debug :as debug]
            [iced.nrepl.format :as format]
            [iced.nrepl.lint :as lint]
            [iced.nrepl.namespace :as namespace]
            [iced.nrepl.refactor.thread :as refactor.thread]
            [iced.nrepl.spec :as spec]
            [iced.nrepl.transport :as transport]))

(if (find-ns 'clojure.tools.nrepl)
  (require
   '[clojure.tools.nrepl.middleware :refer [set-descriptor!]])
  (require
   '[nrepl.middleware :refer [set-descriptor!]]))

(defn- transform-ops [sym]
  (let [v (resolve sym)
        m (meta v)
        op-name (name sym)]
    {op-name (-> m
                 (select-keys [:doc :requires :optional :returns])
                 (assoc :handler (var-get v)))}))

(def iced-nrepl-ops
  (->> '[core/iced-version
         debug/iced-list-tapped
         debug/iced-browse-tapped
         debug/iced-clear-tapped
         debug/iced-complete-tapped
         format/iced-calculate-indent-level
         format/iced-format-code-with-indents
         format/iced-set-indentation-rules
         lint/iced-lint-file
         namespace/iced-project-ns-list
         namespace/iced-pseudo-ns-path
         refactor.thread/iced-refactor-thread-first
         refactor.thread/iced-refactor-thread-last
         spec/iced-spec-check]
       (map transform-ops)
       (into {})))

(defn wrap-iced [handler]
  (fn [{:keys [op] :as msg}]
    (if-let [f (get-in iced-nrepl-ops [op :handler])]
      (when-let [res (f msg)]
        (transport/send! msg (merge {:status :done} res)))
      (handler msg))))

(when (resolve 'set-descriptor!)
  (set-descriptor!
   #'wrap-iced
   {:doc ""
    :requires #{}
    :expects #{}
    :handles
    (reduce-kv #(assoc %1 %2 (dissoc %3 :handler)) {} iced-nrepl-ops)}))
