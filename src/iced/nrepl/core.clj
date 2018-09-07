(ns iced.nrepl.core)

(defn version []
  (-> (slurp "project.clj")
      read-string
      (nth 2)))
