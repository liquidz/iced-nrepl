(ns iced.nrepl.document
  (:require [clojure.data.json :as json]
            [medley.core :as medley]))

(def ^:private docs-cache (atom {}))

(defn- map-from-key [f coll]
  (reduce #(assoc %1 (f %2) %2) {} coll))

(defn- update-cache! [json-file]
  (let [docs (-> json-file slurp (json/read-str :key-fn keyword))]
    (reset! docs-cache
            (->> (:vars docs)
                 (group-by #(:ns %))
                 (medley/map-vals #(map-from-key :name %))
                 (hash-map json-file)))
    true))

(defn- convert-see-alsos [coll]
  (for [{:keys [to-var]} coll]
    (str (:ns to-var) "/" (:name to-var))))

(defn- get-doc [json-file ns-str name-str]
  (when-not (contains? @docs-cache json-file)
    (update-cache! json-file))
  (some-> (get-in @docs-cache [json-file ns-str name-str])
          (update :examples #(map :body %))
          (update :notes #(map :body %))
          (update :see-alsos convert-see-alsos)
          (update :static #(if % 1 0))))

(defn ^{:doc "FIXME"
        :requires {"json-file" "FIXME"
                   "ns" "FIXME"
                   "name" "FIXME"}
        :optional {}
        :returns {"clojuredocs" "Calculated indentation level."
                  "error" "Error message if occured."
                  "status" "done"}}
  iced-find-clojuredocs [msg]
  (try
    (if-let [doc (get-doc (:json-file msg) (:ns msg) (:name msg))]
      {:clojuredocs doc}
      {:error (format "Document for %s/%s is not found." (:ns msg) (:name msg))})
    (catch Exception ex
      {:error (.getMessage ex)})))

(defn ^{:doc "FIXME"
        :requires {"json-file" "FIXME"}
        :optional {}
        :returns {"error" "Error message if occured."
                  "status" "done"}}
  iced-update-clojuredocs [msg]
  (try
    (update-cache! (:json-file msg))
    {:status "done"}
    (catch Exception ex
      {:error (.getMessage ex)})))
