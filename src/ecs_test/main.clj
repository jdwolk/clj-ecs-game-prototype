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
           (sun.java2d SunGraphics2D)) ; for type hinting
  (:gen-class
    :main main))

(set! *warn-on-reflection* true)

;TODO move this somewhere else!!!
;XXX THIS IS HORRID TEST CODE
(def npcs (ref {}))
(def img-map (ref {}))
(def dir-map (ref {}))

;XXX Don't like this...
; its a horrible hack
(defn assoc-npc-in-pool [npc]
  (if npc
    (log :debug3 :main "Altered npc " npc)
    (alter npcs assoc (get-ent-id npc) npc)))

;XXX don't like this either
(defn alter-img-maps [{new-img-map :img-map, new-dir-map :dir-map}]
  (log :debug :main "Altering img-map and dir-map")
  (dosync 
    (alter img-map (fn [_] new-img-map))
    (log :debug2 :main "Contents of img-map: " @img-map))
  (dosync
    (alter dir-map (fn [_] new-dir-map))
    (log :debug2 :main "Contents of dir-map: " @dir-map)))


(defn make-body [x y dir & {vis :visual :or
                           {vis :player_down}}]
  "int -> int -> keyword -> keyword -> Entity"
  (make-entity (make-comp EntType :basicnpc)
               (make-comp Position x y 0)
               (make-comp Direction dir)
               (make-comp Velocity 0)
               (make-comp Visual vis))) 

;TODO need to make Components more composable.
;TODO refactor make-npc in terms of make-body
(defn make-npc []
  (make-entity (make-comp EntType :basicnpc)
               (rand-pos (config-get [:screen-width])
                         (config-get [:screen-height]))
               (rand-direction)
               (rand-velocity 5)
               (make-comp Visual :player_up)))
               ;(make-comp Behavior :random-movement)))

(def player-entity 
  (ref (make-body 10 300 :S)))

;TODO move somewhere else
(defn draw-entity [#^SunGraphics2D g ent]
  (let [pos (get-comp ent :Position)
        img (lookup-img (get-comp ent :Visual) @img-map)]
    (draw g (image-shape (:x pos) (:y pos) img)
          (style :background (color 224 0 0 128)))))

(defn draw-text [#^SunGraphics2D g words x y size]
                 ;& {s :size x :x y :y 
                 ;:or {s 24 x 20 y 20}}]
  (push g
    (draw g (string-shape x y words)
            (style :foreground (color 0 0 0)
                   :font (str "ARIAL-BOLD-" size)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def move-agent (agent nil))

(defn mover [x]
  (send-off *agent* #'mover)
  (doseq [npc (vals @npcs)] 
    (log :debug3 :main "In mover")
   ;(let [moved-npc (apply assoc-comps npc (vals (random-movement npc 15)))]
    (let [new-comps (compfn move-toward-player @player-entity npc @dir-map)
          moved-npc (apply assoc-comps npc (vals new-comps))]
      (dosync
        (assoc-npc-in-pool moved-npc))))
  (. Thread (sleep 100))) ;XXX this is bad and arbitrary
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 

(use 'ecs-test.utils.assetmgr)
(defn paint-world [#^JPanel c #^SunGraphics2D g]
  ;(log :debug3 :main "Painting\nVis: " (:img-name (get-comp @player-entity :Visual)) "\nimg-map:\n" @img-map)
  ;(log :debug3 :main "Asset content: " (lookup-img (get-comp @player-entity :Visual) @img-map))
  (log :debug3 :animation-ag "Painting screen")
  ;(dosync
    (let [start-time (System/nanoTime)
          an-ent  (dosync (alter player-entity
                    (fn [e] (assoc-comps e (compfn delta-loc e)))))
          new-ent (dosync (alter player-entity
                    (fn [e dm] (assoc-comps e (compfn direction-img e dm)))
                    @dir-map))
          all-ents (dosync (cons new-ent (vals @npcs)))]
        (log :debug3 :main "Player ent before: " [an-ent])
        (log :debug3 :main "Player ent after: " [new-ent])
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
    "Down" (do (log :debug2 :main "Pressed down")  (alter player-entity assoc-comps (make-comp Direction :S)))
    "Up"   (alter player-entity assoc-comps (make-comp Direction :N))
    "Left" (alter player-entity assoc-comps (make-comp Direction :W))
    "Right" (alter player-entity assoc-comps (make-comp Direction :E))
    :else  (log :error :MAIN "SOMETHING was pressed"))))

(defn key-up [#^KeyEvent e]
  (dosync
    (alter player-entity assoc-comps (zero-velocity))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;

;(defn setup-screen [width height]
(def screen (canvas :id :gamescreen
                    :paint paint-world
                    :size [650 :by 650])) ;[width :by height]))
          
(def animator (agent screen))

(defn animation [s]
  (send-off animator #'animation)
  (log :debug3 :animation-ag "Painting screen")
  (. s (repaint))
  (. Thread (sleep 100))
  s)

;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn setup-frame []
  (log :info :main "Setting up frame!")
  (let [
        ;screen (canvas :id :gamescreen
        ;               :paint paint-world
        ;               :size [(config-get [:screen-width]) :by 
        ;                      (config-get [:screen-height])])
        ;t (timer (fn [e] (repaint! screen)) :delay 60)
        f (frame :title "My Game"
                 :content screen
                 :on-close :dispose)]
    (native!)
    (listen f :key-pressed  key-dispatch
              :key-released key-up)
    (-> f pack! show!)))


(defn -main []
  (log :info :main "Starting game")
  (alter-img-maps (something "entities/basicnpc" @img-map @dir-map))
  (with-config (load-config "config.clj")
    ;(dosync
    ;  (dotimes [_ 10]
    ;    (assoc-npc-in-pool (make-npc))))
    ;(let [screen (setup-screen (config-get [:screen-width])
    ;                           (config-get [:screen-height]))]
      (send-off animator animation)
      ;(send-off move-agent mover)
      (await-for 200 animator)
      (setup-frame)))

