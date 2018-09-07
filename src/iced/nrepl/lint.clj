(ns iced.nrepl.lint
  (:require [clojure.set :as set]
            [eastwood.lint :as el]))

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

