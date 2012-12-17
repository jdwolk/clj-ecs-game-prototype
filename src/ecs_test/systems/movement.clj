(ns ecs-test.systems.movement
  (:use (ecs-test core)))

(defcomponent Position [x y z])  ; z is height above 'ground level'


