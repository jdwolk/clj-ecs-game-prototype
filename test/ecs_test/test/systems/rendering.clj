(ns ecs-test.test.systems.rendering
  (:use (ecs-test core)
        (ecs-test.utils assetmgr logger)
        (ecs-test.systems core rendering)
        (midje sweet))
  (:import (ecs-test.systems.rendering.Visual)))

(fact "assoc-ent-imgs loads and merges images into the global img-map"
  (with-redefs-fn {#'ecs-test.utils.assetmgr/load-images
                   (fn [imgs] {:img1 "IMAGE1", :img2 "IMAGE2"})}
    #(let [img-map {:img3 "IMAGE3"}]
      (assoc-ent-imgs img-map {:A ["img1"], :B ["img2"]})
       => {:img1 "IMAGE1", :img2 "IMAGE2", :img3 "IMAGE3"})))

(fact "assoc-ent-dirs associates dir map from render-info w/ ent type"
  (let [dirs {:N [.img1.], :S [.img2.], :E [.img3.], :W [.img4.]}]
    (assoc-ent-dirs {} :newentity dirs) 
    => {:newentity dirs}))

(comment
(def ent-manifest
  {:an-ent 
    {:rendering
      {:dir-imgs {:N [:img1], :S [:img2], :E [:img3], :W [:img4]}}}})

(fact "load-entity! loads imgs and dirs into img-map and dir-map"
  (with-redefs [ecs-test.systems.rendering/img-map (ref {})
                ecs-test.systems.rendering/dir-map (ref {})
                ecs-test.systems.rendering/assoc-ent-imgs
                  (fn [d-map dir-imgs] 
                    (merge d-map ent-manifest))
                ecs-test.utils.assetmgr/load-manifest
                  (as-asset "fake-file" identity  
                    (fn [man-file] ent-manifest))]
      (load-entity! "fake-file") => nil
      (println "IMG MAP: " @img-map) => nil
      (count (deref img-map)) => 4
      (count (deref dir-map)) => 4))
)

(fact "direction-img makes a Visual comp based on the dir and ent-type"
  (with-redefs [ecs-test.systems.rendering/dir-map (ref {:awesome {:N [:facing-up]}})]
    (let [comps {:EntType {:ent-type :awesome}, :Direction {:dir :N}}]
      (:img-name (direction-img comps)) => :facing-up)))

(fact "lookup-img gets the asset content of the image looked up by Visual comp in the img-map"
  (with-redefs [ecs-test.systems.rendering/img-map (ref {:only-img (as-asset "only-img" identity (fn [_] "IMAGE: only-img"))})]
    (lookup-img (make-comp Visual :only-img)) => "IMAGE: only-img"))

