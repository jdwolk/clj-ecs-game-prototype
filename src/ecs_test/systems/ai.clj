(ns ecs-test.systems.ai
  (:require [ecs-test.core :refer [defcomponent make-comp ]]
            [ecs-test.systems.movement :refer [delta-loc rand-direction
                                        rand-velocity ->Direction]]
            [ecs-test.systems.rendering :refer [direction-img]])
  (:use (ecs-test.systems core))
  (:import (ecs-test.systems.movement.Position)
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
(defn make-rand-move [{pos :Position vis :Visual}]
  (let [new-dir (rand-direction)
        new-vel (rand-velocity 5)]
   {:Position (compfn delta-loc {:comps
                                       {:Direction new-dir
                                        :Position pos
                                        :Velocity new-vel}})
    :Visual (compfn direction-img {:comps
                                         {:Direction new-dir
                                          :Visual vis}})}))

(defn move-toward-player [{player-pos :Position} {curr-pos :Position vis :Visual}]
  (let [go-horiz (rand-nth [true false])
        new-vel (rand-velocity 5)
        new-dir (if go-horiz
                  (if (< (:x curr-pos) (:x player-pos))
                      (make-comp Direction :E)
                      (make-comp Direction :W))
                  (if (< (:y curr-pos) (:y player-pos))
                      (make-comp Direction :S)
                      (make-comp Direction :N)))]
    {:Position (compfn delta-loc {:comps
                                       {:Direction new-dir
                                        :Position curr-pos
                                        :Velocity new-vel}})
     :Visual (compfn direction-img {:comps
                                         {:Direction new-dir}})}))

(defn random-movement [ent one-in-x]
  "Entity -> int -> {Component}"
  (if (= (rand-int one-in-x) 0)  ; should move?
      (compfn make-rand-move ent)
      {})) ; otherwise nothing changes


