(ns ecs-test.systems.rendering
  (:require [ecs-test.core            :refer [defcomponent make-comp]]
            [ecs-test.utils.assetmgr  :refer [load-images asset-content asset-name load-manifest filename->keyword get-system]]
            [ecs-test.utils.misc      :refer [wrap-inc]]
            [ecs-test.utils.logger    :refer [log]]
            [clojure.set              :refer [union]]))

(defcomponent Visual [curr-frame frames])

(def img-map (ref {}))
(def dir-map (ref {}))

(defn get-dir-imgs [entity-manifest]
  (:dir-imgs (get-system entity-manifest :rendering)))

(defn image-union [dir-img-map]
  (map name (apply union (vals dir-img-map))))

(defn assoc-ent-dirs [all-dir-imgs ent-type dir-imgs]
  (let [new-map (assoc all-dir-imgs ent-type dir-imgs)]
    (log :debug2 :rendering "Contents of new dir-map: " new-map)
    new-map))

(defn assoc-ent-imgs [all-imgs dir-imgs]
  (let [new-map (merge all-imgs (load-images (image-union dir-imgs)))]
    (log :debug2 :rendering "Contents of new img-map: " new-map)
    new-map))

(defn load-resources!
  [man-file]
  (let [entity-manifest (load-manifest man-file)
        manifest-content (asset-content entity-manifest)
        ent-type (filename->keyword (asset-name entity-manifest))
        dir-imgs (get-dir-imgs manifest-content)]
    (dosync (alter dir-map assoc-ent-dirs ent-type dir-imgs))
    (dosync (alter img-map assoc-ent-imgs dir-imgs))))

(defn lookup-frame [n vis]
  (nth (:frames vis) n))

(defn lookup-curr-frame [vis]
  (lookup-frame (:curr-frame vis) vis))

(defn next-frame-vis [vis]
  (let [{:keys [curr-frame frames]} vis]
    (make-comp Visual (wrap-inc curr-frame (count frames)) frames)))

(defn lookup-ent-frames
  ([ent-type]
    (lookup-ent-frames ent-type :N))
  ([ent-type dir]
    (dir (ent-type @dir-map))))

;;;;;;;;;;; Component fns ;;;;;;;;;;;;;

(defn advance-frame [{vis :Visual :as ent}]
  (next-frame-vis vis))

(defn direction-img [{et :EntType dir :Direction vis :Visual vel :Velocity}]
  "Direction -> Visual -> Visual
   Given Direction and Visual components, returns a new
   Visual component (ie. the asset that represents that entity
   turned in that direction)"
  (let [e-type (:ent-type et)
        {curr-dir :dir old-dir :prev-dir} dir
        new-vis (if (or (not (= old-dir curr-dir)) (= 0 (:units vel)))
                    (make-comp Visual 0 (lookup-ent-frames e-type curr-dir))
                    (next-frame-vis vis))]
    (log :debug2 :rendering "(direction-img) old-dir=" old-dir
                             ", dir=" curr-dir
                             ", equal?=" (= old-dir curr-dir)
                             ", curr-frame=" (:curr-frame new-vis))
    new-vis))

;XXX for some reason, the current way is about 2x faster than
;    calling w/ compfn and [{vis :Visual} & {images ...}]
(defn lookup-img [vis]
  " Visual -> asset-content (i.e. ImageIcon)
    Looks up Visual component image in img-map (default to one above)"
  (let [;img-name (keyword (:img-name vis))
        img-name (lookup-curr-frame vis)
        asset (asset-content (img-name @img-map))]
    (log :debug2 :rendering "(lookup-img): Looking up " img-name " in img map: " @img-map)
    (log :debug2 :rendering "(lookup-img): asset-content: " asset)
    asset))

