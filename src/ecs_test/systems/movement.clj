(ns ecs-test.systems.movement
  (:use (ecs-test core)
        (ecs-test.systems core)))

;(import '(ecs-test.systems.movement.Position))
(defcomponent Position [x y z])  ; z is height above 'ground level'
(defcomponent Direction [dir])

;Translates symbolic representation of dirs to numeric
(def dir-translate {:N 0
                    :E 1
                    :S 2
                    :W 3})

; Shameless ripoff of Rich Hickey's ants demo
; Increases clockwise starting @ North
(def dir-delta {0 [ 0  1]
                1 [ 1  0]
                2 [ 0 -1]
                3 [-1  0]})

(defn -delta-loc [x y dir]
  (let [[dx dy] (dir-delta (dir-translate dir))]
    [(+ x dx) (+ y dy)]))


;;;;;;;;;;;;; Component Fns ;;;;;;;;;;;;;;

(defn delta-loc [{pos :Position dir :Direction}]
  "Position -> Direction -> Position
   Given an entity's Position and a Direction, returns a new Position one
   increment in the given Direction. Ignores z direction (for now)"
  (let [[newx newy] (-delta-loc (:x pos) (:y pos) (:dir dir))]
    (assoc-entity-id 
      (:id pos)
      (make-comp Position newx newy (:z pos)))))



