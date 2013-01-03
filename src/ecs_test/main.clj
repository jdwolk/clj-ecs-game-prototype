(ns ecs-test.main
  (:use (ecs-test core)
        (ecs-test.systems rendering movement core)
        [seesaw.core]
        [seesaw.graphics]
        [seesaw.color])
  (:import (ecs-test.systems.rendering.Visual)
           (ecs-test.systems.movement.Position)
           (ecs-test.systems.movement.Direction)
           (ecs-test.systems.movement.Velocity)
           [java.awt.event KeyEvent]
           [javax.swing JPanel]        ; for type hinting
           [sun.java2d SunGraphics2D]) ; for type hinting
  (:gen-class
    :main main))
    ;:post-init post-init 
    ;:extends javax.swing.JApplet))

(set! *warn-on-reflection* true)

(def first-entity 
  (ref (make-entity (make-comp Position 10 300 0)
                    (make-comp Direction :S)
                    (make-comp Velocity 0)
                    (make-comp Visual "player_down"))))

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
              (style :background (color 224 0 0 128)))))))
      
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

(defn zero-velocity [e]
  (dosync
    (alter first-entity assoc-comp (make-comp Velocity 0))))

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
              :key-released zero-velocity)
    (-> f pack! show!)))

(defn -main []
  (println "in -main")
  (setup-frame screen))

(comment
(defn setup-panel [#^JPanel screen]
  (let [t (timer (fn [e] (repaint! screen)) :delay 60)
        p (border-panel :center screen
                        :focusable? true)]
    (listen p :key-pressed  key-dispatch
              :key-released zero-velocity)
    p))

(defn -post-init [#^ecs_test.main this]
  (println "In -post-init")
  (let [panel (setup-panel screen)]
    (.setContentPane this panel)
    ))

(defapplet :content (setup-panel screen)
           :init (fn [_] (println "Initialized"))
           :start (fn [_] (println "Started")))
)

