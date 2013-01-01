(ns ecs-test.test.systems.rendering
  (:use (ecs-test core)
        (ecs-test.utils assetmgr)
        (ecs-test.systems core rendering)
        (midje sweet))
  (:import (ecs-test.systems.rendering.Visual)))

(fact "Turning a particular direction gives the appropriate image (subject to change"
  (let [ent-id "123456"]
    (:img-name (direction-img {:Direction {:entity-id ent-id :dir :N} 
                               :Visual {}})) => "player_up"
    (:img-name (direction-img {:Direction {:entity-id ent-id :dir :S} 
                               :Visual {}})) => "player_down"
    (:img-name (direction-img {:Direction {:entity-id ent-id :dir :E}
                               :Visual {}})) => "player_right"
    (:img-name (direction-img {:Direction {:entity-id ent-id :dir :W}
                               :Visual {}})) => "player_left"))

