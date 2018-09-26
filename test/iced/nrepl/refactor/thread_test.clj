(ns iced.nrepl.refactor.thread-test
  (:require [clojure.test :as t]
            [iced.nrepl.refactor.thread :as sut]))

(t/deftest thread-first-test
  (t/are [in out] (= out (sut/thread-first in))
    "(foo (bar baz))" "(-> baz bar foo)"
    "(foo (bar baz x))" "(-> baz (bar x) foo)"
    "(foo (bar baz x) y)" "(-> baz (bar x) (foo y))"
    "(foo (bar (baz)))" "(-> (baz) bar foo)"
    "(foo)" "(foo)"
    "foo" "foo"
    "(foo bar)" "(foo bar)"
    "(foo bar baz)" "(foo bar baz)"))

(t/deftest thread-first-with-map-test
  (t/is (= "(-> {:a 1 :b 2} bar foo)"
           (sut/thread-first "(foo (bar {:a 1 :b 2}))"))))

(t/deftest thread-first-failure-test
  (t/is (thrown? Exception (sut/thread-first ""))))

(t/deftest thread-last-test
  (t/are [in out] (= out (sut/thread-last in))
    "(foo (bar baz))" "(->> baz bar foo)"
    "(foo (bar x baz))" "(->> baz (bar x) foo)"
    "(foo y (bar x baz))" "(->> baz (bar x) (foo y))"
    "(foo (bar (baz)))" "(->> (baz) bar foo)"
    "(foo)" "(foo)"
    "foo" "foo"
    "(foo bar)" "(foo bar)"
    "(foo bar baz)" "(foo bar baz)"))

(t/deftest thread-last-with-map-test
  (t/is (= "(->> {:a 1 :b 2} bar foo)"
           (sut/thread-last "(foo (bar {:a 1 :b 2}))"))))

(t/deftest thread-last-failure-test
  (t/is (thrown? Exception (sut/thread-last ""))))
