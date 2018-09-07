(defproject iced-nrepl "0.1.0-SNAPSHOT"
  :description "nREPL middleware for vim-iced"
  :url "https://github.com/liquidz/iced-nrepl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [cider/orchard "0.3.0"]
                 [clj-http "3.9.1"]
                 [cljfmt "0.6.0"]
                 [jonase/eastwood "0.2.9" :exclusions [org.clojure/clojure]]
                 [nrepl "0.4.5"]]
  :profiles
  {:dev
   {:dependencies [[cider/cider-nrepl "0.18.0"]
                   [cider/piggieback "0.3.8"]
                   [fudje "0.9.7"]
                   [leiningen-core "2.7.1"]
                   [refactor-nrepl "2.4.0"]]}})
