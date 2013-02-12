(ns ecs-test.systems.ai
  (:require [ecs-test.core :refer [defcomponent make-comp ]]
            [ecs-test.systems.movement :refer [delta-loc rand-direction
                                        rand-velocity ->Direction]]
            [ecs-test.systems.rendering :refer [direction-img]]
            ecs-test.systems.ident)
  (:use (ecs-test.systems core))
  (:import (ecs-test.systems.movement.Position)
           (ecs-test.systems.ident.EntType)
           (ecs-test.systems.movement.Direction)
           (ecs-test.systems.movement.Velocity)))

;TODO 
;- have search mechanism like system.core's aspectfns
;  to see what behaviors an agent entity possesses
;- differentiate Behavior types???
;- behavior is an example of a component that an entity can
;  have more than one of...
;- also a huge example of where inter-component messaging is necessary

;(defprotocol Behavior)

;(defcomponent Behavior [behavior-fn])

;(defn random-walking-behavior [maxsteps])


;XXX is this a comp-fn?
;TODO take out hardcoded 5 in delta-loc :Velocity
(defn make-rand-move [{et :EntType pos :Position
                       vis :Visual dir :Direction vel :Velocity}]
  (let [new-dir (rand-direction (:dir dir))
        new-vel (rand-velocity 5)]
   {:Position (compfn delta-loc {:comps
                                       {:Direction new-dir
                                        :Position pos
                                        :Velocity new-vel}})
    :Visual (compfn direction-img {:comps
                                       {:EntType et
                                        :Direction new-dir
                                        :Visual vis
                                        :Velocity vel}})}))

(defn move-toward-player [{player-pos :Position}
                          {et :EntType curr-pos :Position
                           vis :Visual dir :Direction vel :Velocity}]
  (let [go-horiz (rand-nth [true false])
        new-vel (rand-velocity 5)
        new-dir (if go-horiz
                  (if (< (:x curr-pos) (:x player-pos))
                      (make-comp Direction :E (:dir dir))
                      (make-comp Direction :W (:dir dir)))
                  (if (< (:y curr-pos) (:y player-pos))
                      (make-comp Direction :S (:dir dir))
                      (make-comp Direction :N (:dir dir))))]
    {:Position (compfn delta-loc {:comps
                                       {:Direction new-dir
                                        :Position curr-pos
                                        :Velocity new-vel}})
     :Visual (compfn direction-img {:comps
                                       {:EntType et 
                                        :Direction new-dir
                                        :Visual vis
                                        :Velocity vel}})}))

(defn random-movement [ent one-in-x]
  "Entity -> int -> {Component}"
  (if (= (rand-int one-in-x) 0)  ; should move?
      (compfn make-rand-move ent)
      {})) ; otherwise nothing changes


