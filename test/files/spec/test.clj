(ns files.spec.test
   (:require [clojure.spec.alpha :as s]))

(defn success-func [_] true)
(s/fdef success-func
  :args (s/cat :_ any?)
  :ret boolean?)

(defn fail-func [_] true)
(s/fdef fail-func
        :args (s/cat :_ any?)
        :ret string?)

(defn no-spec-func [] true)
