(ns build
  (:require
   [build-edn.core :as build-edn]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [mranderson.core :as mranderson]))

(def ^:private build-config
  {:lib 'com.github.liquidz/iced-nrepl
   :version "1.2.{{git/commit-count}}"
   :description "nREPL middleware for vim-iced"
   :documents [{:file "resources/version.txt"
                :action :create
                :text "{{version}}"}]
   :pom {:no-clojure-itself? true}
   :github-actions? true})

(def ^:private target-dir "target")
(def ^:private output-dir (str target-dir "/srcdeps"))
(def ^:private source-paths-rename-map
  {"src" output-dir})

(def ^:private repositories
  {:clojars "https://repo.clojars.org"
   :central "https://repo.maven.apache.org/maven2"})

(defn- read-deps-edn
  []
  (->> (slurp "deps.edn")
       (edn/read-string)))

(defn- source-paths
  []
  (->> (read-deps-edn)
       (:paths)))

(defn- dependencies-to-inline
  []
  (->> (get-in (read-deps-edn)
               [:aliases :injection :extra-deps])
       (map (fn [[k v]]
              (with-meta
                (cond-> [k (:mvn/version v)]
                  (:exclusions v)
                  (conj :exclusions (:exclusions v)))
                {:inline-dep true})))))

(defn- fixed-version
  []
  (-> (io/resource "version.txt")
      (slurp)))

(defn- mranderson-context
  [srcdeps-relative]
  (let [project-source-dirs (->> (io/file srcdeps-relative)
                                 (file-seq)
                                 (filter #(.isDirectory %)))
        pname (name (:lib build-config))
        pprefix (str "mranderson" (str/replace pname #"-" ""))
        version (fixed-version)]
    {:pname pname
     :pversion version
     :pprefix pprefix
     :skip-repackage-java-classes nil
     :srcdeps srcdeps-relative
     :prefix-exclusions nil
     :project-source-dirs project-source-dirs
     :unresolved-tree nil
     :overrides nil
     :expositions nil
     :watermark :mranderson/inlined}))

(defn- mranderson-paths
  [{:keys [pprefix srcdeps]}]
  {:src-path (io/file srcdeps pprefix)
   :parent-clj-dirs []
   :branch []})

(defn inline-deps
  [_]
  (let [paths (->> (source-paths)
                   (remove #{"resources"}))]
    (mranderson/copy-source-files paths
                                  target-dir))
  (let [ctx (mranderson-context output-dir)
        deps (dependencies-to-inline)
        paths (mranderson-paths ctx)]
    (mranderson/mranderson repositories deps ctx paths)))

(defn- inlined-build-config
  []
  (let [src-dirs (->> (source-paths)
                      (map #(or (source-paths-rename-map %)
                                %)))]
    (assoc build-config :source-dirs src-dirs)))

(defn pom
  [m]
  (-> (merge (inlined-build-config) m)
      (build-edn/pom)))

(defn jar
  [m]
  (-> (merge (inlined-build-config) m)
      (build-edn/jar)))

(defn install
  [m]
  (-> (merge (inlined-build-config)
             {:version (fixed-version)}
             m)
      (build-edn/install)))

(defn deploy
  [m]
  (-> (merge (inlined-build-config)
             {:version (fixed-version)}
             m)
      (build-edn/deploy)))

(defn update-documents
  [m]
  (-> (merge (inlined-build-config) m)
      (build-edn/update-documents)))
