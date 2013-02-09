(ns ecs-test.test.systems.core
  (:use (ecs-test core)
        (ecs-test.systems core movement)
        (midje sweet))
  (:import (ecs-test.systems.movement.Position)
           (ecs-test.systems.movement.Direction)))

(fact "compfn applies compfn to comps of entity"
  (let [ent (make-entity (make-comp Position 0 0 0)
                         (make-comp Direction :N))]
    (count (compfn identity ent)) => 2))

(fact "apply-if applies compfn to comps of entity if
       all aspectfns are met"
  (let [ent (make-entity (make-comp Position 0 0 0)
                         (make-comp Direction :N))]
    (count (apply-if (constantly true) identity ent)) => 2))

