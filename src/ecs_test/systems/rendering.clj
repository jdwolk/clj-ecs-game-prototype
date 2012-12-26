(ns ecs-test.systems.rendering
  (:use (ecs-test.utils.assetmgr)
        (ecs-test.core)))

(def img-map (load-images ["player_down" "player_up" "player_left" "player_right"]))

(defcomponent Visual [img-name])

(defn lookup-img [vis & {images :img-map :or {images img-map}}]
  " Visual -> asset-content (i.e. ImageIcon)
    Looks up Visual component image in img-map (default to one above)"
  (asset-content ((:img-name vis) images)))

