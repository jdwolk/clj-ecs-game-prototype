(ns ecs-test.test.systems.ident
  (:use (ecs-test.systems ident)
        (ecs-test core)
        (midje sweet)))

(fact "Identity is expressed through EntType component"
  (:ent-type (make-comp EntType :awesome)) => :awesome)
