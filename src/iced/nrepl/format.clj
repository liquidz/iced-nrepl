(ns iced.nrepl.format
    (:require [cljfmt.core :as fmt]
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
