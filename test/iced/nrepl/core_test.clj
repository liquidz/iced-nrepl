(ns iced.nrepl.core-test
  (:require
   [clojure.test :as t]
   [iced.test-helper :as h]))

(t/use-fixtures :once h/repl-server-fixture)

(t/deftest version-test
  (let [resp (h/message {:op "iced-version"})]
    (t/is (contains? (:status resp) "done"))
    (t/is (string? (:version resp)))))
