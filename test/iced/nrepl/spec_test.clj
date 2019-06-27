(ns iced.nrepl.spec-test
  (:require [clojure.test :as t]
            [fudje.sweet :as fj]
            [iced.test-helper :as h]))

(t/use-fixtures :once h/repl-server-fixture)

(t/deftest check-test
  (h/message {:op "load-file" :file (slurp "test/files/spec/test.clj")})

  (t/testing "success"
    (let [resp (h/message {:op "iced-spec-check" :symbol "files.spec.test/success-func" :num-tests 10})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= 10 (:num-tests resp)))
      (t/is (= "OK" (:result resp)))))

  (t/testing "fail"
    (t/is
     (compatible
      (h/message {:op "iced-spec-check" :symbol "files.spec.test/fail-func" :num-tests 10})
      (fj/contains
       {:status (fj/checker #(contains? % "done"))
        :num-tests 1
        :result "NG"
        :message (fj/checker string?)
        :fail (fj/checker any?)}))))

  (t/testing "no spec"
    (let [resp (h/message {:op "iced-spec-check" :symbol "files.spec.test/no-spec-func" :num-tests 10})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= 0 (:num-tests resp)))
      (t/is (= "OK" (:result resp))))))
