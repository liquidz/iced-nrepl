(defproject iced-nrepl "0.1.10-SNAPSHOT"
  :description "nREPL middleware for vim-iced"
  :url "https://github.com/liquidz/iced-nrepl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [cider/orchard "0.3.1"]
                 [clj-http "3.9.1"]
                 [cljfmt "0.6.1" :exclusions [org.clojure/tools.reader
                                              org.clojure/clojurescript]]
                 [jonase/eastwood "0.3.1" :exclusions [org.clojure/clojure]]
                 [medley "1.0.0"]
                 [nrepl "0.4.5"]
                 [refactor-nrepl "2.4.0"]]
  :profiles
  {:dev
   {:dependencies [[fudje "0.9.7"]]}}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
