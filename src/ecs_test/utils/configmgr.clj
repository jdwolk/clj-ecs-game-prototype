(ns ecs-test.utils.configmgr
  (:require [ecs-test.utils.assetmgr :refer [load-manifest asset-content]]))

(def ^:dynamic *config*)

;TODO provide default values, validate config, etc

(defn load-config
  ([cfg-file]
   (asset-content (load-manifest cfg-file)))
  ([]
    (load-config "config.clj")))

(defn config-get [keys-vec]
  (if (bound? (var *config*))
      (get-in *config* keys-vec)
      (throw (Exception. "*config* must first be bound with #'ecs-test.utils.configmgr/with-config"))))

(defmacro with-config [cfg & exprs]
  `(binding [*config* ~cfg]
     ~@exprs))

