(ns icedtest.spec.test
  (:require
   [clojure.spec.alpha :as spec]))

(defn success-func
  [_]
  true)
(spec/fdef success-func
        :args (spec/cat :_ any?)
        :ret boolean?)

(defn fail-func
  [_]
  true)
(spec/fdef fail-func
        :args (spec/cat :_ boolean?)
        :ret string?)

(defn no-spec-func
  []
  true)
