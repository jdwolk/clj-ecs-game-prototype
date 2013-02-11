(ns ecs-test.test.systems.rendering
  (:use (ecs-test core)
        (ecs-test.utils assetmgr)
        (ecs-test.systems core rendering)
        (midje sweet))
  (:import (ecs-test.systems.rendering.Visual)))

(fact "assoc-ent-imgs loads and merges images into the global img-map"
  (with-redefs-fn {#'ecs-test.utils.assetmgr/load-images
                   (fn [imgs] {:img1 "IMAGE1", :img2 "IMAGE2"})}
    #(let [img-map {:img3 "IMAGE3"}]
      (assoc-ent-imgs img-map {:images ["img1" "img2"]})
       => {:img1 "IMAGE1", :img2 "IMAGE2", :img3 "IMAGE3"})))

(fact "assoc-ent-dirs associates dir map from render-info w/ ent type"
  (let [dirs {:N [.img1.], :S [.img2.], :E [.img3.], :W [.img4.]}]
    (assoc-ent-dirs {} :newentity {:dir-map dirs})
    => {:newentity dirs}))
        
(fact "load-entity! loads imgs and dirs into img-map and dir-map"
  (with-redefs [#'ecs-test.systems.rendering/img-map (ref {})
                #'ecs-test.systems.rendering/dir-map (ref {})
                #'ecs-test.utils.assetmgr/load-manifest
                  (fn [man-file] 
                    {:an-ent 
                      {:rendering 
                       {:images [.img1. .img2. .img3.]
                        :dir-map {:N [.img1.], :S [.img2.], :E [.img3.], :W [.img4.] }}}})
    
