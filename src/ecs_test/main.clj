(ns ecs-test.main
  (:use (ecs-test core)
        (ecs-test.systems rendering movement core ai)
        (seesaw core graphics color))
  (:require [ecs-test.utils.configmgr :refer [load-config]])
  (:import (ecs-test.systems.rendering.Visual)
           (ecs-test.systems.movement.Position)
           (ecs-test.systems.movement.Direction)
           (ecs-test.systems.movement.Velocity)
           (ecs-test.systems.ai.Behavior)
           (java.awt.event KeyEvent)
           (javax.swing JPanel)        ; for type hinting
           (sun.java2d SunGraphics2D) ; for type hinting
           (java.util.concurrent TimeUnit)) ;XXX move this
  (:gen-class
    :main main))

(set! *warn-on-reflection* true)

; Will be initialized in -main
(def ^:dynamic *CONFIG*)

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


(defn make-body [x y dir & {vis :visual :or
                           {vis :player_down}}]
  (make-entity (make-comp Position x y 0)
               (make-comp Direction dir)
               (make-comp Velocity 0)
               (make-comp Visual vis)))

(defn make-npc []
  (make-entity (make-comp Position 
                          (rand-int (get-in *CONFIG* [:screen-width]) )
                          (rand-int (get-in *CONFIG* [:screen-height]))
                          0)
               (rand-direction)
               (rand-velocity 5)
               (make-comp Visual :player_up)))
               ;(make-comp Behavior :random-movement)))

(def player-entity 
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

;TODO find somewhere else for this
(defn draw-text [#^SunGraphics2D g words x y s]
                 ;& {s :size x :x y :y 
                 ;:or {s 24 x 20 y 20}}]
  (push g
    (draw g (string-shape x y words)
            (style :foreground (color 0 0 0)
                   :font (str "ARIAL-BOLD-" s)))))
     

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;XXX horrible, horrible. I'm going to hell.
(defn randomly-move [npc]
  ;(println "Moving NPC " (get-ent-id npc))
  ;(if (= (rand-int 15) 0) ; should move?
      (let [{npc-pos :Position npc-vis :Visual}
            (apply-compfn move-toward-player @player-entity npc)]
            ;(apply-compfn make-rand-move npc)]
      (assoc-comp (assoc-comp npc npc-pos) npc-vis)))

(def move-agent (agent nil))

(defn mover [x]
  (send-off *agent* #'mover)
  (doseq [npc (vals @npcs)] 
    (let [moved-npc (randomly-move npc)]
      ;(println "NPC " (get-ent-id npc) " moved")
      ;(println "NPC " (get-ent-id npc) " pos: " (get-comp :Position npc) "\n")
      (dosync
        (assoc-npc-in-pool moved-npc))))
  (. Thread (sleep 100)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 
;1000000000 nanos in 1 sec
;1 frame/dnanos * nanos / sec = frame / sec
;TODO move to engine/clock.clj
(defn calc-fps [start end]
  (let [dnanos (- end start)
        npsec 1000000000]
    (int (* (/ 1 dnanos) npsec))))

(defn paint-world [#^JPanel c #^SunGraphics2D g]
  (dosync
    (let [start-time (System/nanoTime)
          an-ent (alter player-entity
                         (fn [e] (assoc-comp e (apply-compfn delta-loc e))))
          new-ent (alter player-entity
                         (fn [e] (assoc-comp e (apply-compfn direction-img e))))
          all-ents (cons new-ent (vals @npcs))]
        (doseq [e all-ents]
          (push g
            (draw-entity g e)))
        ;Draw FPS  TODO move this elsewhere
        (draw-text g (str (calc-fps start-time (System/nanoTime)) " fps")
                   20 20 15)
        (let [pos (get-comp @player-entity :Position)]
          (draw-text g (str "x: " (:x pos)) 20 35 15)
          (draw-text g (str "y: " (:y pos)) 20 50 15)))))
        
;TODO carryover fns from initial prototype
(defn key-dispatch [#^KeyEvent e]
  (dosync
  (alter player-entity assoc-comp (make-comp Velocity 5))
  (case (KeyEvent/getKeyText (.getKeyCode e))
    "Down" (alter player-entity assoc-comp (make-comp Direction :S))
    "Up"   (alter player-entity assoc-comp (make-comp Direction :N))
    "Left" (alter player-entity assoc-comp (make-comp Direction :W))
    "Right" (alter player-entity assoc-comp (make-comp Direction :E))
    :else  (println "SOMETHING was pressed"))))

(defn key-up [#^KeyEvent e]
  (dosync
    (alter player-entity assoc-comp (zero-velocity))))

(defn setup-frame []
  (println "Setting up frame!")
  (let [screen (canvas :id :gamescreen
                       :paint paint-world
                       :size [(get-in *CONFIG* [:screen-width]) :by 
                              (get-in *CONFIG* [:screen-height])])
        t (timer (fn [e] (repaint! screen)) :delay 60)
        f (frame :title "My Game"
                 :content screen
                 :on-close :dispose)]
    (native!)
    (listen f :key-pressed  key-dispatch
              :key-released key-up)
    (-> f pack! show!)))

(defn -main []
  (println "in -main")
  (binding [*CONFIG* (load-config "config.clj")]
    (dosync
      (dotimes [_ 10]
        (assoc-npc-in-pool (make-npc))))
    (send-off move-agent mover)
    (setup-frame)))

