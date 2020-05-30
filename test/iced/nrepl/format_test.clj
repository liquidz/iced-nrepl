(ns iced.nrepl.format-test
  (:require
   [clojure.string :as str]
   [clojure.test :as t]
   [iced.nrepl.format :as sut]
   [iced.test-helper :as h]
   [medley.core :as medley]))

(t/use-fixtures :once h/repl-server-fixture)

(def ^:private test-indentation-rules
  (let [reg (keyword "#\"^icedtest\"")]
    {:foo "[[:block 1]]"
     :bar/baz "[[:block 2] [:inner 1]]"
     reg "[[:inner 0]]"}))

(defn- reg=
  [& regexps]
  (and (every? #(instance? java.util.regex.Pattern %) regexps)
       (apply = (map str regexps))))

(t/deftest set-indentation-rules!-test
  (sut/set-indentation-rules! {} 1)
  (t/is (empty? @(deref #'sut/indentation-rules)))

  (let [resp (h/message {:op "iced-set-indentation-rules"
                         :rules test-indentation-rules})
        rules @(deref #'sut/indentation-rules)]
    (t/is (contains? (:status resp) "done"))

    (t/is (> (count rules) 3))
    (t/is (= [[:block 1]] (get rules 'foo)))
    (t/is (= [[:block 2] [:inner 1]] (get rules 'bar/baz)))

    (let [[_ v] (medley/find-first #(reg= #"^icedtest" (first %)) rules)]
      (t/is (= [[:inner 0]] v)))))

(t/deftest set-indentation-ruled!-with-overwrite-test
  (sut/set-indentation-rules! {} 1)
  (t/is (empty? @(deref #'sut/indentation-rules)))

  (let [resp (h/message {:op "iced-set-indentation-rules"
                         :rules test-indentation-rules
                         :overwrite? 1})
        rules @(deref #'sut/indentation-rules)]
    (t/is (contains? (:status resp) "done"))

    (t/is (= 3 (count rules)))
    (t/is (map? rules))

    (t/is (= [[:block 1]] (get rules 'foo)))
    (t/is (= [[:block 2] [:inner 1]] (get rules 'bar/baz)))

    (let [[k v] (first (seq (dissoc rules 'foo 'bar/baz)))]
      (t/is (reg= #"^icedtest" k))
      (t/is (= [[:inner 0]] v)))))

(t/deftest code-test
  (let [resp (h/message {:op "iced-format-code-with-indents"
                         :code "(hello (world  )  )"
                         :alias-map {}})]
    (t/is (= "(hello (world))" (:formatted resp)))))

(t/deftest code-option-test
  (sut/set-indentation-rules! test-indentation-rules nil)
  (t/are [code alias-map expected]
         (= expected (:formatted (h/message {:op "iced-format-code-with-indents"
                                             :code code
                                             :alias-map alias-map})))
    "(bar/baz 1 2\n3)", {},          "(bar/baz 1 2\n  3)"
    "(b/baz 1 2\n3)",   {},          "(b/baz 1 2\n       3)"
    "(b/baz 1 2\n3)",   {"b" "bar"}, "(b/baz 1 2\n  3)"))

(t/deftest code-error-test
  (let [resp (h/message {:op "iced-format-code-with-indents"
                         :code "(hello (world)"
                         :alias-map {}})]
    (t/is (contains? (:status resp) "done"))
    (t/is (not (str/blank? (:error resp))))
    (t/is (pos-int? (:line resp)))
    (t/is (pos-int? (:column resp)))))

(t/deftest calculate-indent-level-test
  (sut/set-indentation-rules!
   {:foo "[[:block 1]]"
    :bar "[[:block 2]]"
    :baz/hello "[[:block 2]]"}
   nil)

  (t/are [code lnum alias-map expected]
         (= expected (:indent-level (h/message {:op "iced-calculate-indent-level"
                                                :code code
                                                :line-number lnum
                                                :alias-map alias-map})))
    "(foo 1 2\n3\n)",       1, {},         5
    "(bar 1 2\n3\n)",       1, {},         2
    "(baz/hello 1 2\n3\n)", 1, {},         2
    "(b/hello 1 2\n3\n)",   1, {},         9
    "(b/hello 1 2\n3\n)",   1, {:b "baz"}, 2))
