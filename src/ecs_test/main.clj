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

;TODO Need a better way to handle config settings
(def screen-width 350)
(def screen-height 350)

;TODO move this somewhere else!!!
;XXX THIS IS HORRID TEST CODE
(def npcs (ref {}))

;XXX Don't like this...
; its a horrible hack
(defn assoc-npc-in-pool [npc]
  (if npc
    (do
      ;(println "***** Associating NPC " (get-ent-id npc))
      (alter npcs assoc (get-ent-id npc) npc))))

(set! *warn-on-reflection* true)

(defn make-body [x y dir & {vis :visual :or
                           {vis :player_down}}]
  (make-entity (make-comp Position x y 0)
               (make-comp Direction dir)
               (make-comp Velocity 0)
               (make-comp Visual vis)))

(defn make-npc []
  (make-entity (make-comp Position (rand-int screen-width) 
                                   (rand-int screen-height) 0)
               (rand-direction)
               (rand-velocity 5)
               (make-comp Visual :player_up)))
               ;(make-comp Behavior :random-movement)))

(def first-entity 
  (ref (make-entity (make-comp Position 10 300 0)
                    (make-comp Direction :S)
                    (make-comp Velocity 0)
                    (make-comp Visual :player_down))))

(defn draw-entity [#^SunGraphics2D g ent]
  (let [pos (get-comp ent :Position)
        img (lookup-img (get-comp ent :Visual))]
    ;(println "Drawing " (get-ent-id ent))
    ;(println "Position: " pos)
    ;(println "Vis: " img "\n\n")
    (draw g (image-shape (:x pos) (:y pos) img)
          (style :background (color 224 0 0 128)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;XXX horrible, horrible. I'm going to hell.
(defn rand-move [npc]
  ;(println "Moving NPC " (get-ent-id npc))
  (if (= (rand-int 15) 0) ; should move?
      (let [{npc-pos :Position npc-vis :Visual} 
            (apply-compfn something npc)]
      (assoc-comp (assoc-comp npc npc-pos) npc-vis))))

(def move-agent (agent nil))

(defn mover [x]
  (send-off *agent* #'mover)
  (doseq [npc (vals @npcs)] 
    (let [moved-npc (rand-move npc)]
      ;(println "NPC " (get-ent-id npc) " moved")
      (dosync
        (assoc-npc-in-pool moved-npc))))
  (. Thread (sleep 100)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   
(defn paint-world [#^JPanel c #^SunGraphics2D g]
  (dosync
    (let [an-ent (alter first-entity
                         (fn [e] (assoc-comp e (apply-compfn delta-loc e))))
          new-ent (alter first-entity
                         (fn [e] (assoc-comp e (apply-compfn direction-img e))))
          all-ents (cons new-ent (vals @npcs))]
        (doseq [e all-ents]
          (push g
            (draw-entity g e))))))
     
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
                    :size [screen-width :by screen-height]))

(defn setup-frame [screen]
  (println "Setting up frame!")
  (let [t (timer (fn [e] (repaint! screen)) :delay 60)
        f (frame :title "My Game"
                 :size [screen-width :by screen-height]
                 :content screen
                 :on-close :dispose)]
    (native!)
    (listen f :key-pressed  key-dispatch
              :key-released key-up)
    (-> f pack! show!)))

(defn -main []
  (println "in -main")
  (dosync
    (dotimes [_ 8]
      (assoc-npc-in-pool (make-npc))))
  (send-off move-agent mover)
  (setup-frame screen))

