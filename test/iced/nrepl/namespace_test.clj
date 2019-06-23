(ns iced.nrepl.namespace-test
  (:require [clojure.test :as t]
            [iced.test-helper :as h]))

(t/use-fixtures :once h/repl-server-fixture)

(t/deftest project-ns-list-test
  (t/testing "iced-project-ns-list"
    (let [resp (h/message {:op "iced-project-ns-list"})]
      (t/is (contains? (:status resp) "done"))
      (t/is (sequential? (:project-ns-list resp))))))
