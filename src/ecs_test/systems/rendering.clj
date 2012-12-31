(ns ecs-test.systems.rendering
  (:require [ecs-test.core :refer [defcomponent make-comp]]
            [ecs-test.utils.assetmgr :refer [load-images asset-content]]))

;(println "ASSET_BASE from rendering: " ASSET_BASE)
;(println "SPRITES_DIR from rendering: " SPRITES_DIR)

;(load-img "player_down")

(def img-map (load-images ["player_down" "player_up" "player_left" "player_right"]))

(defcomponent Visual [img-name])

;TODO better names
(def -position-img {:N (make-comp Visual "player_down")
                   :S (make-comp Visual "player_up")
                   :E (make-comp Visual "player_right")
                   :W (make-comp Visual "player_left")})

(defn position-img [{pos :Position}]
  (-position-img pos))

(defn lookup-img [vis & {images :img-map :or {images img-map}}]
  " Visual -> asset-content (i.e. ImageIcon)
    Looks up Visual component image in img-map (default to one above)"
  (asset-content ((:img-name vis) images)))

