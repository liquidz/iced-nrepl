(ns iced.nrepl.format-test
  (:require [cljfmt.core :as fmt]
            [clojure.test :as t]
            [iced.nrepl.format :as sut]))

(t/deftest code-test
  (t/is (= "(hello (world))"
           (sut/code "(hello (world  )  )" {})))

  (with-redefs [fmt/reformat-string (fn [_ opt] opt)]
    (let [indents (sut/code "" {:hello "[[:block 1]]"})]
      (t/is (= [[:block 1]] (get-in indents [:indents 'hello]))))))
