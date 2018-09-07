(ns iced-nrepl.plugin
  (:require [iced.nrepl.core :as core]))

(defn middleware [project]
  (-> project
      (update-in [:dependencies]
                 (fnil into [])
                 [['cider/cider-nrepl "0.18.0"]
                  ['refactor-nrepl "2.4.0"]
                  ['iced-nrepl (core/version)]])
      (update-in [:repl-options :nrepl-middleware]
                 (fnil into [])
                 '[cider.nrepl/wrap-complete
                   cider.nrepl/wrap-debug
                   cider.nrepl/wrap-info
                   cider.nrepl/wrap-macroexpand
                   cider.nrepl/wrap-ns
                   cider.nrepl/wrap-out
                   cider.nrepl/wrap-pprint
                   cider.nrepl/wrap-pprint-fn
                   cider.nrepl/wrap-spec
                   cider.nrepl/wrap-test
                   cider.nrepl/wrap-trace
                   cider.nrepl/wrap-undef
                   refactor-nrepl.middleware/wrap-refactor
                   iced.nrepl/wrap-iced ])))

