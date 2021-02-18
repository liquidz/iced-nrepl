(ns iced.nrepl.namespace
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.set :as set]
   [clojure.string :as str]
   [orchard.namespace :as o.ns]))

(defn ^{:doc "Returns all namespaces in the project."
        :requires {}
        :optional {}
        :returns {"project-ns-list" "Namespace names."
                  "status" "done"}}
  iced-project-ns-list
  [_msg]
  {:project-ns-list (o.ns/project-namespaces)})

(defn- get-props
  []
  {:user-dir (System/getProperty "user.dir")
   :file-sep (System/getProperty "file.separator")})

(defn- ns-name->path
  [ns-name]
  (-> ns-name
      (str/replace "." (:file-sep (get-props)))
      (str/replace "-" "_")))

(defn- ns-drop-last
  [ns-name]
  (when-let [i (str/last-index-of ns-name ".")]
    (subs ns-name 0 i)))

(defn- find-near-ns-name
  [base-ns-name ns-name]
  (when-let [ns-name' (ns-drop-last ns-name)]
    (->> (iterate ns-drop-last base-ns-name)
         (drop 1)
         (take-while some?)
         (map vector (range))
         (some (fn [[priority base-ns-name']]
                 (when (str/starts-with? ns-name' base-ns-name')
                   [priority ns-name]))))))

(defn- find-near-ns-names
  [base-ns-name]
  (let [filter-test-ns-fn (if (str/ends-with? base-ns-name "-test")
                            filter remove)
        project-ns-names (map str (o.ns/project-namespaces))]
    (->> project-ns-names
         (filter-test-ns-fn #(str/ends-with? % "-test"))
         (keep #(find-near-ns-name base-ns-name %))
         (sort-by first)
         (map second))))

(defn- ns-path
  [ns-sym]
  (when-let [^java.net.URL src (o.ns/canonical-source ns-sym)]
    (.getFile src)))

(defn- pseudo-ns-path
  [ns-name]
  (let [{:keys [user-dir file-sep]} (get-props)
        near-ns-name (first (find-near-ns-names ns-name))
        near-ns-path (some-> near-ns-name symbol ns-path)
        near-ns-ext (or (some->> near-ns-path (re-seq #"clj[sc]?$") first) "clj")]
    (when-let [i (and near-ns-path
                      (str/starts-with? near-ns-path user-dir)
                      (str/index-of near-ns-path (ns-name->path near-ns-name)))]
      (let [subdir (subs near-ns-path (inc (count user-dir)) (dec i))]
        (str (str/join file-sep (conj [user-dir subdir] (ns-name->path ns-name)))
             "." near-ns-ext)))))

(defn- default-ns-path
  [ns-name]
  (let [{:keys [user-dir file-sep]} (get-props)
        subdir (if (str/ends-with? ns-name "-test") "test" "src")]
    (str user-dir file-sep subdir file-sep (ns-name->path ns-name) ".clj")))

(defn ^{:doc "Returns pseudo namespace path."
        :requires {"ns" "Namespace name(String)."}
        :optional {}
        :returns {"status" "done"
                  "path" "Pseudo namespace path"}}
  iced-pseudo-ns-path
  [msg]
  (let [{ns-name :ns} msg]
    {:path (or (ns-path (symbol ns-name))
               (pseudo-ns-path ns-name)
               (default-ns-path ns-name))}))

(def ^:private java-classes*
  (-> "java_classes.edn"
      (io/resource)
      (slurp)
      (edn/read-string)))

(defn- convert-class-map
  [class-map]
  (reduce-kv
   (fn [accm k v]
     (assoc accm (keyword k) (set (map symbol v))))
   {}
   class-map))

(defn- java-class
  [additional-class-map]
  (if additional-class-map
    (merge-with set/union
                java-classes*
                (convert-class-map additional-class-map))
    java-classes*))

(defn- candidates
  [class-name classes]
  (let [sym (symbol class-name)]
    (reduce-kv
     (fn [res pkg class-set]
       (if (contains? class-set sym)
         (conj res (str (name pkg) "." sym))
         res))
     [] classes)))

(defn ^{:doc "Returns java class candidates."
        :requires {"symbol" "Symbol to find candidates."}
        :optional {"class-map" "Optional map of package name to class name."}
        :returns {"candidates" "Java class candidates."
                  "error" "Error message if occurred"
                  "status" "done"}}
  iced-java-class-candidates
  [msg]
  (try
    (let [classes (java-class (:class-map msg))]
      (if-let [result (some-> (:symbol msg)
                              (str/split #"/" 2)
                              (first)
                              (candidates classes))]
        {:candidates result}
        {:status #{:done :failed} :error "Not found"}))
    (catch Exception ex
      {:status #{:done :failed} :error (.getMessage ex)})))
