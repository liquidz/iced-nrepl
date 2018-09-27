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

(t/deftest aliases-test
  (t/testing "clj"
    (t/is (= {"t" "clojure.test"
              "fj" "fudje.sweet"
              "sut" "iced.nrepl.namespace"}
             (sut/aliases "clj" "(ns iced.nrepl.namespace-test)"))))

  (t/testing "cljs"
    (t/is (= {"bar" "foo.bar"}
             (sut/aliases "cljs" "(ns foo.core (:require [foo.bar :as bar]))")))))
