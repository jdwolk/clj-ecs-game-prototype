(ns ecs-test.test.core
  (:use (ecs-test unique core)
        (midje sweet)))

(comment
(fact "Entities extend the Unique protocol"
  (not (nil? (get-id (make-entity)))) => true)
)


(defcomponent MyComp [a b])
(fact "Entities extend the Entity protocol "
  (let [entity (make-entity (make-comp MyComp "hello" "there"))]
      (empty? (get-comp (make-entity) MyComp)) => true
      (empty? (get-comps (make-entity))) => true
      (= 1 (count (get-comp entity MyComp))) = true
      (= 1 (count (get-comps entity))) => true))

(fact "All components extend the Component protocol"
  (= 123456 (get-entity-id ((make-comp MyComp 1 2) 123456))) => true)
  
