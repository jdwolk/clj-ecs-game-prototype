(ns ecs-test.test.core
  (:use (ecs-test core)
        (midje sweet)))

(defcomponent TestComp123 [a])

(fact "Entities extend the Unique protocol"
  (not (nil? (get-id (make-entity)))) => true)

(fact "Components extend the Unique protocol"
  (not (nil? (get-id (make-comp TestComp123 "test")))) => true)

(fact "Component ids have unique values"
  (= (get-id (make-comp TestComp123 "hello"))
     (get-id (make-comp TestComp123 "hello"))) => false)

(fact "Entities extend the Entity protocol "
<<<<<<< Updated upstream
  (let [entity (make-entity (make-comp TestComp123 "hello"))]
      (empty? (get-comp (make-entity) TestComp123)) => true
      (empty? (get-comps (make-entity))) => true
      (= 1 (count (get-comp entity TestComp123))) = true
      (= 1 (count (get-comps entity))) => true))

=======
  (let [entity (make-entity (make-comp MyComp "hello" "there"))]
      (empty? (get-comp (make-entity) MyComp)) => true
      (empty? (get-comps (make-entity))) => true
      (= 1 (count (get-comp entity MyComp))) = true
      (= 1 (count (get-comps entity))) => true))
>>>>>>> Stashed changes

