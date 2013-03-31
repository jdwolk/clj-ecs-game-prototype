(ns ecs-test.main
  (:use (ecs-test core)
        (ecs-test.systems rendering movement core ident ai)
        (seesaw core graphics color))
  (:require [ecs-test.utils.configmgr :refer [with-config config-get load-config]]
            [ecs-test.utils.logger :refer [log]]
            [ecs-test.engine.time :refer [calc-fps]])
  (:import (ecs-test.systems.rendering.Visual)
           (ecs-test.systems.ident.EntType)
           (ecs-test.systems.movement.Position)
           (ecs-test.systems.movement.Direction)
           (ecs-test.systems.movement.Velocity)
           (ecs-test.systems.ai.Behavior)
           (java.awt.event KeyEvent)
           (javax.swing JPanel)       ; for type hinting
           (sun.java2d SunGraphics2D) ; for type hinting
           (java.awt.event WindowListener))
  (:gen-class
    :main main))

(set! *warn-on-reflection* true)

;TODO move this somewhere else!!!
;XXX THIS IS HORRID TEST CODE
(def npcs (ref {}))

;XXX Don't like this...
; its a horrible hack
(defn assoc-npc-in-pool [npc]
  (if npc
    (do
      (alter npcs assoc (get-id npc) npc))))

(defn make-body [x y dir & {vis :visual :or
                           {vis :player_down}}]
  "int -> int -> keyword -> keyword -> Entity"
  (make-entity (make-comp EntType :player)
               (make-comp Position x y 0)
               (make-comp Direction dir nil)
               (make-comp Velocity 0)
               (make-comp Visual 0 [vis])))

;TODO need to make Components more composable.
;TODO refactor make-npc in terms of make-body
(defn make-npc []
  (make-entity (make-comp EntType :basicnpc)
               (rand-pos (config-get [:screen-width])
                         (config-get [:screen-height]))
               (rand-direction nil)
               (rand-velocity 5)
               (make-comp Visual 0 [:npc_up])
               (make-comp Behavior move-toward-player)))

(def player-entity 
  (ref (make-body 10 300 :S)))

;TODO move somewhere else
(defn draw-entity [#^SunGraphics2D g ent]
  (let [pos (get-comp ent :Position)
        img (lookup-img (get-comp ent :Visual))]
    (draw g (image-shape (:x pos) (:y pos) img)
          (style :background (color 224 0 0 128)))))

(defn draw-text [#^SunGraphics2D g words x y size]
                 ;& {s :size x :x y :y 
                 ;:or {s 24 x 20 y 20}}]
  (push g
    (draw g (string-shape x y words)
            (style :foreground (color 0 0 0)
                   :font (str "ARIAL-BOLD-" size)))))

 
(defn paint-world [#^JPanel c #^SunGraphics2D g]
  (log :debug3 :animation-ag "Painting screen")
  (let [start-time (System/nanoTime)
        an-ent  (dosync (alter player-entity
                    (fn [e] (assoc-comps e (compfn delta-loc e)))))
        new-ent (dosync (alter player-entity
                    (fn [e] (assoc-comps e (compfn direction-img e)))))
        all-ents (dosync (cons new-ent (vals @npcs)))]
    (log :debug3 :main "Entities: " (count all-ents))
    (doseq [e all-ents]
      (push g
        (dosync (draw-entity g e))))
    ;Draw FPS  TODO move this elsewhere
    (draw-text g (str (calc-fps start-time (System/nanoTime)) " fps")
               20 20 15)
    (let [pos (get-comp @player-entity :Position)]
      (draw-text g (str "x: " (:x pos)) 20 35 15)
      (draw-text g (str "y: " (:y pos)) 20 50 15))))
        
;TODO carryover fns from initial prototype
(defn key-dispatch [#^KeyEvent e]
  (dosync
  (alter player-entity assoc-comps (make-comp Velocity 5))
  (case (KeyEvent/getKeyText (.getKeyCode e))
    "Down" (alter player-entity assoc-comps (make-comp Direction :S (:dir (get-comp @player-entity :Direction))))
    "Up"   (alter player-entity assoc-comps (make-comp Direction :N (:dir (get-comp @player-entity :Direction))))
    "Left" (alter player-entity assoc-comps (make-comp Direction :W (:dir (get-comp @player-entity :Direction))))
    "Right" (alter player-entity assoc-comps (make-comp Direction :E (:dir (get-comp @player-entity :Direction))))
    :else  (log :error :MAIN "SOMETHING was pressed"))))

(defn key-up [#^KeyEvent e]
  (dosync
      (alter player-entity assoc-comps (zero-velocity))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def mover (agent nil :error-handler (fn [ag err] (log :error :mover (.printStackTrace err)))))

(defn movement [x]
  (send-off mover #'movement)
  (log :debug3 :move-ag "Moving entities")
  (doseq [npc (vals @npcs)] 
    ;(let [new-comps (compfn move-toward-player @player-entity npc)
    (let [new-comps (act npc @player-entity)
          moved-npc (apply assoc-comps npc (vals new-comps))]
      (log :debug3 :main "Moved npc: " moved-npc)
      (dosync (assoc-npc-in-pool moved-npc))))
  (. Thread (sleep 100))
  x) ;XXX this is bad and arbitrary

;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Must initialize to screen before sending off animation
(def animator (agent nil :error-handler (fn [ag err] (log :error :animator (.printStackTrack err)))))

(defn animation [#^JPanel s]
  (send-off animator #'animation)
  (log :debug3 :anim-ag "Painting screen")
  (. s (repaint))
  (. Thread (sleep 100))
  s)

(defn kill-agent-loop [an-agent]
  (await (send-off an-agent (fn [_] nil)))
  (log :debug :main "Killed agent loop " an-agent))

;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn setup-screen [width height]
            (canvas :id :gamescreen
                    :paint paint-world
                    :size [width :by height]))

(defn kill-agents-on-close [agents]
  (reify WindowListener
    (windowActivated [this e])
    (windowClosing [this e]
      (doseq [a agents] (kill-agent-loop a)))
    (windowDeactivated [this e])
    (windowDeiconified [this e])
    (windowIconified [this e])
    (windowOpened [this e])
    (windowClosed [this e])))

(defn setup-frame []
  (log :info :main "Setting up frame!")
  (let [f (frame :title "My Game"
                 :content @animator
                 :on-close :exit)]
    (native!)
    (listen f :key-pressed  key-dispatch
              :key-released key-up)
    (.addWindowListener f (kill-agents-on-close [mover animator]))
    (-> f pack! show!)))

(defn -main []
  (log :info :main "Starting game")
  (load-entity! "entities/characters/basicnpc")
  (load-entity! "entities/characters/player")
  (load-entity! "entities/effects/explosion")
  (with-config (load-config "config.clj")
    (dosync
      (dotimes [_ 10]
        (assoc-npc-in-pool (make-npc))))
    (send-off animator 
      (fn [_] (setup-screen (config-get [:screen-width])
                            (config-get [:screen-height]))))
    (send-off animator animation)
    (send-off mover movement)
    (await-for 200 animator)
    (setup-frame)))

