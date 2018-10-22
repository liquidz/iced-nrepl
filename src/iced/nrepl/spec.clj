(ns iced.nrepl.spec)

(defn- try-requires [& syms]
  (try 
    (doseq [sym syms]
      (require sym))
    (catch Exception ex nil)))

(try-requires 'clojure.spec.test.alpha
              'clojure.spec.test)

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
        {:result "NG" :num-tests num-tests :message (.getMessage result) :fail fail}

        :else
        {:result "NG" :num-tests num-tests :fail fail}))))
