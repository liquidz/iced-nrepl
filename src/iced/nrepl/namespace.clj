(ns iced.nrepl.namespace
  (:require [clojure.string :as str]
            [iced.util.namespace :as i.u.ns]
            [medley.core :as medley]
            [orchard.namespace :as o.ns]
            [refactor-nrepl.ns.ns-parser :as r.ns.parser]))

(defn- ensure-string-map [m]
  (medley/map-kv (fn [k v] [(str k) (str v)]) m))

(defmulti aliases (fn [env _] env))
(defmethod aliases "clj"
  [_ ns-code]
  (or (some->> ns-code i.u.ns/extract-ns-sym ns-aliases
               (medley/map-vals ns-name)
               ensure-string-map)
      {}))

(defmethod aliases "cljs"
  [_ ns-code]
  (or (when (seq ns-code)
        (some-> ns-code read-string r.ns.parser/get-libspecs
                r.ns.parser/aliases
                ensure-string-map))
      {}))
