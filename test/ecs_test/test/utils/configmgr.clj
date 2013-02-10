(ns ecs-test.test.utils.configmgr
  (:use (ecs-test.utils configmgr)
        (ecs-test.utils assetmgr)
        (midje sweet)))


(fact "load-config gets the asset content of the config manifest"
  (let [config-content {:val1 "hello" :val2 "there"}]
    (with-redefs-fn {#'ecs-test.utils.assetmgr/load-manifest
                    (fn [cfg] (reify Asset (path-to [_]) (asset-name [_])
                              (asset-content [_] config-content)))}
      #(do
        (load-config) => config-content
        (load-config "my-config.clj") => config-content))))

(fact "config-get returns values from *config*"
  (binding [ecs-test.utils.configmgr/*config* 
            {:hello "there" :my {:best "friend"}}]
    (config-get [:hello])    => "there"
    (config-get [:my :best]) => "friend"))

(fact "with-config just binds *config* to the given value"
  (with-config ["val1" "val2"]
    (first *config*)  => "val1"
    (second *config*) => "val2")
  (with-config {:hello "there" :my {:best "friend"}}
    (config-get [:my :best]) => "friend"))

