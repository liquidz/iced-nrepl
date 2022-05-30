(defproject com.github.liquidz/iced-nrepl "1.2.12-SNAPSHOT"
  :description "nREPL middleware for vim-iced"
  :url "https://github.com/liquidz/iced-nrepl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[nrepl "0.9.0"]
                 ^:inline-dep [org.clojure/data.json "2.4.0"
                               :exclusions [org.clojure/clojure]]
                 ^:inline-dep [org.clojure/test.check "1.1.1"
                               :exclusions [org.clojure/clojure]]
                 ^:inline-dep [org.clojure/tools.namespace "1.3.0" ; required by cljfmt
                               :exclusions [org.clojure/clojure
                                            org.clojure/java.classpath]]
                 ^:inline-dep [cider/orchard "0.9.2"]
                 ^:inline-dep [cljfmt "0.8.0"
                               :exclusions [org.clojure/clojure
                                            org.clojure/clojurescript
                                            org.clojure/tools.reader]]
                 ^:inline-dep [medley "1.4.0"
                               :exclusions [org.clojure/clojure]]]

  :plugins [[thomasa/mranderson "0.5.3"]
            [lein-cloverage "1.2.3"]]
  :mranderson {:project-prefix "mrandersonicednrepl"}

  :profiles
  {:dev {:source-paths ["src" "test_files"]
         :global-vars {*warn-on-reflection* true}}
   :1.9 [:dev {:dependencies [[org.clojure/clojure "1.9.0"]]}]
   :1.10 [:dev {:dependencies [[org.clojure/clojure "1.10.3"]]}]
   :1.11 [:dev {:dependencies [[org.clojure/clojure "1.11.1"]]}]

   :release {:dependencies [[org.clojure/clojure "1.11.1"]]}
   :antq {:dependencies [[com.github.liquidz/antq "RELEASE"]]}}

  :deploy-repositories [["clojars" {:sign-releases false :url "https://clojars.org/repo"}]]

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :aliases {"test-all" ["do" ["with-profile" "1.9:1.10:1.11" "test"]]})
