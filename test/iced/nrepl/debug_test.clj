(ns iced.nrepl.debug-test
  (:require
   [clojure.string :as str]
   [clojure.test :as t]
   [iced.nrepl.debug :as sut]
   [iced.test-helper :as h]))

(t/use-fixtures :once h/repl-server-fixture)

(def ^:private tap>' (resolve 'clojure.core/tap>))

(t/deftest list-tapped-test
  (when sut/supported?
    (h/message {:op "iced-clear-tapped"})
    (t/is (empty?  (:tapped (h/message {:op "iced-list-tapped"}))))

    (tap>' 1)
    (tap>' "hello")
    (tap>' ["foo" "bar" "baz"])
    (tap>' {:foo 1 :bar {:baz "abcd"}})
    (Thread/sleep 500)

    (let [resp (h/message {:op "iced-list-tapped"
                           :max-vector-length 2
                           :max-string-length 3})
          tapped (get resp :tapped [])]
      (t/is (contains? (:status resp) "done"))
      (t/is (= 4 (count tapped)))
      (t/is (= ["1"
                "hello"
                "[\"foo\" \"bar\" ...]"
                "{:foo 1, :bar {:baz \"abc...\"}}"]
               tapped)))

    (h/message {:op "iced-clear-tapped"})
    (let [resp (h/message {:op "iced-list-tapped"})]
      (t/is (contains? (:status resp) "done"))
      (t/is (empty? (:tapped resp))))))

(t/deftest browse-tapped-test
  (when sut/supported?
    (h/message {:op "iced-clear-tapped"})
    (tap>' {:foo [:bar {:baz {:hello "abc" :world "def"}}
                  {true "bool true"
                   false "bool false"
                   "true" "string true"
                   nil "null"}]})
    (Thread/sleep 500)

    (let [resp (h/message {:op "iced-browse-tapped" :keys [0]
                           :max-string-length 2})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= "{:foo [:bar ...]}"
               (str/trim (str/join "" (:value resp))))))

    (let [resp (h/message {:op "iced-browse-tapped" :keys [0 ":foo"]
                           :max-string-length 2})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= "[:bar {:baz {:hello \"ab...\", etc ...}} {true \"bo...\", etc ...}]"
               (str/trim (str/join "" (:value resp))))))

    (t/testing "bool"
      (let [resp (h/message {:op "iced-browse-tapped" :keys [0 ":foo" 2 "true"]
                             :max-string-length 2})]
        (t/is (contains? (:status resp) "done"))
        (t/is (= "\"bool true\""
                 (str/trim (str/join "" (:value resp))))))

      (let [resp (h/message {:op "iced-browse-tapped" :keys [0 ":foo" 2 "false"]
                             :max-string-length 2})]
        (t/is (contains? (:status resp) "done"))
        (t/is (= "\"bool false\""
                 (str/trim (str/join "" (:value resp))))))

      (let [resp (h/message {:op "iced-browse-tapped" :keys [0 ":foo" 2 "\"true\""]
                             :max-string-length 2})]
        (t/is (contains? (:status resp) "done"))
        (t/is (= "\"string true\""
                 (str/trim (str/join "" (:value resp)))))))

    (t/testing "nil"
      (let [resp (h/message {:op "iced-browse-tapped" :keys [0 ":foo" 2 "nil"]
                             :max-string-length 2})]
        (t/is (contains? (:status resp) "done"))
        (t/is (= "\"null\""
                 (str/trim (str/join "" (:value resp)))))))))

(defn- test-browse-tapped
  [ks]
  (some->> (h/message {:op "iced-browse-tapped" :keys ks})
           :value
           (str/join "")
           str/trim))

