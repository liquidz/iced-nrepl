(ns iced.nrepl.function-test
  (:require [clojure.test :as t]
            [fudje.sweet :as fj]
            [iced.nrepl.function :as sut]))

(t/deftest project-functions-test
  (t/is
   (compatible
    (sut/project-functions)
    (fj/contains ["iced.nrepl.function/project-functions"
                  "iced.nrepl.function-test/project-functions-test"]
                 :in-any-order)))

  (let [res (set (sut/project-functions "iced.util"))]
    (t/is (contains? res "iced.util.namespace/extract-ns-sym"))
    (t/is (not (contains? res "iced.nrepl.function/project-functions")))))
