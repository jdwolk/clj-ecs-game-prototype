(ns ecs-test.systems.rendering
  (:require [ecs-test.core :refer [defcomponent make-comp get-ent-id]]
            [ecs-test.utils.assetmgr :refer [load-images asset-content load-manifest]]
            [ecs-test.utils.logger :refer [log]]))

;(load-manifest "entities/basicnpc")
;(def img-map (load-images ["player_down" "player_up" "player_left" "player_right"]))

(defcomponent Visual [img-name])

(def img-map (ref {}))
(def dir-map (ref {}))

(defn something-else [all-img-map ent-type render-info]
  (let [img-map (load-images (:images render-info))]
    (merge all-img-map img-map)))
    ;(assoc all-img-map ent-type img-map)))

(defn something-else-2 [all-dir-maps ent-type render-info]
  (let [dir-map (:dir-map render-info)]
        ;XXX can't take value of a macro
        ;as-comps (zipmap (keys dir-map)
        ;                 (map (partial make-comp Visual) (vals dir-map)))]
    ;(assoc all-dir-maps ent-type as-comps)))
    (assoc all-dir-maps ent-type dir-map)))

(defn something 
  [man-file]
  (let [contents (seq (asset-content (load-manifest man-file)))]
    (doseq [[ent-type system-contents] contents]
      (let [render-info (:rendering system-contents)]
        (dosync (alter img-map something-else ent-type render-info)
        (dosync (alter dir-map something-else-2 ent-type render-info)))
        (log :debug2 :rendering "Contents of img-map: " @img-map)
        (log :debug2 :rendering "Contents of dir-map: " @dir-map)))))

;(def lookup-direction-img {:N (make-comp Visual :player_up)
;                           :S (make-comp Visual :player_down)
;                           :E (make-comp Visual :player_right)
;                           :W (make-comp Visual :player_left)})


;;;;;;;;;;; Component fns ;;;;;;;;;;;;;

;TODO need a new kind of component...using Visual for this seems wrong
; maybe something like EntityType? or CurrentState? Something
; that represents not only the entity's type, but also, i.e., what
; weapons/aromor it has that determine the appearance
(defn direction-img [{et :EntType dir :Direction} &
                     {dir-mappings :dir-map :or {dir-mappings @dir-map}}]
  "Direction -> Visual -> Visual
   Given Direction and Visual components, returns a new
   Visual component (ie. the asset that represents that entity
   turned in that direction)"
  ;(lookup-direction-img (:dir dir)))
  (make-comp Visual ((:dir dir) ((:ent-type et) dir-mappings))))

;XXX for some reason, the current way is about 2x faster than
;    calling w/ compfn and [{vis :Visual} & {images ...}]
(defn lookup-img [vis & {images :img-map :or {images @img-map}}]
  " Visual -> asset-content (i.e. ImageIcon)
    Looks up Visual component image in img-map (default to one above)"
  (println "Lookup img - images: " images)
  (asset-content ((keyword (:img-name vis)) images)))
                  

