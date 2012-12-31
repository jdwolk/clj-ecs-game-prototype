(ns ecs-test.utils.assetmgr
  (:import [javax.swing ImageIcon]
           [java.io File]
           [java.net URI]))

(defn normpath [path]
  (-> (File. path)
      .getAbsolutePath
      URI.
      .normalize
      .getPath))

(defn pathjoin [& paths]
    (normpath (apply str (interpose File/separator paths))))

(def ASSET_BASE (pathjoin *file* ".." ".." ".." "assets"))
(def SPRITES_DIR (pathjoin ASSET_BASE "sprites"))

(defprotocol Asset
  (asset-name [this]) ; :keyword representing the asset
  (path-to [this])    ;  path on disk to the asset
  (asset-content [this])) ; the in-memory content of the actual asset

(defn safe-load-img [imgpath]
  "Loads a standard images (notfound.png) if 
   the image at imgpath is not found" 
  (try
    ;(ImageIcon. (clojure.java.io/resource imgpath))
    (ImageIcon. imgpath)
  (catch NullPointerException npe
    ;(ImageIcon. (clojure.java.io/resource 
    ;              (pathjoin SPRITES_DIR "notfound.png"))))))
    (ImageIcon. (pathjoin SPRITES_DIR "notfound.png")))))

(defn load-img [name]
  ;TODO perform dir walk to find file
  (let [path-to-asset (pathjoin SPRITES_DIR (str name ".png"))
        content (safe-load-img path-to-asset)]
    (println "Asset: " path-to-asset)
    (reify Asset
      (asset-name [_] name)
      (path-to    [_] path-to-asset)
      (asset-content [this] content))))

(defn load-images [names]
  (zipmap names (map load-img names)))

