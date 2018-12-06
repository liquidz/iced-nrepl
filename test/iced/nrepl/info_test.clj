(ns iced.nrepl.info-test
  (:require [clojure.test :as t]
            [iced.nrepl.info :as sut]))

(t/deftest var-pattern-test
  (let [re (sut/var-pattern "foo.bar/baz?")]
    (t/are [matched? input] (= matched?  (some? (re-seq re input)))
           true "(head (foo.bar/baz?) tail)"
           true "(head (foo.bar/baz? arg) tail)"
           true "(head (foo.bar/baz?"
           true "(head foo.bar/baz? tail)"

           false "(headfoo.bar/baz? tail)"
           false "foo.bar/baz?"
           false "(head (foo.bar/baz??) tail)"
           false "(head (foo_bar/baz?) tail)"
           false "(head (foo.bar/baz) tail)")))
