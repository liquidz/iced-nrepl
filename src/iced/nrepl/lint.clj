(ns iced.nrepl.lint
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.set :as set]
            [clojure.string :as str]
            [eastwood.lint :as el]
            [iced.util.namespace :as i.u.ns]))

(defn- to-long [s] (Long/parseLong s))

;; NOTE: joker does not ignore underscored binding
(defn- unused-underscored-binding? [msg]
  (or (str/includes? msg "unused binding: _")
      (str/includes? msg "Unable to resolve symbol: _")))

(defn- parse-error [s]
  (when-let [[[_ line column message]] (re-seq #"^.+:(\d+):(\d+): (.+)$" s)]
    (when-not (unused-underscored-binding? message)
      {:line (to-long line) :column (to-long column) :msg message})))

(defn- working-directory [file-path]
  (.. (io/file file-path)
      getParentFile
      getAbsolutePath))

(defn lint-by-joker [file-path]
  (let [dir (working-directory file-path)
        {:keys [err]} (shell/sh "joker" "--working-dir" dir "--lint" file-path)
        errors (-> err str/trim (str/split #"[\r\n]+"))]
    (->> errors
         (map parse-error)
         (remove nil?)
         (map #(assoc % :path file-path)))))

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
    (to-long n)))

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

(defn lint-file [file-path env eastwood-linters]
  (or (check-file-syntax file-path)
      (and (= env "clj")
           (some-> (slurp file-path)
                   i.u.ns/extract-ns-sym
                   (lint-by-eastwood eastwood-linters)))
      (and (= env "cljs")
           (lint-by-joker file-path))))
