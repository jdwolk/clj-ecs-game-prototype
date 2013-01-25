(ns ecs-test.utils.mathops)

(defn vector-op [op & vs]
  (let [cols [:x :y :z]] ; assume each vector has :x, :y, :z
    (apply map op (partition (count cols)
                            (for [v vs, c cols] (c v))))))

(defn vector-add [v1 v2]
  (vector-op + v1 v2))

(defn vector-sub [v1 v2]
  (vector-op - v1 v2))

(defn vector-scale [scalar v]
  (vector-op (partial * scalar) v))

(defn vector-length [v]
  (Math/sqrt (reduce + (vector-op (fn [x] (Math/pow x 2)) v))))

  ;{:x (+ (:x v1) (:x v2))
  ; :y (+ (:y v1) (:y v2))
  ; :z (+ (:z v1) (:z v2))})

;(apply map + (partition 3 '(1 2 3 4 5 6)))
;(reduce + (map :x [v1 v2]))

