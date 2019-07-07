(ns iced.nrepl.spec)

(defn- try-requires [& syms]
  (try
    (doseq [sym syms]
      (require sym))
    (catch Exception _ nil)))

(try-requires 'clojure.spec.test.alpha
              'clojure.spec.test
              'clojure.test.check.generators)

(defn- convert-failed-input [failed-input]
  ;; NOTE: failed-input
  ;;       [[arg1-1, arg1-2], [arg2-1, arg2-2], ...]
  (mapv #(mapv pr-str %) failed-input))

(defmacro stest [fname & args]
  `(when-let [f# (or (resolve (symbol "clojure.spec.test.alpha" ~fname))
                     (resolve (symbol "clojure.spec.test" ~fname)))]
     (f# ~@args)))

(defn check [sym num-tests]
  (when-let [test-results (stest "check" sym {:clojure.spec.test.check/opts {:num-tests num-tests}})]
    (let [ret (-> test-results first :clojure.spec.test.check/ret)
          {:keys [result num-tests fail]} ret]
      (cond
        (empty? test-results)
        {:result "OK" :num-tests 0}

        (true? result)
        {:result "OK" :num-tests num-tests}

        (instance? Exception result)
        {:result "NG"
         :num-tests num-tests
         :error (.getMessage ^Exception result)
         :failed-input (convert-failed-input fail)}

        :else
        {:result "NG"
         :num-tests num-tests
         :failed-input (convert-failed-input fail)}))))

(defn ^{:doc "Returns the checking spec result."
        :requires {"symbol" "Symbol to check spec."
                   "num-tests" "Expected number of tests."}
        :optional {}
        :returns {"result" "'OK' or 'NG'."
                  "num-tests" "Actual number of tests."
                  "error" "Error message if occured."
                  "failed-input" "The input when error occured."
                  "status" "done"}}
  iced-spec-check [msg]
  (let [{sym :symbol num-tests :num-tests} msg]
    (check (symbol sym) num-tests)))
