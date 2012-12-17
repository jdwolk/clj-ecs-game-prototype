(ns ecs-test.main
  (:use (ecs-test core)))

(comment
  (:import ecs_test.core.Entity
           ecs_test.components.Position
           ecs_test.components.Visual)


(defn main []
  (let [e1 (defentity (Position. 1 2 3)
                      (Visual. :player-down 200 200))]
    (println (str "e1: " (get-components e1)))
    (println (str "comps: " (:comps e1)))
    (println (str "X: " (:x ((:comps e1) "Position"))))
    (println (str "X-position of e1: " (get-component e1 "Position")))
    (println (str "Y-position of e1: " (:y (get-component e1 "Position"))))))

(main)
)
