(ns iced.nrepl.format-test
  (:require [cljfmt.core :as fmt]
            [clojure.test :as t]
            [fudje.sweet :as fj]
            [iced.nrepl.format :as sut]))

(t/deftest code-test
  (t/is (= {:formatted "(hello (world))"}
           (sut/code "(hello (world  )  )" {})))

  (with-redefs [fmt/reformat-string (fn [_ opt] opt)]
    (let [indents (:formatted (sut/code "" {:hello "[[:block 1]]"}))]
      (t/is (= [[:block 1]] (get-in indents [:indents 'hello]))))))

(t/deftest code-error-test
  (t/is (compatible
         (sut/code "(hello (world)" {})
         (fj/just {:error (fj/checker string?)
                   :line (fj/checker pos?)
                   :column (fj/checker pos?)}))))
