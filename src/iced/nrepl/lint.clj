(ns iced.nrepl.lint
  (:require [clojure
             [set :as set]
             [string :as str]]
            [clojure.java.io :as io]
            [eastwood.lint :as el]
            [iced.util.namespace :as i.u.ns]))

(defn lint-by-eastwood [ns-sym linters]
  (let [linters (when (sequential? linters)
                  (map keyword linters))
        opt (cond-> {:namespaces [ns-sym]}
              (seq linters) (assoc :linters linters))]
    (for [warn (:warnings (el/lint opt))]
      (-> warn
          (select-keys [:msg :column :line :uri])
          (update :uri #(.getPath %))
          (set/rename-keys {:uri :path})))))

(defn- remove-exception-name [s]
  (str/replace s "java.lang.RuntimeException: " ""))

(defn- extract-line-number [s]
  (when-let [[[_ n]] (re-seq #"starting at line (\d+)" s)]
      (Long/parseLong n)))

(defn check-file-syntax [file-path]
  (with-open [rdr (clojure.lang.LineNumberingPushbackReader. (io/reader file-path))]
    (binding [*read-eval* false]
      (try
        (loop [result nil]
          (when-not (= result :__EOF__)
            (recur (read rdr false :__EOF__))))
        (catch Exception ex
          (let [msg (remove-exception-name (.getMessage ex))
                line (or (extract-line-number msg) (.getLineNumber rdr))]
            [{:msg msg :line line :column 0 :path file-path}]))))))

(defn lint-file [file-path eastwood-linters]
  (or (check-file-syntax file-path)
      (some-> (slurp file-path)
              i.u.ns/extract-ns-sym
              (lint-by-eastwood eastwood-linters))))
