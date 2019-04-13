(defproject iced-nrepl "0.4.4-SNAPSHOT"
  :description "nREPL middleware for vim-iced"
  :url "https://github.com/liquidz/iced-nrepl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[nrepl "0.6.0"]
                 [refactor-nrepl "2.4.0"]

                 ^:inline-dep [org.clojure/data.json "0.2.6"
                               :exclusions [org.clojure/clojure]]
                 ^:inline-dep [org.clojure/test.check "0.10.0-alpha3"
                               :exclusions [org.clojure/clojure]]
                 ^:inline-dep [org.clojure/tools.namespace "0.3.0-alpha4"
                               :exclusions [org.clojure/java.classpath]]
                 ^:inline-dep [cider/orchard "0.4.0"]
                 ^:inline-dep [cljfmt "0.6.4"
                               :exclusions [org.clojure/clojure
                                            org.clojure/clojurescript
                                            org.clojure/tools.reader]]
                 ^:inline-dep [http-kit "2.4.0-alpha4"]
                 ^:inline-dep [jonase/eastwood "0.3.5"
                               :exclusions [org.clojure/clojure]]
                 ^:inline-dep [medley "1.1.0"
                               :exclusions [org.clojure/clojure]]]

  :plugins [[thomasa/mranderson "0.5.1"]]
  :mranderson {:project-prefix "mrandersonicednrepl"}

  :profiles
  {:dev {:dependencies [[fudje "0.9.7"]]}
   :1.9 [:dev {:dependencies [[org.clojure/clojure "1.9.0"]]}]
   :1.10 [:dev {:dependencies [[org.clojure/clojure "1.10.0"]]}]}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :aliases {"test-all" ["do" ["with-profile" "1.9:1.10" "test"]]}
  )
