(ns iced.nrepl.namespace-test
  (:require
   [clojure.string :as str]
   [clojure.test :as t]
   [iced.nrepl.namespace :as sut]
   [iced.test-helper :as h]))

(t/use-fixtures :once h/repl-server-fixture)

(t/deftest project-ns-list-test
  (t/testing "iced-project-ns-list"
    (let [resp (h/message {:op "iced-project-ns-list"})]
      (t/is (contains? (:status resp) "done"))
      (t/is (sequential? (:project-ns-list resp))))))

(defn- pseudo-ns-path
  [ns-name]
  (let [resp (h/message {:op "iced-pseudo-ns-path" :ns ns-name})]
    (t/is (contains? (:status resp) "done"))
    (:path resp)))

(t/deftest pseudo-ns-path-test
  (let [_ (h/message {:op "eval" :code "(require 'icedtest.namespace.foo.bar)"})
        _ (h/message {:op "eval" :code "(require 'icedtest.namespace.bar.baz)"})]
    (t/is (str/ends-with? (pseudo-ns-path "icedtest.namespace.foo.bar")
                          "/test_files/icedtest/namespace/foo/bar.clj"))

    (t/is (str/ends-with? (pseudo-ns-path "icedtest.namespace.foo.baz")
                          "/test_files/icedtest/namespace/foo/baz.clj"))
    (t/is (str/ends-with? (pseudo-ns-path "icedtest.namespace.foo.bar-test")
                          "/test_files/icedtest/namespace/foo/bar_test.clj"))

    (t/is (str/ends-with? (pseudo-ns-path "icedtest.namespace.bar.baz-test")
                          "/test_files/icedtest/namespace/bar/baz_test.clj"))

    (t/is (str/ends-with? (pseudo-ns-path "icedtest.namespace.bar.foo-test")
                          "/test_files/icedtest/namespace/bar/foo_test.clj"))

    (t/is (str/ends-with? (pseudo-ns-path "icedtest.namespace.bar.foo")
                          "/test_files/icedtest/namespace/bar/foo.clj"))

    (t/testing "default ns-path"
      (t/is (str/ends-with? (pseudo-ns-path "nonexisting.foo.bar")
                            "/src/nonexisting/foo/bar.clj"))

      (t/is (str/ends-with? (pseudo-ns-path "nonexisting.foo.bar-test")
                            "/test/nonexisting/foo/bar_test.clj")))))

(t/deftest pseudo-ns-path-error-test
  (t/is (str/ends-with? (:path (sut/iced-pseudo-ns-path {:ns "foo.baz"}))
                        "foo/baz.clj")))

(t/deftest java-class-candidates-test
  (t/testing "single candidate"
    (let [resp (h/message {:op "iced-java-class-candidates"
                           :symbol "UUID"})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= ["java.util.UUID"] (:candidates resp)))))

  (t/testing "no candidates"
    (let [resp (h/message {:op "iced-java-class-candidates"
                           :symbol "NonExisting"})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= [] (:candidates resp)))))

  (t/testing "invalid message"
    (let [resp (h/message {:op "iced-java-class-candidates"})]
      (t/is (contains? (:status resp) "done"))
      (t/is (contains? (:status resp) "failed"))
      (t/is (some? (:error resp)))))

  (t/testing "additional class-map"
    (let [resp (h/message {:op "iced-java-class-candidates"
                           :symbol "IcedTestClass"
                           :class-map {"iced.nrepl.namespace-test" ["IcedTestClass"]}})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= ["iced.nrepl.namespace-test.IcedTestClass"]
               (:candidates resp)))))

  (t/testing "multiple candidates"
    (let [resp (h/message {:op "iced-java-class-candidates"
                           :symbol "SameNameClass"
                           :class-map {"iced.nrepl.namespace-test.foo" ["SameNameClass"]
                                       "iced.nrepl.namespace-test.bar" ["SameNameClass"]}})]
      (t/is (contains? (:status resp) "done"))
      (t/is (= #{"iced.nrepl.namespace-test.foo.SameNameClass"
                 "iced.nrepl.namespace-test.bar.SameNameClass"}
               (set (:candidates resp)))))))
