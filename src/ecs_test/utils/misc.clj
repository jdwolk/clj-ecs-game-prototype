(ns ecs-test.utils.misc)

(defn splitlast [a-str regex]
  (last (clojure.string/split a-str regex)))

(defn class->keyword [klass]
  (keyword (splitlast (str klass) #"\.")))

(defn generate-id [] (str (java.util.UUID/randomUUID)))

(defn wrap-inc [n maxn]
  (if (< n (dec maxn)) (inc n) 0))

(defn keyrange
  ([key end]
    (keyrange key 0 end))
  ([key start end]
    (let [key-str (if (keyword? key) (name key) key)]
      (map (fn [n] (keyword (str key-str n))) (range start end)))))
