(ns iced.nrepl.document-test
  (:require [clojure.string :as str]
            [clojure.test :as t]
            [fudje.sweet :as fj]
            [iced.test-helper :as h]))

(t/use-fixtures :once h/repl-server-fixture)

(def ^:private test-json
  "test_files/clojuredocs-export.json")

(t/deftest clojuredocs-test
  (let [resp (h/message {:op "iced-find-clojuredocs"
                         :json-file test-json
                         :ns "clojure.core"
                         :name "butlast"})]
    (t/is (contains? (:status resp) "done"))
    (t/is
     (compatible
      (:clojuredocs resp)
      (fj/contains {:ns "clojure.core"
                    :name "butlast"
                    :see-alsos (fj/checker #(every? string? %))
                    :examples (fj/checker #(every? string? %))
                    :notes (fj/checker #(every? string? %))
                    :doc (fj/checker #(not (str/blank? %)))
                    :arglists (fj/checker #(every? string? %))})))))

(t/deftest clojuredocs-not-found-test
  (let [resp (h/message {:op "iced-find-clojuredocs"
                         :json-file test-json
                         :ns "non-existing"
                         :name "non-existing"})]
    (t/is (contains? (:status resp) "done"))
    (t/is (not (contains? resp :clojuredocs)))
    (t/is (not (str/blank? (:error resp))))))
