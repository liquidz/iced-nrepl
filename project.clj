(defproject iced-nrepl "0.3.1-SNAPSHOT"
  :description "nREPL middleware for vim-iced"
  :url "https://github.com/liquidz/iced-nrepl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[nrepl "0.5.3"]
                 [refactor-nrepl "2.4.0"]

                 ^:source-dep [org.clojure/data.json "0.2.6"]
                 ^:source-dep [org.clojure/test.check "0.10.0-alpha3"]
                 ^:source-dep [org.clojure/tools.namespace "0.3.0-alpha4"]
                 ^:source-dep [cider/orchard "0.3.3"]
                 ^:source-dep [cljfmt "0.6.3" :exclusions [org.clojure/clojurescript
                                                           org.clojure/tools.reader]]
                 ^:source-dep [http-kit "2.3.0"]
                 ^:source-dep [jonase/eastwood "0.3.4"
                               :exclusions [org.clojure/clojure]]
                 ^:source-dep [medley "1.0.0"]]

  :plugins [[thomasa/mranderson "0.4.9"]]

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
