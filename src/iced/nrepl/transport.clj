(ns iced.nrepl.transport)

(if (find-ns 'clojure.tools.nrepl)
  (require
   '[clojure.tools.nrepl.misc :refer [response-for]]
   '[clojure.tools.nrepl.transport :as transport])
  (require
   '[nrepl.misc :refer [response-for]]
   '[nrepl.transport :as transport]))

(def send-list-limit 50)

(defn send!
  [msg m]
  (let [{:keys [transport]} msg]
    (transport/send transport (response-for msg m))))
