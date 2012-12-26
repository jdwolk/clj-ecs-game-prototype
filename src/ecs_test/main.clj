(ns ecs-test.main
  (:use (ecs-test core)
        (ecs-test.systems rendering position))
  (:import '(ecs-test.systems.rendering.Visual)
           '(ecs-test.systems.movement Position Direction)))

(def entities (atom
      { (make-entity (make-comp Position 0 0 0)
                     (make-comp Direction :S
                     (make-comp Visual "player_down"))) }))

(defn paint-world [c g]
  (dosync
    ; apply relevant compfns to all entities
    ))

(defn start-game []
  (let [screen (canvas
                 :id :gamescreen
                 :paint paint-world
                 :size [350 :by 350])
        t      (timer (fn [e] (repaint! screen)) :delay 60)
        f      (frame :title "My Game"
                      :size [350 :by 350]
                      :content screen)]
    (native!)
    (listen f :key-pressed  key-dispatch
              :key-released something)
    (-> f pack! show!)))

