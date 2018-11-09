(ns iced.nrepl.refactor.thread-test
  (:require [clojure.test :as t]
            [iced.nrepl.refactor.thread :as sut]))

(t/deftest thread-first-test
  (t/are [in out] (= out (sut/thread-first in))
    "(foo (bar baz))"     "(-> baz bar foo)"
    "(foo (bar baz x))"   "(-> baz (bar x) foo)"
    "(foo (bar baz x) y)" "(-> baz (bar x) (foo y))"
    "(foo (bar (baz)))"   "(-> (baz) bar foo)"
    "(foo)"               "(foo)"
    "foo"                 "foo"
    "(foo bar)"           "(foo bar)"
    "(foo bar baz)"       "(foo bar baz)"))

(t/deftest thread-first-with-map-test
  (t/is (= "(-> {:a 1 :b 2} bar foo)"
           (sut/thread-first "(foo (bar {:a 1 :b 2}))"))))

(t/deftest thread-first-with-vector-test
  (t/is (= "(-> [1 2 3] (bar baz) foo)"
           (sut/thread-first "(foo (bar [1 2 3] baz))"))))

(t/deftest thread-first-with-macro-test
  (t/are [in out] (= out (sut/thread-first in))
    "(foo (bar 123 #(baz %)))"      "(-> 123 (bar #(baz %)) foo)"
    "(foo (bar 1 #(bar 2 %)))"      "(-> 1 (bar #(bar 2 %)) foo)"
    "(foo (bar #(baz %) 123))"      "(-> #(baz %) (bar 123) foo)"
    "(foo (bar 1 #(b %)) #(bar %))" "(-> 1 (bar #(b %)) (foo #(bar %)))"
    "(foo (bar 1 #(bar %)) #(b %))" "(-> 1 (bar #(bar %)) (foo #(b %)))"))

(t/deftest thread-first-with-deref-test
  (t/are [in out] (= out (sut/thread-first in))
    "(foo (bar @baz))"   "(-> @baz bar foo)"
    "(foo (bar @(baz)))" "(-> @(baz) bar foo)"))

(t/deftest thread-first-failure-test
  (t/is (thrown? Exception (sut/thread-first ""))))

(t/deftest thread-last-test
  (t/are [in out] (= out (sut/thread-last in))
    "(foo (bar baz))"     "(->> baz bar foo)"
    "(foo (bar x baz))"   "(->> baz (bar x) foo)"
    "(foo y (bar x baz))" "(->> baz (bar x) (foo y))"
    "(foo (bar (baz)))"   "(->> (baz) bar foo)"
    "(foo)"               "(foo)"
    "foo"                 "foo"
    "(foo bar)"           "(foo bar)"
    "(foo bar baz)"       "(foo bar baz)"))

(t/deftest thread-last-with-map-test
  (t/is (= "(->> {:a 1 :b 2} bar foo)"
           (sut/thread-last "(foo (bar {:a 1 :b 2}))"))))

(t/deftest thread-last-with-vector-test
  (t/is (= "(->> [1 2 3] (bar baz) foo)"
           (sut/thread-last "(foo (bar baz [1 2 3]))"))))

(t/deftest thread-last-with-macro-test
  (t/are [in out] (= out (sut/thread-last in))
    "(foo (bar #(baz %) 123))" "(->> 123 (bar #(baz %)) foo)"
    "(foo (bar #(bar %) 123))" "(->> 123 (bar #(bar %)) foo)"
    "(foo (bar 123 #(baz %)))" "(->> #(baz %) (bar 123) foo)"
    "(foo (bar #(+ 1 %) 23))"  "(->> 23 (bar #(+ 1 %)) foo)"
    "(foo (bar #(bar %) 123))" "(->> 123 (bar #(bar %)) foo)"))

(t/deftest thread-last-with-deref-test
  (t/are [in out] (= out (sut/thread-last in))
    "(foo (bar @baz))"   "(->> @baz bar foo)"
    "(foo (bar @(baz)))" "(->> @(baz) bar foo)"))

(t/deftest thread-last-failure-test
  (t/is (thrown? Exception (sut/thread-last ""))))
