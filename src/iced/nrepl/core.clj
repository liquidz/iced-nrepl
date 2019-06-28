(ns iced.nrepl.core)

(defn version []
  (-> (slurp "project.clj")
      read-string
      (nth 2)))

(defn ^{:doc "Returns the version of iced-nrepl middleware."
        :requires {}
        :optional {}
        :returns {"version" "The version of iced-nrepl-middleware."
                  "status" "done"}}
  iced-version [_]
  {:version (version)})
