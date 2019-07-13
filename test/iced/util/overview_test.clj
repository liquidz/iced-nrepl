(ns iced.util.overview-test
  (:require [clojure.test :as t]
            [iced.util.overview :as sut]))

(t/deftest cut-test
  (t/is (= nil (sut/cut nil 5)))
  (t/is (= 'sym (sut/cut 'sym 5)))
  (t/is (= 'kwd (sut/cut 'kwd 5)))
  (t/is (= 1234 (sut/cut 1234 5))))

(t/deftest cut-list-test
  (t/are [in n expected] (= expected (sut/cut in n))
    '(1 2 3), 2,  '(1 2 ...)
    '(1 2 3), 3,  '(1 2 3)
    '(1 2 3), 4,  '(1 2 3)
    '(1 2 3), 0,  '(...)
    '(1 2 3), -1, '(...)
    '(),      2,  '()
    '(),      0,  '()
    '(),      -1, '()))

(t/deftest cut-vector-test
  (t/are [in n expected] (= expected (sut/cut in n))
    [1 2 3], 2,  '[1 2 ...]
    [1 2 3], 3,  '[1 2 3]
    [1 2 3], 4,  '[1 2 3]
    [1 2 3], 0,  '[...]
    [1 2 3], -1, '[...]
    [],      2,  []
    [],      0,  []
    [],      -1, []))

(t/deftest cut-map-test
  (t/are [in n expected] (= expected (sut/cut in n))
    {:a 1 :b 2 :c 3}, 2,  {:a 1 :b 2 'etc '...}
    {:a 1 :b 2 :c 3}, 3,  {:a 1 :b 2 :c 3}
    {:a 1 :b 2 :c 3}, 4,  {:a 1 :b 2 :c 3}
    {:a 1 :b 2 :c 3}, 0,  {'etc '...}
    {:a 1 :b 2 :c 3}, -1, {'etc '...}
    {}, 2, {}
    {}, 0, {}
    {}, -1, {}))

(t/deftest cut-set-test
  (t/are [in n expected] (= expected (sut/cut in n))
    #{1 2 3}, 3,  #{1 2 3}
    #{1 2 3}, 4,  #{1 2 3}
    #{1 2 3}, 0,  #{'...}
    #{1 2 3}, -1, #{'...}
    #{},      2,  #{}
    #{},      0,  #{}
    #{},      -1, #{})
  (t/is (contains? #{#{1 2 '...} #{1 3 '...}} (sut/cut #{1 2 3} 2))))

(t/deftest cut-string-test
  (t/are [in n expected] (= expected (sut/cut in n))
    "abc", 2,  "ab..."
    "abc", 3,  "abc"
    "abc", 4,  "abc"
    "abc", 0,  "..."
    "abc", -1, "..."
    "",    2,  ""
    "",    0,  ""
    "",    -1, ""))

(t/deftest overview-test
  (t/is (= nil (sut/overview nil)))
  (t/is (= "foo" (sut/overview "foo")))
  (t/is (= "fo..." (sut/overview "foo" {:max-depth 0 :max-string-length 2}))))

(t/deftest overview-list-test
  (t/are [in context expected] (= expected (sut/overview in context))
    '(1 2 3 4),   {},                   '(1 2 3 4)
    '(1 nil 3),   {},                   '(1 nil 3)
    '(1 2 (3 4)), {},                   '(1 2 (3 ...))
    '(),          {},                   '()
    '(1 2 (3 4)), {:max-depth 0},       '(1 ...)
    '(1),         {:max-depth 0},       '(1)
    '(1 2 (3 4)), {:max-depth 2},       '(1 2 (3 4))
    '(1 2 (3 4)), {:max-list-length 2}, '(1 2 ...)
    '(1 2),       {:max-list-length 2}, '(1 2)))

(t/deftest overview-vector-test
  (t/are [in context expected] (= expected (sut/overview in context))
    [1 2 3 4],   {},                     '[1 2 3 4]
    [1 nil 3],   {},                     '[1 nil 3]
    [1 2 [3 4]], {},                     '[1 2 [3 ...]]
    [],          {},                     '[]
    [1 2 [3 4]], {:max-depth 0},         '[1 ...]
    [1],         {:max-depth 0},         '[1]
    [1 2 [3 4]], {:max-depth 2},         '[1 2 [3 4]]
    [1 2 [3 4]], {:max-vector-length 2}, '[1 2 ... ]
    [1 2],       {:max-vector-length 2}, '[1 2]))

(t/deftest overview-map-test
  (t/are [in context expected] (= expected (sut/overview in context))
    {:a 1 :b 2 :c 3},           {},                  {:a 1 :b 2 :c 3}
    {:a 1 :b nil},              {},                  {:a 1 :b nil}
    {:a 1 :b 2 :c {:d 3 :e 4}}, {},                  {:a 1 :b 2 :c {:d 3 'etc '...}}
    {},                         {},                  {}
    {:a 1 :b 2 :c {:d 3 :e 4}}, {:max-depth 0},      {:a 1 'etc '...}
    {:a 1},                     {:max-depth 0},      {:a 1}
    {:a 1 :b 2 :c {:d 3 :e 4}}, {:max-depth 2},      {:a 1 :b 2 :c {:d 3 :e 4}}
    {:a 1 :b 2 :c 3},           {:max-map-length 2}, {:a 1 :b 2 'etc '...}
    {:a 1 :b 2},                {:max-map-length 2}, {:a 1 :b 2}))

(t/deftest overview-set-test
  (t/are [in context expected] (contains? expected (sut/overview in context))
    #{1 2 3},      {},                  #{#{1 2 3}}
    #{1 nil 3},    {},                  #{#{1 nil 3}}
    #{1 2 #{3 4}}, {},                  #{#{1 2 #{3 '...}}
                                          #{1 2 #{4 '...}}}
    #{},           {},                  #{#{}}
    #{1 2 3},      {:max-depth 0},      #{#{1 '...}
                                          #{2 '...}
                                          #{3 '...}}
    #{1},          {:max-depth 0},      #{#{1}}
    #{1 2 #{3 4}}, {:max-depth 2},      #{#{1 2 #{3 4}}}
    #{1 2 3},      {:max-set-length 2}, #{#{1 2 '...}
                                          #{1 3 '...}
                                          #{2 3 '...}}
    #{1 2},        {:max-set-length 2}, #{#{1 2}}))

(t/deftest overview-string-test
  (t/are [in context expected] (= expected (sut/overview in context))
    "hello", {},                                  "hello"
    "",      {},                                  ""
    "hello", {:max-depth 0},                      "hello"
    "hello", {:max-depth 0 :max-string-length 2}, "he..."
    "he",    {:max-depth 0 :max-string-length 2}, "he"
    "hello", {:max-string-length 2},              "hello"))
