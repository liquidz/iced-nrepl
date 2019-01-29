(ns iced.alias.json
  "mranderson will change `clojure.data.json` namespace name.
  This ns is an alias for `clojure.data.json`."
  (:require [clojure.data.json :as json]))

(def read-str json/read-str)
(def write-str json/write-str)
