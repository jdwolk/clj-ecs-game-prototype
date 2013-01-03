(ns ecs-test.utils.assetmgr
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

(def SPRITES_DIR (pathjoin "assets" "sprites"))
)

(defprotocol Asset
  (asset-name [this]) ; :keyword representing the asset
  (path-to [this])    ;  path on disk to the asset
  (asset-content [this])) ; the in-memory content of the actual asset

(defn safe-load-img [imgpath]
  "Loads a standard images (notfound.png) if 
   the image at imgpath is not found" 
  (println "Trying to load " imgpath)
  (try
    (ImageIcon. (clojure.java.io/resource imgpath))
  (catch NullPointerException npe
    (ImageIcon. (clojure.java.io/resource "notfound.png")))))

(defn load-img [name]
  ;TODO perform dir walk to find file
  (let [path-to-asset (str name ".png")
        content (safe-load-img path-to-asset)]
    (println "Asset: " path-to-asset)
    (reify Asset
      (asset-name [_] name)
      (path-to    [_] path-to-asset)
      (asset-content [this] content))))

(defn load-images [names]
  (zipmap (map keyword names) (map load-img names)))

