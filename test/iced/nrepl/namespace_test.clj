(ns iced.nrepl.namespace-test
  (:require [clojure.test :as t]
            [fudje.sweet :as fj]
            [iced.nrepl.namespace :as sut]
            [orchard.namespace :as o.ns]))

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

(t/deftest related-namespaces-test
  (with-redefs [o.ns/project-namespaces
                (constantly '(foo.bar.baz
                              foo.xxx.baz
                              foo.xxx.baz.yyy
                              bar.baz
                              bar.foo.baz
                              foo.bar.baz-test
                              foo.yyy.baz-test.yyy))]
    (t/is
     (compatible
      (sut/related-namespaces "foo.bar.baz")
      (fj/just ["foo.xxx.baz"
                "foo.xxx.baz.yyy"
                "foo.bar.baz-test"] :in-any-order)))

    (t/is
     (compatible
      (sut/related-namespaces "foo.bar.baz-test")
      (fj/just ["foo.bar.baz"
                "foo.xxx.baz"
                "foo.xxx.baz.yyy"] :in-any-order)))))
