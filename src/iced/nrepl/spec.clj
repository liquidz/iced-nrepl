(ns iced.nrepl.spec
  (:require [clojure.spec.test.alpha :as stest]))

(defn check [sym num-tests]
  (let [opts {:clojure.spec.test.check/opts {:num-tests num-tests}}
        test-results (stest/check sym opts)
        [{:clojure.spec.test.check/keys [ret]}] test-results
        {:keys [result num-tests fail]} ret]
    (cond
      (empty? test-results)
      {:result "OK" :num-tests 0}

      (true? result)
      {:result "OK" :num-tests num-tests}

      (instance? Exception result)
      {:result "NG" :num-tests num-tests :message (.getMessage result) :fail fail}

      :else
      {:result "NG" :num-tests num-tests :fail fail})))
