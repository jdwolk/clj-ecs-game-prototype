(ns ecs-test.test.utils.assetmgr
  (:use (ecs-test.utils assetmgr)
        (midje sweet)))

(fact "load-asset has name, content, and path"
  (let [a (as-asset "fakeasset" 
                      (constantly "path")
                      (constantly "content"))]
    (asset-name a)    => "fakeasset"
    (path-to a)       => "path"
    (asset-content a) => "content"))

(fact "load-img loads .png files"
  (with-redefs-fn {#'ecs-test.utils.assetmgr/safe-load-img 
                   (fn [path] (str "IMAGEICON: " path))}
    #(let [a (load-img "an-image")]
      (asset-name a)    => "an-image"
      (path-to a)       => "an-image.png"
      (asset-content a) => "IMAGEICON: an-image.png")))

(comment
(fact "load-manifest returns an Asset representing a manifest file"
  (let [contents {:fake-ent {:rendering ["hello" "there"]}}]
    (with-redefs-fn {#'ecs-test.utils.assetmgr/resource-path
                      (fn [path] "/some/path/to/manifest.clj")
                     #'load-file 
                      (fn [f] contents)}
      #(let [asset (load-manifest "manifest")]
        (asset-name asset)    => "manifest.clj"
        (path-to asset)       => "/some/path/to/manifest.clj"
        (asset-content asset) => contents))))
)

(fact "load-images gives a map or {:img-name img-asset}"
  (with-redefs-fn {#'ecs-test.utils.assetmgr/as-asset
                   (fn [n _ _] (str "ASSET " n))}
    #(do
       (load-images ["img1"]) =>
          {:img1 "ASSET img1"}
       (load-images ["img1" "img2" "img3"]) =>
          {:img1 "ASSET img1", :img2 "ASSET img2", :img3 "ASSET img3"})))

