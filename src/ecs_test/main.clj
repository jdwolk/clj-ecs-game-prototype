(ns ecs-test.main
  (:use (ecs-test core)
        (ecs-test.systems rendering movement core)
        [seesaw.core]
        [seesaw.graphics]
        [seesaw.color]
        [seesaw.applet])
  (:import (ecs-test.systems.rendering.Visual)
           (ecs-test.systems.movement.Position)
           (ecs-test.systems.movement.Direction)
           (ecs-test.systems.movement.Velocity)
           [javax.swing ImageIcon] ;XXX remove?
           [java.awt.event KeyEvent]))

(def first-entity 
  (ref (make-entity (make-comp Position 10 300 0)
                    (make-comp Direction :S)
                    (make-comp Velocity 0)
                    (make-comp Visual "player_down"))))

(defn paint-world [c g]
  (dosync
    (let [an-ent (alter first-entity
                         (fn [e] (assoc-comp e (apply-compfn delta-loc e))))
          new-ent (alter first-entity
                         (fn [e] (assoc-comp e (apply-compfn direction-img e))))
          new-pos (get-comp @first-entity :Position)
          new-vis (get-comp @first-entity :Visual)
          new-img (lookup-img new-vis)]
    ;(println "new img"  new-img)
    ;(println "new pos"  new-pos)
    (push g
      (draw g (image-shape (:x new-pos) (:y new-pos) new-img)
              (style :background (color 224 0 0 128)))))))
      
;TODO carryover fns from initial prototype
(defn key-dispatch [e]
  (dosync
  (alter first-entity assoc-comp (make-comp Velocity 5))
  (case (KeyEvent/getKeyText (.getKeyCode e))
    "Down" (alter first-entity assoc-comp (make-comp Direction :S))
    "Up"   (alter first-entity assoc-comp (make-comp Direction :N))
    "Left" (alter first-entity assoc-comp (make-comp Direction :W))
    "Right" (alter first-entity assoc-comp (make-comp Direction :E))
    :else  (println "SOMETHING was pressed"))))

(defn zero-velocity [e]
  (dosync
    (alter first-entity assoc-comp (make-comp Velocity 0))))

; Thread-local (i.e. tied to Swing thread)
(comment
(def screen (canvas :id :gamescreen
                    :paint paint-world
                    :size [350 :by 350]))
)

(defn setup-frame []
  (println "Setting up frame!")
  (let [screen (canvas 
                    :id :gamescreen
                    :paint paint-world
                    :size [350 :by 350])
        t (timer (fn [e] (repaint! screen)) :delay 60)
        f (frame :title "My Game"
                 :size [350 :by 350]
                 :content screen)]
    (native!)
    (listen f :key-pressed  key-dispatch
              :key-released zero-velocity)
    (-> f pack! show!)))

(setup-frame)

; Taken from Rich Hickey's ants demo
(comment
(def animator (agent nil))
(def animation-sleep-ms 100)

(defn animation [x]
  (when running
    (send-off *agent* #'animation))
  (. screen (repaint))
  (. Thread (sleep animation-sleep-ms))
  nil)

(defn game-loop []
  (setup-frame)
  (send-off animator animation))
)

