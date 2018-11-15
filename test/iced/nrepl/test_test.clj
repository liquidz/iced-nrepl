(ns iced.nrepl.test-test
  (:require [clojure.test :as t]
            [fudje.sweet :as fj]
            [iced.test-helper :as h]))

(defn dummy-public-function [] "dummy")

(t/use-fixtures :once h/repl-server-fixture)

(t/deftest test-vars-test
  (t/is
   (compatible
    (h/message {:op "iced-test-vars" :ns "iced.nrepl.test-test"})
    (fj/contains {:status #{"done"}
                  :test-vars ["test-vars-test"]})))

  (t/is
   (compatible
    (h/message {:op "iced-test-vars" :ns "non-existing-namespace"})
    (fj/contains {:status #{"done" "failed"}}))))
