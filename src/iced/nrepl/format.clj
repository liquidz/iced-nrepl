(ns iced.nrepl.format
  (:require [cljfmt.core :as fmt]
            [clojure.string :as str]
            [medley.core :as medley]))

(def ^:private indentation-rules
  (atom fmt/default-indents))

(defn- keyword->string [^clojure.lang.Keyword kw]
  (str (.sym kw)))

(defn- read-symbol [sym]
  (read-string (str sym)))

(defn- read-keyword [^clojure.lang.Keyword kw]
  (let [sym (.sym kw)]
    (cond-> sym
      (str/starts-with? (str sym) "#\"") read-symbol)))

(defn set-indentation-rules! [rules overwrite?]
  (let [default-indents (if overwrite? {} fmt/default-indents)]
    (->> rules
         (reduce (fn [res [k v]]
                   (assoc res (read-keyword k) (read-string v)))
                 default-indents)
         (reset! indentation-rules))))

(defn- parse-error-message [s]
  (if-let [[[_ line column]] (re-seq #"at line (\d+), column (\d+)" s)]
    {:error s :line (Long/parseLong line) :column (Long/parseLong column)}
    {:error s}))

(defn format-code [code-str alias-map]
  (let [option {:indents @indentation-rules
                :alias-map (medley/map-keys keyword->string alias-map)}]
    (try
      {:formatted (fmt/reformat-string code-str option)}
      (catch Exception ex
        (parse-error-message (.getMessage ex))))))

(defn calcalate-indent-level [code-str cursor-line-number alias-map]
  (let [option {:indents @indentation-rules
                :alias-map (medley/map-keys keyword->string alias-map)
                :remove-consecutive-blank-lines? false
                :remove-surrounding-whitespace? false
                :insert-missing-whitespace? false
                :indentation? true
                :remove-trailing-whitespace? false}
        indent-keyword "::__vim-iced-calc-indent__"
        ;; HACK: cljfmt does not indent correctly without any elements.
        ;;       To indent correctly, add a dummy keyword.
        code-str (->> (str/split code-str #"\r?\n" (inc cursor-line-number))
                      (map-indexed #(cond->> %2 (= %1 cursor-line-number)
                                             (str indent-keyword)))
                      (str/join "\n"))]
    (try
      (let [indented (fmt/reformat-string code-str option)
            x (str/index-of indented indent-keyword)
            y (inc (or (str/last-index-of indented "\n" x) -1))]
        {:indent-level (- x y)})
      (catch Exception ex
        (parse-error-message (.getMessage ex))))))

(defn
  ^{:doc "Sets indentation rules for formatting."
    :requires {"rules" "Indentation rule map. Default rules are `cljfmt.core/default-indents`."}
    :optional {"overwrite?" "If logical true, `cljfmt.core/default-indents` will not be used."}
    :returns {"status" "done"}}
  iced-set-indentation-rules [msg]
  (let [{:keys [rules overwrite?]} msg]
    (set-indentation-rules! rules overwrite?)
    {:status #{:done}}))

(defn ^{:doc "Formats codes by rules which is set by `iced-set-indentation-rules` op."
        :requires {"code" "Code to format."
                   "alias-map" "Namespace alias map."}
        :optional {}
        :returns {"formatted" "Formatted code."
                  "error" "Error message if occured."
                  "line" "Error line number if occured."
                  "column" "Error column number if occured."
                  "status" "done"}}
  iced-format-code-with-indents [msg]
  (let [{:keys [code alias-map]} msg]
    (format-code code alias-map)))

(defn ^{:doc "Returns the indentation level."
        :requires {"code" "Code to calculate indentation level."
                   "line-number" "Line number to calculate indentation level. This is zero-based."
                   "alias-map" "Namespace alias map."}
        :optional {}
        :returns {"indent-level" "Calculated indentation level."
                  "error" "Error message if occured."
                  "line" "Error line number if occured."
                  "column" "Error column number if occured."
                  "status" "done"}}
  iced-calculate-indent-level [msg]
  (let [{:keys [code line-number alias-map]} msg]
    (calcalate-indent-level code line-number alias-map)))
