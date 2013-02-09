(ns ecs-test.utils.assetmgr
(import java.util.jar.JarFile)
  (:require [ecs-test.utils.logger :refer [log]])
  (:import [javax.swing ImageIcon]
           [java.io File]
           [java.net URI]
           [java.util.jar JarFile]))

(defn resource-path 
  "Wrapper around clojure.java.io/resource so
   I can make better mocks that don't hit the filesystem"
  [resource-name]
  (.getPath (clojure.java.io/resource resource-name)))

(defprotocol Asset
  (asset-name [this]) ; :keyword representing the asset
  (path-to [this])    ;  path on disk to the asset
  (asset-content [this])) ; the in-memory content of the actual asset

(defn as-asset [name path-fn load-fn]
  (let [path (path-fn name)
        _    (log :info :assetmgr "Loading asset " path)
        content (load-fn path)]
    (reify Asset
      (asset-name [_] name)
      (path-to [_] path)
      (asset-content [_] content))))

(defn safe-load-img [imgpath]
  "Loads a standard images (notfound.png) if 
   the image at imgpath is not found" 
  (try
    (ImageIcon. (resource-path imgpath))
  (catch NullPointerException npe
    (ImageIcon. (resource-path "notfound.png")))))

(defn load-img [name]
  (as-asset name (fn [s] (str s ".png")) safe-load-img))

; Need to read in resources separately when on file system
; than when in jar file
(defn read-jar-resource [jarpath respath]
  (let [jar (JarFile. jarpath)]
    (slurp (.getInputStream jar (.getJarEntry jar respath)))))

(defn load-jar-file 
  "Loads a resource from a jar and evals it;
   similar to load-file but for jar resources"
  [jar-filepath manfile]
  (if-let [matches (if (= "/" (System/getProperty "file.separator"))
                       (re-find #"file:(.*\.jar).*" jar-filepath)
                       (re-find #"file:/?(.*\.jar).*" jar-filepath))]
    (let [jarpath (second matches)]
      (eval (load-string (read-jar-resource jarpath manfile))))))

(defn load-manifest
  "A manifest is just a description of other assets that
   need to be loaded, i.e. a set of images for depicting an entity,
   a file representing a level's layout, etc"
  [man-file]
  (let [filename (if (.endsWith man-file ".clj")
                     man-file
                    (str man-file ".clj"))
        filepath (resource-path filename)]
    (as-asset filename
      (constantly filepath) 
      (fn [f] (let [contents (or (load-jar-file filepath filename)
                                 (load-file filepath))]
        (log :debug2 :assetmgr "Filepath: " filepath)
        (log :info :assetmgr "Manifest " f " contents:" contents)
        contents)))))

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


