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
      (empty? (get-component (make-entity) MyComp)) => true
      (empty? (get-components (make-entity))) => true
      (= 1 (count (get-component entity MyComp))) = true
      (= 1 (count (get-components entity))) => true))

(fact "All components extend the Component protocol"
  (= 123456 (get-entity-id ((make-comp MyComp 1 2) 123456))) => true)
  
