(ns iced.nrepl.spec-test
  (:require
   [clojure.spec.test.alpha :as st]
   [clojure.test :as t]
   [iced.nrepl.spec :as sut]
   [iced.test-helper :as h]))

(t/use-fixtures :once h/repl-server-fixture)

(defn- done?
  [statuses]
  (contains? statuses "done"))

(t/deftest check-test
  (h/message {:op "load-file" :file (slurp "test_files/icedtest/spec/test.clj")})

  (t/testing "success"
    (let [resp (h/message {:op "iced-spec-check" :symbol "icedtest.spec.test/success-func" :num-tests 10})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= 10 (:num-tests resp)))
      (t/is (= "OK" (:result resp)))))

  (t/testing "fail"
    (let [resp (h/message {:op "iced-spec-check" :symbol "icedtest.spec.test/fail-func" :num-tests 10})]
      (t/is (done? (:status resp)))
      (t/is (= 1 (:num-tests resp)))
      (t/is (= "NG" (:result resp)))
      (t/is (string? (:error resp)))
      (t/is (some? (:failed-input resp)))))

  (t/testing "no spec"
    (let [resp (h/message {:op "iced-spec-check" :symbol "icedtest.spec.test/no-spec-func" :num-tests 10})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= 0 (:num-tests resp)))
      (t/is (= "OK" (:result resp))))))

(t/deftest check-unexpected-result-error-test
  (with-redefs [st/check (constantly nil)]
    (t/is (nil?  (sut/check 'dummy-symbol 1))))

  (with-redefs [st/check (constantly [{:clojure.spec.test.check/ret
                                       {:result "invalid result"
                                        :num-tests 0
                                        :fail [["dummy"]]}}])]
    (t/is (= {:result "NG"
              :num-tests 0
              :failed-input [["\"dummy\""]]}
             (sut/check 'dummy-symbol 1)))))
