(ns ecs-test.systems.rendering
  (:require [ecs-test.core            :refer [defcomponent make-comp]]
            [ecs-test.utils.assetmgr  :refer [load-images asset-content load-manifest]]
            [ecs-test.utils.logger    :refer [log]]))

(defcomponent Visual [img-name])

(def img-map (ref {}))
(def dir-map (ref {}))

(defn assoc-ent-imgs [all-img-map ent-type render-info]
  (log :debug :rendering "(something-else): Creating new img-map")
  (let [img-map (load-images (:images render-info))]
    (log :debug2 :rendering "img-map: " img-map)
    (merge all-img-map img-map)))

(defn assoc-ent-dirs [all-dir-maps ent-type render-info]
  (log :debug :rendering "(something-else-2): Creating new dir-map")
  (let [dir-map (:dir-map render-info)]
    (log :debug2 :rendering "dir-map: " dir-map)
    (assoc all-dir-maps ent-type dir-map)))

(defn load-entity!
  "Loads entity manifest into img-map and dir-map refs"
  [man-file]
  (let [contents (seq (asset-content (load-manifest man-file)))
       [ent-type system-contents] (first contents)
        render-info (:rendering system-contents)]
        ;TODO handle multiple config sections!
        (dosync (alter img-map assoc-ent-imgs ent-type render-info)
        (dosync (alter dir-map assoc-ent-dirs ent-type render-info)))
        (log :debug2 :rendering "Contents of img-map: " @img-map)
        (log :debug2 :rendering "Contents of dir-map: " @dir-map)))

;;;;;;;;;;; Component fns ;;;;;;;;;;;;;

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
(defn lookup-img [vis]
  " Visual -> asset-content (i.e. ImageIcon)
    Looks up Visual component image in img-map (default to one above)"
  (let [img-name (keyword (:img-name vis))]
    (log :debug2 :rendering "(lookup-img): Looking up " img-name " in img map: " @img-map)
    (asset-content (img-name @img-map))))

