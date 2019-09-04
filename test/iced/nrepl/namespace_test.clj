(ns iced.nrepl.namespace-test
  (:require [clojure.string :as str]
            [clojure.test :as t]
            [iced.nrepl.namespace :as sut]
            [iced.test-helper :as h]
            [orchard.namespace :as o.ns]))

(t/use-fixtures :once h/repl-server-fixture)

(t/deftest project-ns-list-test
  (t/testing "iced-project-ns-list"
    (let [resp (h/message {:op "iced-project-ns-list"})]
      (t/is (contains? (:status resp) "done"))
      (t/is (sequential? (:project-ns-list resp))))))

(defn- pseudo-ns-path [ns-name]
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

    (t/is (str/ends-with? (pseudo-ns-path "icedtest.namespace.foo.baz-test")
                          "/test_files/icedtest/namespace/bar/foo.clj"))

    (t/testing "default ns-path"
      (t/is (str/ends-with? (pseudo-ns-path "nonexisting.foo.bar")
                            "/src/nonexisting/foo/bar.clj"))

      (t/is (str/ends-with? (pseudo-ns-path "nonexisting.foo.bar-test")
                            "/test/nonexisting/foo/bar_test.clj")))))

(t/deftest pseudo-ns-path-error-test
  (with-redefs [o.ns/project-namespaces (constantly '[foo.bar barbaz])]
    (t/is (str/ends-with? (:path (sut/iced-pseudo-ns-path {:ns "foo.baz"}))
                          "foo/baz.clj"))))
