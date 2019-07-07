(ns iced.util.overview
  (:require [medley.core :as medley]))

(def default-overview-context
  {:depth 0
   :max-depth 1
   :max-list-length 20
   :max-vector-length 20
   :max-set-length 20
   :max-map-length 20
   :max-string-length 20})

(def overview-options
  {"max-depth" "Max depth to overview data."
   "max-list-length" "Max length to overview IPersistentList."
   "max-vector-length" "Max length to overview IPersistentVector."
   "max-set-length" "Max length to overview IPersistentSet."
   "max-map-length" "Max length to overview IPersistentMap."
   "max-string-length" "Max length to overview String."})

(defprotocol ICuttable ; {{{
  (cut [n x]))

(extend-protocol ICuttable
  Object
  (cut [x _] x)

  nil
  (cut [_ _] nil)

  clojure.lang.IPersistentList
  (cut [x n]
    (let [l (count x)]
      (cond-> (take n x)
        (and (not= 0 l) (> l n)) (concat '[...]))))

  clojure.lang.IPersistentVector
  (cut [x n]
    (let [l (count x)]
      (cond-> (vec (take n x))
        (and (not= 0 l) (> l n)) (conj '...))))

  clojure.lang.IPersistentMap
  (cut [x n]
    (let [l (count x)]
      (into {} (cond-> (take n x)
                 (and (not= 0 l) (> l n))
                 (concat '[[etc ...]])))))

  clojure.lang.IPersistentSet
  (cut [x n]
    (let [l (count x)]
      (cond-> (set (take n x))
        (and (not= 0 l) (> l n)) (conj '...))))

  String
  (cut [x n]
    (let [l (count x)]
      (cond-> (subs x 0 (max 0 (min l n)))
        (and (not= 0 l) (> l n)) (str "...")))))
;; }}}

(defprotocol IOverview  ; {{{
  (overview* [x context]))

(extend-protocol IOverview
  Object
  (overview* [x _] x)

  nil
  (overview* [_ _] nil)

  clojure.lang.IPersistentList
  (overview* [x {:keys [depth max-depth max-list-length] :as context}]
    (->> (cut x (if (< depth max-depth) max-list-length 1))
         (map #(and % (overview* % (update context :depth inc))))))

  clojure.lang.IPersistentVector
  (overview* [x {:keys [depth max-depth max-vector-length] :as context}]
    (->> (cut x (if (< depth max-depth) max-vector-length 1))
         (mapv #(and % (overview* % (update context :depth inc))))))

  clojure.lang.IPersistentMap
  (overview* [x {:keys [depth max-depth max-map-length] :as context}]
    (->> (cut x (if (< depth max-depth) max-map-length 1))
         (medley/map-vals #(and % (overview* % (update context :depth inc))))))

  clojure.lang.IPersistentSet
  (overview* [x {:keys [depth max-depth max-set-length] :as context}]
    (->> (cut x (if (< depth max-depth) max-set-length 1))
         (map #(and % (overview* % (update context :depth inc))))
         set))

  String
  (overview* [x {:keys [depth max-depth max-string-length]}]
    (cond-> x
      (>= depth max-depth) (cut max-string-length))))
; }}}

(defn overview
  ([x] (overview x {}))
  ([x context]
   (overview* x (merge default-overview-context context))))
