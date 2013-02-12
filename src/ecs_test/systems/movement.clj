(ns ecs-test.systems.movement
  (:use (ecs-test core)
        (ecs-test.systems core)))

;(import '(ecs-test.systems.movement.Position))
(defcomponent Position [x y z])  ; z is height above 'ground level'
(defcomponent Direction [dir prev-dir])
(defcomponent Velocity [units])  ; units per ...milli?

;Translates symbolic representation of dirs to numeric
(def dir-translate {:N 2
                    :E 1
                    :S 0
                    :W 3})

; Shameless ripoff of Rich Hickey's ants demo
; Increases clockwise starting @ North
(def dir-delta [[ 0  1]
                [ 1  0]
                [ 0 -1]
                [-1  0]])

(defn rand-dir [& {dirs :dirs :or {dirs (keys dir-translate)}}]
  (nth dirs (rand (count dirs))))

(defn delta-loc-calc [x y vel dir]
  (let [[dx dy] (map (partial * vel) (nth dir-delta (dir-translate dir)))]
    [(+ x dx) (+ y dy)]))

;;;;;;;;;;;;; Component Fns ;;;;;;;;;;;;;;

(defn delta-loc [{pos :Position, vel :Velocity, dir :Direction}]
  "Position -> Velocity -> Direction -> Position
   Given an entity's Position Velcoty and a Direction, 
   returns a new Position n increments in the given Direction
   (n is Velcity). Ignores z direction (for now)"
  (let [[newx newy] (delta-loc-calc (:x pos) 
                                    (:y pos) 
                                    (:units vel)
                                    (:dir dir))]
      (make-comp Position newx newy (:z pos))))

;XXX what are these? Compfns?
(defn zero-velocity []
  (make-comp Velocity 0))

(defn rand-velocity [max-vel]
  (make-comp Velocity (rand (inc max-vel))))

(defn rand-direction [old-dir]
  (make-comp Direction (rand-dir) old-dir))

(defn rand-pos [max-x max-y]
  (make-comp Position (rand-int (inc max-x)) (rand-int (inc max-y)) 0))

