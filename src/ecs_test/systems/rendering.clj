(ns ecs-test.systems.rendering
  (:require [ecs-test.core :refer [defcomponent make-comp get-ent-id]]
            [ecs-test.utils.assetmgr :refer [load-images asset-content load-manifest]]
            [ecs-test.utils.logger :refer [log]]))

(defcomponent Visual [img-name])

(def img-map (ref {}))
(def dir-map (ref {}))

(defn something-else [all-img-map ent-type render-info]
  (log :debug :rendering "(something-else): Creating new img-map")
  (let [img-map (load-images (:images render-info))]
    (log :debug2 :rendering "img-map: " img-map)
    (merge all-img-map img-map)))

(defn something-else-2 [all-dir-maps ent-type render-info]
  (log :debug :rendering "(something-else-2): Creating new dir-map")
  (let [dir-map (:dir-map render-info)]
    (log :debug2 :rendering "dir-map: " dir-map)
    (assoc all-dir-maps ent-type dir-map)))

(defn something 
  ;[man-file img-map dir-map]
  [man-file]
  (let [contents (seq (asset-content (load-manifest man-file)))
       [ent-type system-contents] (first contents)
        render-info (:rendering system-contents)]
    ;TODO handle multiple config sections!
    ;(doseq [[ent-type system-contents] contents] 
    ;{:img-map (something-else img-map ent-type render-info) ;img-map
    ; :dir-map (something-else-2 dir-map ent-type render-info) })) ;dir-map
        (dosync (alter img-map something-else ent-type render-info)
        (dosync (alter dir-map something-else-2 ent-type render-info)))
        (log :debug2 :rendering "Contents of img-map: " @img-map)
        (log :debug2 :rendering "Contents of dir-map: " @dir-map)))

;(def lookup-direction-img {:N (make-comp Visual :player_up)
;                           :S (make-comp Visual :player_down)
;                           :E (make-comp Visual :player_right)
;                           :W (make-comp Visual :player_left)})


;;;;;;;;;;; Component fns ;;;;;;;;;;;;;

;(defn direction-img [{et :EntType dir :Direction} dir-map]
(defn direction-img [{et :EntType dir :Direction}]
  "Direction -> Visual -> Visual
   Given Direction and Visual components, returns a new
   Visual component (ie. the asset that represents that entity
   turned in that direction)"
  (log :debug2 :rendering (str "(direction-img): lookup " (:dir dir) " in dir map: " @dir-map))
  ;TODO animation! Don't just take (first)
  (make-comp Visual (first ((:dir dir) ((:ent-type et) @dir-map)))))

;XXX for some reason, the current way is about 2x faster than
;    calling w/ compfn and [{vis :Visual} & {images ...}]
;(defn lookup-img [vis img-map]
(defn lookup-img [vis]
  " Visual -> asset-content (i.e. ImageIcon)
    Looks up Visual component image in img-map (default to one above)"
  (let [img-name (keyword (:img-name vis))]
    (log :debug2 :rendering "(lookup-img): Looking up " img-name " in img map: " @img-map)
    (asset-content (img-name @img-map))))

