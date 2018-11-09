(ns iced.nrepl.namespace-test
  (:require [clojure.test :as t]
            [fudje.sweet :as fj]
            [iced.nrepl.namespace :as sut]
            [orchard.namespace :as o.ns]))

(t/deftest aliases-test
  (t/testing "clj"
    (t/is (= {"t" "clojure.test"
              "fj" "fudje.sweet"
              "sut" "iced.nrepl.namespace"
              "o.ns" "orchard.namespace"}
             (sut/aliases "clj" "(ns iced.nrepl.namespace-test)"))))

  (t/testing "clj with no ns form"
    (t/is (= {}  (sut/aliases "clj" "(list 1 2 3)"))))

  (t/testing "clj with no ns form"
    (t/is (= {}  (sut/aliases "clj" ""))))

  (t/testing "cljs"
    (t/is (= {"bar" "foo.bar"}
             (sut/aliases "cljs" "(ns foo.core (:require [foo.bar :as bar]))"))))

  (t/testing "cljs with no ns form"
    (t/is (= {}  (sut/aliases "cljs" "(list 1 2 3)"))))

  (t/testing "cljs with no ns form"
    (t/is (= {}  (sut/aliases "cljs" "")))))

