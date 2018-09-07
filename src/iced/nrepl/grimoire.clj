(ns iced.nrepl.grimoire
  (:require [clj-http.client :as client]))

(def ^:private grimoire-url "https://conj.io")

(defn search [platform ns-name sym]
  (let [url (format "%s/search/v1/%s/%s/%s/" grimoire-url platform ns-name sym)]
    (client/get url {:content-type :text})))
