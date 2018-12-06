(ns iced.nrepl.info
  (:require [clojure.string :as str]
            [iced.nrepl.namespace :as i.n.ns]
            [orchard.namespace :as o.ns]
            [refactor-nrepl.ns.ns-parser :as r.ns.parser]))

(defn var-pattern [var-name]
  (let [escaped (str/replace var-name #"([.*?])" "\\\\$1")]
    (re-pattern (str "[( ]" escaped "([) ]|$)"))))

(defn- extract-requiring-sym [path ns-sym]
  (let [ns-data (r.ns.parser/parse-ns path)]
    (when-let [x (some #(and (= ns-sym (:ns %)) %)
                       ;; NOTE: only support clj currently
                       (get-in ns-data [:clj :require]))]
      (or (:as x) (:ns x)))))

(defn- find-references-by-path [path var-name]
  (let [re (var-pattern var-name)]
    (->> (slurp path)
         str/split-lines
         (map-indexed
          (fn [lnum line]
            (when (re-seq re (str/trim line))
              {:filename path
               :lnum (inc lnum)
               :text (str/trim line)})))
         (remove nil?))))

(defn find-var-references [ns-name var-name]
  (let [ns-sym (symbol ns-name)]
    (->> (i.n.ns/project-ns-list)
         (map o.ns/ns-path)
         (remove nil?)
         (map #(vector % (extract-requiring-sym % ns-sym)))
         (filter second)
         (mapcat (fn [[path ns-alias-sym]]
                   (find-references-by-path path (str ns-alias-sym "/" var-name)))))))
