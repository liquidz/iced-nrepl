(ns iced.nrepl.refactor.thread
  (:require [clojure.string :as str]))

(defmulti expand-sexp (fn [sym _] sym))

(defmethod expand-sexp '->
  [_ x]
  (loop [x x, expanded []]
    (let [[head target & tail] (if (sequential? x) x [])]
      (if target
        (recur target (conj expanded {:head head :tail tail}))
        (conj expanded {:value x})))))

(defmethod expand-sexp '->>
  [_ x]
  (loop [x x, expanded []]
    (let [[head & tail] (if (sequential? x) x [])
          target (last tail)
          tail (drop-last tail)]
      (if target
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

(defn- thread* [sym code]
  (let [sexp (read-string code)
        expanded (expand-sexp sym sexp)]
    (if (> (count expanded) 2)
      (-> (construct sym expanded)
          str
          (str/replace "," ""))
      code)))

(def thread-first (partial thread* '->))
(def thread-last (partial thread* '->>))
