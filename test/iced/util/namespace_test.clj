(ns iced.util.namespace-test
  (:require [clojure.test :as t]
            [iced.util.namespace :as sut]))

(t/deftest extract-ns-sym-test
  (t/are [x y] (= x (sut/extract-ns-sym y))
    'foo.bar "(ns foo.bar)"
    'foo.bar "(ns  foo.bar)"
    'foo.bar "(ns foo.bar (:require bar.baz))"
    'foo.bar "(ns foo.bar\n(:require bar.baz))"
    'foo.bar "(ns\nfoo.bar\n(:require bar.baz))"
    'foo.bar "(ns\n  foo.bar\n(:require bar.baz))"
    'foo.bar-baz "(ns foo.bar-baz)"
    'foo2bar.baz "(ns foo2bar.baz)"
    nil ""
    nil "(def foo 1)"))
