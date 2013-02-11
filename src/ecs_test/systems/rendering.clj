(ns ecs-test.systems.rendering
  (:require [ecs-test.core            :refer [defcomponent make-comp]]
            [ecs-test.utils.assetmgr  :refer [load-images asset-content asset-name load-manifest filename->keyword get-system]]
            [ecs-test.utils.logger    :refer [log]]
            [clojure.set              :refer [union]]))

(defcomponent Visual [img-name])

(def img-map (ref {}))
(def dir-map (ref {}))

(defn get-dir-imgs [entity-manifest]
  (:dir-imgs (get-system entity-manifest :rendering)))

(defn image-union [dir-img-map]
  (map name (apply union (vals dir-img-map))))

(defn load-entity!
  [man-file]
  (let [entity-manifest (load-manifest man-file)
        manifest-content (asset-content entity-manifest)
        ent-type (filename->keyword (asset-name entity-manifest))
        dir-imgs (get-dir-imgs manifest-content)]
    (dosync (alter dir-map assoc ent-type dir-imgs))
    (dosync (alter img-map merge (load-images (image-union dir-imgs))))
    (log :debug2 :rendering "Contents of img-map: " @img-map)
    (log :debug2 :rendering "Contents of dir-map: " @dir-map)))

;;;;;;;;;;; Component fns ;;;;;;;;;;;;;

(defn direction-img [{et :EntType dir :Direction}]
  "Direction -> Visual -> Visual
   Given Direction and Visual components, returns a new
   Visual component (ie. the asset that represents that entity
   turned in that direction)"
  (let [t (:ent-type et), d (:dir dir)]
    (log :debug2 :rendering (str "(direction-img): lookup " d " of " t " in dir map: " @dir-map))
  ;TODO animation! Don't just take (first)
    (make-comp Visual (first (d (t @dir-map))))))

;XXX for some reason, the current way is about 2x faster than
;    calling w/ compfn and [{vis :Visual} & {images ...}]
(defn lookup-img [vis]
  " Visual -> asset-content (i.e. ImageIcon)
    Looks up Visual component image in img-map (default to one above)"
  (let [img-name (keyword (:img-name vis))
        asset (asset-content (img-name @img-map))]
    (log :debug2 :rendering "(lookup-img): Looking up " img-name " in img map: " @img-map)
    (log :debug2 :rendering "(lookup-img): asset-content: " asset)
    asset))

