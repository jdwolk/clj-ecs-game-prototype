(ns ecs-test.main
  (:use (ecs-test core)
        (ecs-test.systems rendering movement core ai)
        (seesaw core graphics color))
  (:import (ecs-test.systems.rendering.Visual)
           (ecs-test.systems.movement.Position)
           (ecs-test.systems.movement.Direction)
           (ecs-test.systems.movement.Velocity)
           (ecs-test.systems.ai.Behavior)
           (java.awt.event KeyEvent)
           (javax.swing JPanel)        ; for type hinting
           (sun.java2d SunGraphics2D)) ; for type hinting
  (:gen-class
    :main main))

(set! *warn-on-reflection* true)

(def first-entity 
  (ref (make-entity (make-comp Position 10 300 0)
                    (make-comp Direction :S)
                    (make-comp Velocity 0)
                    (make-comp Visual :player_down))))

(def npc-entity
  (ref (make-entity (make-comp Position 10 10 0)
                    (make-comp Direction :E)
                    (make-comp Velocity 0)
                    (make-comp Visual :player_right))))
                    ;(make-comp Behavior :random-walking))))

(defn paint-world [#^JPanel c #^SunGraphics2D g]
  (dosync
    (let [an-ent (alter first-entity
                         (fn [e] (assoc-comp e (apply-compfn delta-loc e))))
          new-ent (alter first-entity
                         (fn [e] (assoc-comp e (apply-compfn direction-img e))))
          new-pos (get-comp @first-entity :Position)
          new-vis (get-comp @first-entity :Visual)
          new-img (lookup-img new-vis)]
    (push g
      (draw g (image-shape (:x new-pos) (:y new-pos) new-img)
              (style :background (color 224 0 0 128)))
      (draw g (image-shape (:x (get-comp @npc-entity :Position))
                           (:y (get-comp @npc-entity :Position))
                           (lookup-img (get-comp @npc-entity :Visual)))
              (style :background (color 224 0 0 128)))))))

;{npc-pos :Position npc-vis :Visual} (apply-compfn something @npc-entity)
;(draw g (circle 150 150 30)
;        (style :background (color 110 10 10))))))
;(alter npc-entity assoc-comp :Position npc-pos)
;(alter npc-entity assoc-comp :Visual npc-vis)

;(draw g (image-shape (:x npc-pos)
;                     (:y npc-pos)
;                     (lookup-img npc-vis))
;        (style :background (color 224 0 0 128)))))))
     
     
;TODO carryover fns from initial prototype
(defn key-dispatch [#^KeyEvent e]
  (dosync
  (alter first-entity assoc-comp (make-comp Velocity 5))
  (case (KeyEvent/getKeyText (.getKeyCode e))
    "Down" (alter first-entity assoc-comp (make-comp Direction :S))
    "Up"   (alter first-entity assoc-comp (make-comp Direction :N))
    "Left" (alter first-entity assoc-comp (make-comp Direction :W))
    "Right" (alter first-entity assoc-comp (make-comp Direction :E))
    :else  (println "SOMETHING was pressed"))))

(defn key-up [#^KeyEvent e]
  (dosync
    (alter first-entity assoc-comp (zero-velocity))))

(def screen (canvas :id :gamescreen
                    :paint paint-world
                    :size [350 :by 350]))

(defn setup-frame [screen]
  (println "Setting up frame!")
  (let [t (timer (fn [e] (repaint! screen)) :delay 60)
        f (frame :title "My Game"
                 :size [350 :by 350]
                 :content screen
                 :on-close :dispose)]
    (native!)
    (listen f :key-pressed  key-dispatch
              :key-released key-up)
    (-> f pack! show!)))

(defn -main []
  (println "in -main")
  (setup-frame screen))

