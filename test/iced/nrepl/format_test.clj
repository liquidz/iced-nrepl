(ns iced.nrepl.format-test
  (:require [cljfmt.core :as fmt]
            [clojure.test :as t]
            [fudje.sweet :as fj]
            [iced.nrepl.format :as sut]))

(def ^:private test-indentation-rules
  {:foo "[[:block 1]]"
   :bar/baz "[[:block 2] [:inner 1]]"})

(t/deftest set-indentation-rules!-test
  (sut/set-indentation-rules! test-indentation-rules)

  (t/is
   (compatible
    @(deref #'sut/indentation-rules)
    (fj/contains {'foo [[:block 1]]
                  'bar/baz [[:block 2] [:inner 1]] }))))

(t/deftest code-test
  (t/is (= {:formatted "(hello (world))"}
           (sut/code "(hello (world  )  )" {}))))

(t/deftest code-option-test
  (sut/set-indentation-rules! test-indentation-rules)
  (with-redefs [fmt/reformat-string (fn [_ opt] opt)]
    (let [alias-map {:str "clojure.string"}]
      (t/is
       (compatible
        (:formatted (sut/code "" alias-map))
        (fj/contains {:indents (fj/contains {'foo [[:block 1]]})
                      :alias-map {"str" "clojure.string"}}))))))

(t/deftest code-error-test
  (t/is (compatible
         (sut/code "(hello (world)" {})
         (fj/just {:error (fj/checker string?)
                   :line (fj/checker pos?)
                   :column (fj/checker pos?)}))))
