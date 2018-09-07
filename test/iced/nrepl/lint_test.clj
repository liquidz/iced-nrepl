(ns iced.nrepl.lint-test
  (:require [clojure
             [string :as str]
             [test :as t]]
            [clojure.java.io :as io]
            [eastwood.lint :as el]
            [fudje.sweet :as fj]
            [iced.nrepl.lint :as sut]))

(t/deftest lint-by-eastwood-test
  (with-redefs [el/lint (constantly {:warnings [{:column 1 :line 2 :msg "dummy"
                                                 :uri (io/as-url (io/file "project.clj")) :foo "bar"}]})]
    (t/is
     (compatible
      (sut/lint-by-eastwood 'dummy nil)
      (fj/just [{:column 1 :line 2 :msg "dummy"
                 :path (fj/checker #(str/ends-with? % "project.clj"))}])))))
