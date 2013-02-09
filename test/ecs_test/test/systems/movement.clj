(ns ecs-test.test.systems.movement
  (:use (ecs-test core)
        (ecs-test.systems core movement)
        (midje sweet))
  (:import (ecs-test.systems.movement.Position)
           (ecs-test.systems.movement.Velocity)
           (ecs-test.systems.movement.Direction)))

(fact "delta-loc-calc gives the correct increment for the x, y, and dir"
  (delta-loc-calc 0 0 1 :N) => [0 1]
  (delta-loc-calc 0 10 1 :N) => [0 11]
  (delta-loc-calc 10 0 1 :N) => [10 1]
  (delta-loc-calc 10 10 1 :N) => [10 11]
  (delta-loc-calc 0 0 0 :N) => [0 0]
  (delta-loc-calc 0 0 5 :N) => [0 5]

  (delta-loc-calc 0 0 1 :E) => [1 0]
  (delta-loc-calc 0 10 1 :E) => [1 10]
  (delta-loc-calc 10 0 1 :E) => [11 0]
  (delta-loc-calc 10 10 1 :E) => [11 10]
  (delta-loc-calc 0 0 0 :E) => [0 0]
  (delta-loc-calc 0 0 5 :E) => [5 0]
      
  (delta-loc-calc 0 0 1 :S) => [0 -1]
  (delta-loc-calc 0 10 1 :S) => [0 9]
  (delta-loc-calc 10 0 1 :S) => [10 -1]
  (delta-loc-calc 10 10 1 :S) => [10 9]
  (delta-loc-calc 0 0 0 :S) => [0 0]
  (delta-loc-calc 0 0 5 :S) => [0 -5]

  (delta-loc-calc 0 0 1 :W) => [-1 0]
  (delta-loc-calc 0 10 1 :W) => [-1 10]
  (delta-loc-calc 10 0 1 :W) => [9 0]
  (delta-loc-calc 10 10 1 :W) => [9 10]
  (delta-loc-calc 0 0 0 :W) => [0 0]
  (delta-loc-calc 0 0 5 :W) => [-5 0])

(fact "delta-loc returns new position incremented by one in the right direction"
  (let [poscomp ((make-comp Position 0 0 0) "blah-id")
        velcomp ((make-comp Velocity 1) "blah-id")]
    (:y (delta-loc {:Position poscomp :Velocity velcomp 
                    :Direction ((make-comp Direction :N) "blah-id")}))
      => 1
    (:x (delta-loc {:Position poscomp :Velocity velcomp
                    :Direction ((make-comp Direction :E) "blah-id")}))
      => 1
    (:y (delta-loc {:Position poscomp :Velocity velcomp 
                    :Direction ((make-comp Direction :S) "blah-id")}))
      => -1
    (:x (delta-loc {:Position poscomp :Velocity velcomp 
                    :Direction ((make-comp Direction :W) "blah-id")}))
      => -1))

