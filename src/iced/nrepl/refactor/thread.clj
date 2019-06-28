(ns iced.nrepl.refactor.thread
  (:require [clojure.string :as str]))

(def ^:private replace-prefix "__ICED__")

(defmulti expand-sexp (fn [sym _] sym))

(defmethod expand-sexp '->
  [_ x]
  (loop [x x, expanded []]
    (let [[head target & tail] (if (sequential? x) x [])]
      (if (and target
               (not (vector? x))
               (not (str/starts-with? (str head) replace-prefix)))
        (recur target (conj expanded {:head head :tail tail}))
        (conj expanded {:value x})))))

(defmethod expand-sexp '->>
  [_ x]
  (loop [x x, expanded []]
    (let [[head & tail] (if (sequential? x) x [])
          target (last tail)
          tail (drop-last tail)]
      (if (and target
               (not (vector? x))
               (not (str/starts-with? (str head) replace-prefix)))
        (recur target (conj expanded {:head head :tail tail}))
        (conj expanded {:value x})))))

(defn- construct [sym expanded]
  (let [[value & bodies] (reverse expanded)]
    (if bodies
      (cons sym
            (reduce (fn [res {:keys [head tail]}]
                      (conj res (if (seq tail) (cons head tail) head)))
                    [(:value value)] bodies))
      (:value value))))

(defn- lambda-replace-pair [code]
  (reduce (fn [res x]
            (assoc res x (str "(" replace-prefix (subs x 2))))
          {} (re-seq #"#\([^ )]+" code)))

(defn- deref-replace-pair [code]
  (reduce (fn [res x]
            (assoc res x (if (str/starts-with? x "@(")
                           (str "(" replace-prefix (subs x 2))
                           (str replace-prefix (subs x 1)))))
          {} (re-seq #"@[^ )]+" code)))

(defn- apply-replace-pairs [code pairs]
  (reduce (fn [res [before after]] (str/replace res before after))
          code pairs))

(defn- rollback-replace-pairs [code pairs]
  (reduce (fn [res [before after]] (str/replace res after before))
          code pairs))

(defn- thread* [sym code]
  (let [replace-pairs (merge (lambda-replace-pair code)
                             (deref-replace-pair code))
        code' (apply-replace-pairs code replace-pairs)
        sexp (read-string code')
        expanded (expand-sexp sym sexp)]
    (if (> (count expanded) 2)
      (-> (construct sym expanded)
          str
          (str/replace "," "")
          (rollback-replace-pairs replace-pairs))
      code)))

(def thread-first (partial thread* '->))
(def thread-last (partial thread* '->>))

(defn
  ^{:doc "Rewrites code to use `->` threading macro."
    :requires {"code" "Code to rewrite."}
    :optional {}
    :returns {"code" "Rewritten code."
              "error" "Error message if occured."
              "status" "done"}}
  iced-refactor-thread-first [msg]
  (try
    {:code (thread-first (:code msg))}
    (catch Exception ex
      {:status #{:done :failed} :error (.getMessage ex)})))

(defn
  ^{:doc "Rewrites code to use `->>` threading macro."
    :requires {"code" "Code to rewrite."}
    :optional {}
    :returns {"code" "Rewritten code."
              "error" "Error message if occured."
              "status" "done"}}
  iced-refactor-thread-last [msg]
  (try
    {:code (thread-last (:code msg))}
    (catch Exception ex
      {:status #{:done :failed} :error (.getMessage ex)})))
