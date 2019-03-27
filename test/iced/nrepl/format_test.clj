(ns iced.nrepl.format-test
  (:require [cljfmt.core :as fmt]
            [clojure.test :as t]
            [fudje.sweet :as fj]
            [iced.nrepl.format :as sut]
            [medley.core :as medley]))

(def ^:private test-indentation-rules
  (let [reg (keyword "#\"^icedtest\"")]
    {:foo "[[:block 1]]"
     :bar/baz "[[:block 2] [:inner 1]]"
     reg "[[:inner 0]]"}))

(defn- reg= [& regexps]
  (and (every? #(instance? java.util.regex.Pattern %) regexps)
       (apply = (map str regexps))))

(t/deftest set-indentation-rules!-test
  (sut/set-indentation-rules! test-indentation-rules nil)
  (let [rules @(deref #'sut/indentation-rules)]
    (t/is (> (count rules) 3))

    (t/is
     (compatible
      rules
      (fj/contains {'foo [[:block 1]]
                    'bar/baz [[:block 2] [:inner 1]]})))

    (let [[_ v] (medley/find-first #(reg= #"^icedtest" (first %)) rules)]
      (t/is (= [[:inner 0]] v)))))

(t/deftest set-indentation-ruled!-with-overwrite-test
  (sut/set-indentation-rules! test-indentation-rules 1)
  (let [rules @(deref #'sut/indentation-rules)]
    (t/is (= 3 (count rules)))
    (t/is (map? rules))

    (t/is (= [[:block 1]] (get rules 'foo)))
    (t/is (= [[:block 2] [:inner 1]] (get rules 'bar/baz)))

    (let [[k v] (first (seq (dissoc rules 'foo 'bar/baz)))]
      (t/is (reg= #"^icedtest" k))
      (t/is (= [[:inner 0]] v)))))

(t/deftest code-test
  (t/is (= {:formatted "(hello (world))"}
           (sut/code "(hello (world  )  )" {}))))

(t/deftest code-option-test
  (sut/set-indentation-rules! test-indentation-rules nil)
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

(t/deftest calculate-indent-level-test
  (sut/set-indentation-rules!
   {:foo "[[:block 1]]"
    :bar "[[:block 2]]"
    :baz/hello "[[:block 2]]"}
   nil)

  (t/is (= {:indent-level 5}
           (sut/calcalate-indent-level "(foo 1 2\n3\n)" 1 {})))

  (t/is (= {:indent-level 2}
           (sut/calcalate-indent-level "(bar 1 2\n3\n)" 1 {})))

  (t/is (= {:indent-level 2}
           (sut/calcalate-indent-level "(baz/hello 1 2\n3\n)" 1 {})))

  (t/is (= {:indent-level 9}
           (sut/calcalate-indent-level "(b/hello 1 2\n3\n)" 1 {})))

  (t/is (= {:indent-level 2}
           (sut/calcalate-indent-level "(b/hello 1 2\n3\n)" 1 {:b "baz"}))))
