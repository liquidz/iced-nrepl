(ns iced.nrepl.namespace-test
  (:require [clojure.test :as t]
            [fudje.sweet :as fj]
            [iced.nrepl.namespace :as sut]))

(t/deftest project-namespaces-test
  (t/is
   (compatible
    (sut/project-namespaces)
    (fj/contains '[iced.nrepl.namespace
                   iced.nrepl.namespace-test]
                 :in-any-order)))

  (let [res (set (sut/project-namespaces "iced.util"))]
    (t/is (contains? res 'iced.util.namespace))
    (t/is (not (contains? res 'iced.nrepl.namespace)))))
