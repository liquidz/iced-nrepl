(ns iced.nrepl.function
  (:require [iced.nrepl.namespace :as i.n.ns]
            [medley.core :as medley]
            [orchard.namespace :as o.ns]))

(defn- path-ns-dict [ns-list]
  (reduce (fn [res sym]
            (assoc res (o.ns/ns-path sym) sym))
    {} ns-list))

(defn- var->map [file v]
  (-> (meta v)
      (select-keys [:line :column :name])
      (update :name str)
      (assoc :file file)))

(defn- get-interns [sym file]
  (->> (try (ns-interns sym) (catch Exception _ []))
       (map (comp (partial var->map file) second))))

(defn- get-publics [sym file]
  (->> (try (ns-publics sym) (catch Exception _ []))
       (map (comp (partial var->map file) second))
       (map #(update % :name (fn [s] (str sym "/" s))))))

(defn project-functions [& [current-file]]
  (let [dict (-> (i.n.ns/project-namespaces)
                 path-ns-dict)
        current-ns (get dict current-file)
        other-ns-dict (medley/filter-vals #(not= % current-file) dict)]
    (concat (some-> current-ns (get-interns current-file))
            (mapcat (fn [[file sym]] (get-publics sym file))
                    other-ns-dict))))
