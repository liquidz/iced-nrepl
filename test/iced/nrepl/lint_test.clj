(ns iced.nrepl.lint-test
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [clojure.test :as t]
            [eastwood.lint :as el]
            [fudje.sweet :as fj]
            [iced.nrepl.lint :as sut]
            [iced.test-helper :as h])
  (:import java.net.URI))

(t/use-fixtures :once h/repl-server-fixture)

(t/deftest lint-file-clj-success-test
  (let [path "src/iced/nrepl/lint.clj"
        _ (h/message {:op "load-file" :file (slurp path)})
        resp (h/message {:op "iced-lint-file"
                         :file path
                         :env "clj"
                         :opt {"linters" ["all"]}})]
    (t/is (contains? (:status resp) "done"))
    (t/is (not (contains? resp :lint-warnings)))))

(t/deftest lint-file-clj-warning-test
  (let [path "test/files/lint/private_never_used.clj"
        _ (h/message {:op "load-file" :file (slurp path)})
        resp (h/message {:op "iced-lint-file"
                         :file path
                         :env "clj"
                         :opt {"linters" ["all"]}})
        warnings (get resp :lint-warnings [])]
    (t/is (contains? (:status resp) "done"))
    (t/is (= 1 (count warnings)))
    (t/is
     (compatible
      (first warnings)
      (fj/just {:column 1
                :line 3
                :msg (fj/checker string?)
                :path (.getAbsolutePath (io/file path))})))))

(t/deftest lint-by-eastwood-with-invalid-column-line-test
  (with-redefs [el/lint (constantly {:warnings [{:kind :lint-warning
                                                 :warn-data {:column []
                                                             :line "invalid"
                                                             :msg "dummy"
                                                             :uri (URI. "./project.clj")
                                                             :foo "bar"}}]})]
    (t/is
     (compatible
      (sut/lint-by-eastwood 'dummy nil)
      (fj/just [{:msg "dummy" :path (fj/checker #(str/ends-with? % "project.clj"))}])))))

(t/deftest lint-by-joker-test
  (with-redefs [sut/working-directory (constantly ".")
                shell/sh (constantly {:exit 0 :out "" :err ""})]
    (t/is (empty? (sut/lint-by-joker "dummy file"))))

  (let [dummy-error (str "foo.cljs:8:3: Parse warning: Wrong number of args (1) passed to user/myplus\n"
                         "foo.cljs:2:14: Parse warning: unused namespace clojure.string\n")]
    (with-redefs [sut/working-directory (constantly ".")
                  shell/sh (constantly {:exit 1 :out "" :err dummy-error})]
      (t/is
       (compatible
        (sut/lint-by-joker "dummy file")
        (fj/just [{:path "dummy file" :line 8 :column 3
                   :msg "Parse warning: Wrong number of args (1) passed to user/myplus"}
                  {:path "dummy file" :line 2 :column 14
                   :msg "Parse warning: unused namespace clojure.string"}] :in-any-order))))))

(t/deftest lint-by-joker-with-unused-undersocred-binding-test
  (let [dummy-error (str "foo.cljs:1:2: unused binding: _foo\n"
                         "foo.cljs:3:4: Unable to resolve symbol: _bar\n")]
    (with-redefs [sut/working-directory (constantly ".")
                  shell/sh (constantly {:exit 1 :out "" :err dummy-error})]
      (t/is (empty? (sut/lint-by-joker "dummy file"))))))

(t/deftest lint-by-joket-failure-test
  (with-redefs [sut/working-directory (constantly ".")
                shell/sh (fn [& _] (throw (Exception. "test")))]
    (t/is (thrown? Exception (sut/lint-by-joker "dummy file")))))

(t/deftest check-file-syntax-test
  (let [path "test/files/lint/not_closing_paren.edn"
        resp (h/message {:op "iced-lint-file"
                         :file path
                         :env "clj"})
        warnings (get resp :lint-warnings [])]
    (t/is (contains? (:status resp) "done"))
    (t/is (= 1 (count warnings)))
    (t/is
     (compatible
      (first warnings)
      (fj/just {:column 0 :line 3 :msg (fj/checker string?) :path path}))))

  (let [path "test/files/lint/not_matched_closing_paren.edn"
        resp (h/message {:op "iced-lint-file"
                         :file path
                         :env "clj"})
        warnings (get resp :lint-warnings [])]
    (t/is (contains? (:status resp) "done"))
    (t/is (= 1 (count warnings)))
    (t/is
     (compatible
      (first warnings)
      (fj/just {:column 0 :line 6 :msg (fj/checker string?) :path path})))))
