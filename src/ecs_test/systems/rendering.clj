(ns ecs-test.systems.rendering)

(comment
  (:use ecs-test.core
        ecs-test.components)
  (:import ecs_test.components.Visual
           [javax.swing ImageIcon])

; asset-loader/get-img?
; also, use sprite sheet coodinates instead?
(defn load-img [img-name]
  (ImageIcon. (clojure.java.io/resource (str "assets/sprites/" img-name ".png"))))

(def img-map {:player-down (load-img "player-down")})


(defn get-img [img] (img img-map))

(defn paint-entity [ent]
  (let [vis-comp "hi"]
  (get-img (img vis-comp))))
)
