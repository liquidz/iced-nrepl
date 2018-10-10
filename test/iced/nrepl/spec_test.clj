(ns iced.nrepl.spec-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :as t]
            [fudje.sweet :as fj]
            [iced.nrepl.spec :as sut]))

(defn success-func [_] true)
(s/fdef success-func
  :args (s/cat :_ any?)
  :ret boolean?)

(defn fail-func [_] true)
(s/fdef fail-func
        :args (s/cat :_ any?)
        :ret string?)

(defn no-spec-func [] true)

(t/deftest check-test
  (t/is (= {:result "OK" :num-tests 10}
           (sut/check `success-func 10)))

  (t/is
   (compatible
    (sut/check `fail-func 10)
    (fj/just {:result "NG"
              :num-tests (fj/checker int?)
              :message (fj/checker string?)
              :fail (fj/checker any?)}))))

(t/deftest check-with-no-spec-function-test
  (t/is (= {:result "OK" :num-tests 0}
           (sut/check `no-spec-func 10))))
