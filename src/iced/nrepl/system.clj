(ns iced.nrepl.system
  (:require [clojure.string :as str]))

(defn info []
  (let [user-dir (System/getProperty "user.dir")
        sep (System/getProperty "file.separator")]
    {:user-dir user-dir
     :file-separator sep
     :project-name (-> (.split user-dir sep) seq last)}))
