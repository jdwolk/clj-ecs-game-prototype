(ns ecs-test.utils.misc)

(defn splitlast [a-str regex]
  (last (clojure.string/split a-str regex)))

(defn class->keyword [klass]
  (keyword (splitlast (str klass) #"\.")))

(defn generate-id [] (str (java.util.UUID/randomUUID)))
