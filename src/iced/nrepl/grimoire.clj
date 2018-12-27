(ns iced.nrepl.grimoire
  (:require [org.httpkit.client :as http]))

(def ^:private grimoire-url "https://conj.io")

(defn search [platform ns-name sym]
  (let [url (format "%s/search/v1/%s/%s/%s/"
                    grimoire-url platform ns-name
                    (http/url-encode sym))]
    @(http/get url {:headers {"Content-Type" "text/plain"}})))
