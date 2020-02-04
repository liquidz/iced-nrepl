(ns iced.nrepl.debug-test
  (:require [clojure.string :as str]
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
    (tap>' {:foo [:bar {:baz {:hello "abc" :world "def"}}]})
    (Thread/sleep 500)

    (let [resp (h/message {:op "iced-browse-tapped" :keys [0]
                           :max-string-length 2})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= "{:foo [:bar ...]}" (str/trim (str/join "" (:value resp))))))

    (let [resp (h/message {:op "iced-browse-tapped" :keys [0 ":foo"]
                           :max-string-length 2})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= "[:bar {:baz {:hello \"ab...\", etc ...}}]"
               (str/trim (str/join "" (:value resp))))))

    (let [resp (h/message {:op "iced-browse-tapped" :keys [0 ":foo" "/" "md=10" "msl=5"]
                           :max-string-length 2})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= "[:bar {:baz {:hello \"abc\", :world \"def\"}}]"
               (str/trim (str/join "" (:value resp))))))))

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

(t/deftest complete-tapped-test
  (when sut/supported?
    (h/message {:op "iced-clear-tapped"})
    (tap>' {:foo [:bar 0] :baz "hello"})
    (Thread/sleep 500)

    (let [resp (h/message {:op "iced-complete-tapped" :keys [0]})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= [":foo" ":baz"] (:complete resp))))

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
