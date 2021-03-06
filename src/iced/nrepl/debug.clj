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
  (and datafy' (swap! tapped conj {:unique-id (str (java.util.UUID/randomUUID))
                                   :content (datafy' x)})))

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

    (and (string? k) (#{"true" "false" "nil"} k))
    (case k
      "true" true
      "false" false
      nil)

    (string? k)
    (str/replace k #"(^\"|\"$)" "")

    :else k))

(defn- convert-keys
  [[first-key & rest-keys]]
  ;; First key must be index(Integer) or unique-id(String)
  (let [index (if (integer? first-key)
                first-key
                (reduce (fn [idx item]
                          (if (= first-key (:unique-id item))
                            (reduced idx)
                            (inc idx)))
                        0 @tapped))]
    (concat [index :content] (map convert-key rest-keys))))

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

(defn- key->str
  [x]
  (cond
    (string? x) (str \" x \")
    (boolean? x) (str x)
    (nil? x) "nil"
    :else (str x)))

(defn- extract-candidates
  [x]
  (condp #(instance? %1 %2) x
    clojure.lang.IPersistentMap (map key->str (keys x))
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
                  "error" "If occurred."
                  "status" "done"}}
  iced-list-tapped
  [msg]
  (if-not supported?
    {:error tap-not-supported-msg}
    (let [option (extract-overview-option msg)]
      {:tapped (map (fn [{:keys [unique-id content]}]
                      {:unique-id unique-id
                       :value (str (i.u.overview/overview content option))})
                    @tapped)})))

(defn ^{:doc "Browses tapped values and returns the value."
        :requires {"keys" "Keys to browse tapped values."}
        :optional i.u.overview/overview-options
        :returns {"value" "The browsed value."
                  "error" "If occurred."
                  "status" "done"}}
  iced-browse-tapped
  [msg]
  (if-not supported?
    {:error tap-not-supported-msg}
    (let [ks (->> (get msg :keys [])
                  convert-keys)
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
                  "error" "If occurred."
                  "status" "done"}}
  iced-fetch-tapped-children
  [msg]
  (if-not supported?
    {:error tap-not-supported-msg}
    (let [ks (->> (get msg :keys [])
                  convert-keys)]
      (try
        {:children (-> @tapped (get-in* ks) extract-children)}
        (catch Exception ex
          {:error (.getMessage ex)})))))

(defn ^{:doc "Completes a key for browsing tapped values."
        :requires {"keys" "Keys to browse tapped values."}
        :optional {}
        :returns {"complete" "Completion results."
                  "error" "If occurred."
                  "status" "done"}}
  iced-complete-tapped
  [msg]
  (if-not supported?
    {:error tap-not-supported-msg}
    (let [ks (->> (get msg :keys [])
                  convert-keys)]
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
                  "error" "If occurred."
                  "status" "done"}}
  iced-clear-tapped
  [_]
  (if-not supported?
    {:error tap-not-supported-msg}
    (do (reset! tapped [])
        {:result "OK"})))

(defn ^{:doc "Delete the specified tapped value."
        :requires {"key" "The key you'd like to delete"}
        :optional {}
        :returns {"result" "OK"
                  "error" "If occurred."
                  "status" "done"}}
  iced-delete-tapped
  [{target-key :key}]
  (if-not supported?
    {:error tap-not-supported-msg}
    (do (reset! tapped
                (if (integer? target-key)
                  (vec (concat (take target-key @tapped)
                               (drop (inc target-key) @tapped)))
                  (vec (remove #(= target-key (:unique-id %)) @tapped))))
        {:result "OK"})))
