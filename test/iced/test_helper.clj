(ns iced.test-helper
  (:require
   [iced.nrepl :as iced]
   [nrepl.core :as nrepl]
   [nrepl.server :as server])
  (:import
   nrepl.server.Server
   nrepl.transport.FnTransport))

(def ^:dynamic *session* nil)

(defn repl-server-fixture
  [f]
  (let [handler (server/default-handler #'iced/wrap-iced)]
    (with-open [server ^Server (server/start-server :handler handler)
                transport ^FnTransport (nrepl/connect :port (:port server))]
      (let [client (nrepl/client transport Long/MAX_VALUE)]
        (binding [*session* (nrepl/client-session client)]
          (f))))))

(defn message
  ([m] (message m true))
  ([m combine?]
   (cond-> (nrepl/message *session* m)
     combine? nrepl/combine-responses)))