(t/deftest browse-tapped-option-test
  (when sut/supported?
    (h/message {:op "iced-clear-tapped"})
    (tap>' {:foo [:bar {:baz {:hello "abc" :world "def"}}]})
    (tap>' {:foo (list 1 2 3 4)})
    (tap>' {:foo [5 6 7 8]})
    (tap>' {:foo #{:a :b}})
    (tap>' {:foo {:c :d :e :f :g :h}})
    (Thread/sleep 500)

    (t/testing "max-depth"
      (t/is (= "[:bar {:baz {:hello \"abc\", etc ...}}]"
               (test-browse-tapped [0 ":foo"])))
      (t/is (= "[:bar {:baz {:hello \"abc\", :world \"def\"}}]"
               (test-browse-tapped [0 ":foo" "/" "md=10"]))))

    (t/testing "max-string-length"
      (t/is (= "[:bar {:baz {:hello \"abc\", etc ...}}]"
               (test-browse-tapped [0 ":foo"])))
      (t/is (= "[:bar {:baz {:hello \"ab...\", etc ...}}]"
               (test-browse-tapped [0 ":foo" "/" "msl=2"]))))

    (t/testing "max-list-length"
      (t/is (= "(1 2 3 4)"
               (test-browse-tapped [1 ":foo"])))
      (t/is (= "(1 2 ...)"
               (test-browse-tapped [1 ":foo" "/" "mll=2"]))))

    (t/testing "max-vector-length"
      (t/is (= "[5 6 7 8]"
               (test-browse-tapped [2 ":foo"])))
      (t/is (= "[5 6 ...]"
               (test-browse-tapped [2 ":foo" "/" "mvl=2"]))))

    (t/testing "max-set-length"
      (t/is (contains? #{"#{:a :b}" "#{:b :a}"}
                       (test-browse-tapped [3 ":foo"])))
      (t/is (contains? #{"#{:a ...}" "#{:b ...}"}
                       (test-browse-tapped [3 ":foo" "/" "mSl=1"]))))

    (t/testing "max-map-length"
      (t/is (= "{:c :d, :e :f, :g :h}"
               (test-browse-tapped [4 ":foo"])))
      (t/is (= "{:c :d, :e :f, etc ...}"
               (test-browse-tapped [4 ":foo" "/" "mml=2"]))))))

(t/deftest fetch-tapped-children-test
  (when sut/supported?
    (h/message {:op "iced-clear-tapped"})

    (tap>' {:foo [:bar {:baz {:hello "abc" :world "def"}}]})
    (Thread/sleep 500)

    (let [resp (h/message {:op "iced-fetch-tapped-children" :keys [0]})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= [{:name ":foo" :has-children? "true"}] (:children resp))))

    (let [resp (h/message {:op "iced-fetch-tapped-children" :keys [0 ":foo"]})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= [{:name "0" :has-children? "true"}
                {:name "1" :has-children? "true"}]
               (:children resp))))

    (let [resp (h/message {:op "iced-fetch-tapped-children" :keys [0 ":foo" 0]})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= [{:name ":bar" :has-children? "false"}] (:children resp))))

    (let [resp (h/message {:op "iced-fetch-tapped-children" :keys [0 ":foo" 1 ":baz" ":world"]})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= [{:name "def" :has-children? "false"}] (:children resp))))

    (let [resp (h/message {:op "iced-fetch-tapped-children" :keys [0 ":invalid"]})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= [] (:children resp))))))

(t/deftest fetch-tapped-children-lazyseq-test
  (when sut/supported?
    (h/message {:op "iced-clear-tapped"})

    (tap>' (map #(str % "!") (range 2)))
    (Thread/sleep 500)

    (let [resp (h/message {:op "iced-fetch-tapped-children" :keys [0]})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= [{:name "0" :has-children? "true"}
                {:name "1" :has-children? "true"}]
               (:children resp))))

    (let [resp (h/message {:op "iced-fetch-tapped-children" :keys [0 1]})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= [{:name "1!" :has-children? "false"}]
               (:children resp))))))

(t/deftest complete-tapped-test
  (when sut/supported?
    (h/message {:op "iced-clear-tapped"})
    (tap>' {:foo [:bar 0] :baz "hello"
            true 1
            false 2
            "true" 3
            nil 4})
    (Thread/sleep 500)

    (let [resp (h/message {:op "iced-complete-tapped" :keys [0]})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= [":foo" ":baz" "true" "false" "\"true\"" "nil"]
               (:complete resp))))

    (let [resp (h/message {:op "iced-complete-tapped" :keys [0 ":foo"]})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= [0 1] (:complete resp))))

    (let [resp (h/message {:op "iced-complete-tapped" :keys [0 ":baz"]})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= [] (:complete resp))))))

(t/deftest not-supported-test
  (when-not sut/supported?
    (doseq [op ["iced-list-tapped"
                "iced-clear-tapped"
                "iced-browse-tapped"
                "iced-complete-tapped"]]
      (let [resp (h/message {:op op})]
        (t/is (contains? (:status resp) "done"))
        (t/is (str/includes? (:error resp) "not supported"))))))
