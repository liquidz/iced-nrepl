(ns iced.nrepl.format
  (:require [cljfmt.core :as fmt]
            [clojure.string :as str]
            [medley.core :as medley]))

(def ^:private indentation-rules
  (atom fmt/default-indents))

(defn- keyword->string [kw]
  (subs (str kw) 1))

(def ^:private keyword->symbol
  (comp symbol keyword->string))

(defn set-indentation-rules! [rules]
  (->> rules
       (reduce (fn [res [k v]]
                 (assoc res (keyword->symbol k) (read-string v)))
               fmt/default-indents)
       (reset! indentation-rules)))

(defn- parse-error-message [s]
  (if-let [[[_ line column]] (re-seq #"at line (\d+), column (\d+)" s)]
    {:error s :line (Long/parseLong line) :column (Long/parseLong column)}
    {:error s}))

(defn code [code-str alias-map]
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
