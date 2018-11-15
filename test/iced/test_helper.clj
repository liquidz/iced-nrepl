(ns iced.test-helper
  (:require [iced.nrepl :as iced]))

(if (find-ns 'clojure.tools.nrepl)
  (require
   '[clojure.tools.nrepl :as nrepl]
   '[clojure.tools.nrepl.server :as server])
  (require
   '[nrepl.core :as nrepl]
   '[nrepl.server :as server]))

(def ^:dynamic *session* nil)

(defn repl-server-fixture
  [f]
  (with-open [server (server/start-server :handler (server/default-handler #'iced/wrap-iced))
              transport (nrepl/connect :port (:port server))]
    (let [client (nrepl/client transport Long/MAX_VALUE)]
      (binding [*session* (nrepl/client-session client)]
        (f)))))

(defn message
  ([m] (message m true))
  ([m combine?]
   (cond-> (nrepl/message *session* m)
     combine? nrepl/combine-responses)))
