(defproject iced-nrepl "0.7.1-SNAPSHOT"
  :description "nREPL middleware for vim-iced"
  :url "https://github.com/liquidz/iced-nrepl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[nrepl "0.6.0"]
                 ^:inline-dep [org.clojure/data.json "0.2.6"
                               :exclusions [org.clojure/clojure]]
                 ^:inline-dep [org.clojure/test.check "0.10.0"
                               :exclusions [org.clojure/clojure]]
                 ^:inline-dep [org.clojure/tools.namespace "0.3.1" ; required by cljfmt
                               :exclusions [org.clojure/java.classpath]]
                 ^:inline-dep [cider/orchard "0.5.4"]
                 ^:inline-dep [cljfmt "0.6.4"
                               :exclusions [org.clojure/clojure
                                            org.clojure/clojurescript
                                            org.clojure/tools.reader]]
                 ^:inline-dep [medley "1.2.0"
                               :exclusions [org.clojure/clojure]]]

  :plugins [[thomasa/mranderson "0.5.1"]
            [lein-cloverage "1.1.1"]]
  :mranderson {:project-prefix "mrandersonicednrepl"}

  :profiles
  {:dev {:dependencies [[fudje "0.9.7"]]
         :source-paths ["src" "test_files"]
         :global-vars {*warn-on-reflection* true}}
   :1.9 [:dev {:dependencies [[org.clojure/clojure "1.9.0"]]}]
   :1.10 [:dev {:dependencies [[org.clojure/clojure "1.10.0"]]}]
   :1.10.1 [:dev {:dependencies [[org.clojure/clojure "1.10.1"]]}]

   :release {:dependencies [[org.clojure/clojure "1.10.1"]]}}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :aliases {"test-all" ["do" ["with-profile" "1.9:1.10:1.10.1" "test"]]}
  )
