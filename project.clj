(defproject iced-nrepl "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-http "3.9.1"]
                 [jonase/eastwood "0.2.9" :exclusions [org.clojure/clojure]]
                 [nrepl "0.4.5"]
                 [cider/orchard "0.3.0"]
                 [cljfmt "0.6.0"]
                 ;;[cider/piggieback "0.3.9"]
                 [refactor-nrepl "2.4.0"]
                 [cider/cider-nrepl "0.18.0"]]
  :profiles
  {:dev
   {:dependencies [[fudje "0.9.7"]]}})
