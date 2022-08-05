(ns iced.nrepl.core
  (:require
   [clojure.java.io :as io]))

(defn version
  []
  (-> "version.txt"
      (io/resource)
      (slurp)))

(defn ^{:doc "Returns the version of iced-nrepl middleware."
        :requires {}
        :optional {}
        :returns {"version" "The version of iced-nrepl-middleware."
                  "status" "done"}}
  iced-version
  [_]
  {:version (version)})
