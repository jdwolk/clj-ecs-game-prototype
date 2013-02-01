(ns ecs-test.utils.assetmgr
  (:use (clojure pprint))
  (:require [ecs-test.utils.logger :refer [log]])
  (:import [javax.swing ImageIcon]
           [java.io File]
           [java.net URI]))

(comment
(defn normpath [path]
  (->  (URI. path)
      .normalize
      .getPath))

(defn pathjoin [& paths]
    (normpath (apply str (interpose File/separator paths))))
)

(defprotocol Asset
  (asset-name [this]) ; :keyword representing the asset
  (path-to [this])    ;  path on disk to the asset
  (asset-content [this])) ; the in-memory content of the actual asset

(defn safe-load-img [imgpath]
  "Loads a standard images (notfound.png) if 
   the image at imgpath is not found" 
  (try
    (ImageIcon. (clojure.java.io/resource imgpath))
  (catch NullPointerException npe
    (ImageIcon. (clojure.java.io/resource "notfound.png")))))

(defn load-asset [name path-fn load-fn]
  (let [path (path-fn name)
        content (load-fn path)]
    (log :info :assetmgr "Loading " path)
    (reify Asset
      (asset-name [_] name)
      (path-to [_] path)
      (asset-content [_] content))))

(defn load-img [name]
  (load-asset name (fn [s] (str s ".png")) safe-load-img))

;(load-manifest "src/ecs_test/game/manifests/entities/basicnpc.clj")

(defn load-manifest
  "A manifest is just a description of other assets that
   need to be loaded, i.e. a set of images for depicting an entity,
   a file representing a level's layout, etc"
  [man-file]
  (load-asset man-file identity 
   (fn [f] (let [filename (if (.endsWith f ".clj") f (str f ".clj"))
                 filepath (.getPath (clojure.java.io/resource filename))
                 contents (load-file filepath)]
            (log :debug2 :assetmgr "Filepath: " filepath)
            (log :info :assetmgr (str "Manifest " f " contents:"))
            (pprint contents)
            contents))))

(defn load-images [names]
  (zipmap (map keyword names) (map load-img names)))

;(defprotocol ImageManifest
;  "An ImageManifest is a description of image assets
;   that need to be loaded"
;  (image-names [this])
;  (load-images [this]))

;(defn as-image-manifest
;  "Interprets an already-loaded manifest as an ImageManifest"
;  [manifest]
;  (reify ImageManifest
;    (image-names [_] (:images manifest))
;    (load-images [this]
;      (let [names (image-names this)]
;          (zipmap (map keyword names) (map load-img names))))))

;(def manifest-type-map {:ImageManifest as-image-manifest})

;(defn interpret-manifest [man-file & man-types]
;  (let [contents (asset-content (load-manifest man-file))]
;    (map (fn [t] ((t something-map) contents)) man-types))


