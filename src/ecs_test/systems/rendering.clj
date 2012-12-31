(ns ecs-test.systems.rendering
  (:require [ecs-test.core :refer [defcomponent make-comp assoc-entity-id get-ent-id]]
            [ecs-test.utils.assetmgr :refer [load-images asset-content]]))

(def img-map (load-images ["player_down" "player_up" "player_left" "player_right"]))

(defcomponent Visual [img-name])

;TODO better names
(def lookup-direction-img {:N (make-comp Visual :player_up)
                           :S (make-comp Visual :player_down)
                           :E (make-comp Visual :player_right)
                           :W (make-comp Visual :player_left)})


;;;;;;;;;;; Component fns ;;;;;;;;;;;;;

;TODO need a new kind of component...using Visual for this seems wrong
; maybe something like EntityType? or CurrentState? Something
; that represents not only the entity's type, but also, i.e., what
; weapons/aromor it has that determine the appearance
(defn direction-img [{dir :Direction vis :Visual}]
  "Direction -> Visual -> Visual
   Given Direction and Visual components, returns a new
   Visual component (ie. the asset that represents that entity
   turned in that direction)"
  ;TODO do something w/ visual component
  (assoc-entity-id (:entity-id vis)
                   (lookup-direction-img (:dir dir))))

(defn lookup-img [vis & {images :img-map :or {images img-map}}]
  " Visual -> asset-content (i.e. ImageIcon)
    Looks up Visual component image in img-map (default to one above)"
  (asset-content ((:img-name vis) images)))

