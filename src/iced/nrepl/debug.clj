(ns iced.nrepl.debug
  (:require
   [clojure.pprint :as pprint]
   [clojure.string :as str]
   [iced.util.overview :as i.u.overview]))

(try (require 'clojure.core.protocols)
     (catch Exception _ nil))

(def ^:private tapped (atom []))

(def ^:private datafy' (resolve 'clojure.core.protocols/datafy))
(def ^:private add-tap' (resolve 'clojure.core/add-tap))
(def ^:private remove-tap' (resolve 'clojure.core/remove-tap))
(def ^:private tap-not-supported-msg
  "Tap is not supported in the Clojure version you are using. Please use Clojure 1.10.0 or later.")

(def supported?
  (every? some? [datafy' add-tap' remove-tap']))

(defn- catch-tapped!
  [x]
  (and datafy' (swap! tapped #(cons (datafy' x) %))))

(when supported?
  (remove-tap' #'catch-tapped!)
  (add-tap' #'catch-tapped!))

(defn- get-in*
  [coll ks]
  (reduce (fn [acc k]
            ((if (integer? k) nth get) acc k)) coll ks))

(defn- convert-key
  [k]
  (cond
    (and (string? k) (str/starts-with? k ":"))
    (keyword (subs k 1))

    (and (string? k) (str/starts-with? k "'"))
    (symbol (subs k 1))

    :else k))

(defn parse-option-string
  [s]
  (let [[k v] (str/split s #"=" 2)
        value (Long/parseLong v)]
    (case k
      ("max-depth" "md") [:max-depth value]
      ("max-string-length" "msl") [:max-string-length value]
      ("max-list-length" "mll") [:max-list-length value]
      ("max-vector-length" "mvl") [:max-vector-length value]
      ("max-set-length" "mSl") [:max-set-length value]
      ("max-map-length" "mml") [:max-map-length value]
      nil)))

(defn- browse-in
  ([coll ks] (browse-in coll ks {}))
  ([coll ks option]
   (let [[ks [_ & opts]] (split-with #(not= % "/") ks)
         option (->> opts
                     (map parse-option-string)
                     (into {})
                     (merge option))]
     (i.u.overview/overview (get-in* coll ks) option))))

(defn- extract-candidates
  [x]
  (condp #(instance? %1 %2) x
    clojure.lang.IPersistentMap (map str (keys x))
    clojure.lang.IPersistentList (range (count x))
    clojure.lang.IPersistentVector (range (count x))
    []))

(defn- extract-overview-option
  [msg]
  (select-keys msg (keys i.u.overview/default-overview-context)))

(defn ^{:doc "Returns tapped values."
        :requires {}
        :optional i.u.overview/overview-options
        :returns {"tapped" "Tapped values converted to String."
                  "error" "If occured."
                  "status" "done"}}
  iced-list-tapped
  [msg]
  (if-not supported?
    {:error tap-not-supported-msg}
    (let [option (extract-overview-option msg)]
      {:tapped (map #(str (i.u.overview/overview % option)) @tapped)})))

(defn ^{:doc "Browses tapped values and returns the value."
        :requires {"keys" "Keys to browse tapped values."}
        :optional i.u.overview/overview-options
        :returns {"value" "The browsed value."
                  "error" "If occured."
                  "status" "done"}}
  iced-browse-tapped
  [msg]
  (if-not supported?
    {:error tap-not-supported-msg}
    (let [ks (->> (get msg :keys [])
                  (map convert-key))
          options (extract-overview-option msg)]
      (try
        {:value (with-out-str
                  (pprint/pprint (browse-in @tapped ks options)))}
        (catch Exception ex
          {:error (.getMessage ex)})))))

(defn- extract-children
  [x]
  (letfn [(wrap-child
            [name has-children?]
            {:name (str name) :has-children? (str has-children?)})]
    (condp #(instance? %1 %2) x
      clojure.lang.IPersistentMap (map #(wrap-child % true) (keys x))
      clojure.lang.IPersistentList (->> x count range (map #(wrap-child % true)))
      clojure.lang.IPersistentVector (->> x count range (map #(wrap-child % true)))
      clojure.lang.LazySeq (->> x count range (map #(wrap-child % true)))
      (if (nil? x)
        []
        [(wrap-child x false)]))))

(defn ^{:doc "Fetches tapped values and returns its child elements."
        :requires {"keys" "Keys to fetch tapped values."}
        :optional {}
        :returns {"children" "The fetched value."
                  "error" "If occured."
                  "status" "done"}}
  iced-fetch-tapped-children
  [msg]
  (if-not supported?
    {:error tap-not-supported-msg}
    (let [ks (->> (get msg :keys [])
                  (map convert-key))]
      (try
        {:children (-> @tapped (get-in* ks) extract-children)}
        (catch Exception ex
          {:error (.getMessage ex)})))))

(defn ^{:doc "Completes a key for browsing tapped values."
        :requires {"keys" "Keys to browse tapped values."}
        :optional {}
        :returns {"complete" "Completion results."
                  "error" "If occured."
                  "status" "done"}}
  iced-complete-tapped
  [msg]
  (if-not supported?
    {:error tap-not-supported-msg}
    (let [ks (->> (get msg :keys [])
                  (map convert-key))]
      (try
        {:complete (-> @tapped
                       (get-in* ks)
                       extract-candidates)}
        (catch Exception ex
          {:error (.getMessage ex)})))))

(defn ^{:doc "Clear tapped values."
        :requires {}
        :optional {}
        :returns {"result" "OK"
                  "error" "If occured."
                  "status" "done"}}
  iced-clear-tapped
  [_]
  (if-not supported?
    {:error tap-not-supported-msg}
    (do (reset! tapped [])
        {:result "OK"})))
; vim:fdm=marker:fdl=0
