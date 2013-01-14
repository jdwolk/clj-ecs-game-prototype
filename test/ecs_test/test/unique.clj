(ns ecs-test.test.unique
  (:use (ecs-test unique)
        (midje sweet)))

(defunique MyComp [a b])
  
(fact "unique records have an :id field in their meta info"
    (not (nil? (:id (meta (make-unique MyComp "hello" "there"))))) => true)

(fact "unique records extend the Unique protocol"
  (not (nil? (get-id (make-unique MyComp "hello" "there")))) => true)

(fact "component ids have unique values"
  (= (get-id (make-unique MyComp "hello" "there"))
     (get-id (make-unique MyComp "hello" "there"))) => false)

;;XXX this shouldn't work...don't want (defcomponent)s to pollute tests
(println (make-unique MyComp "hi" "there"))


