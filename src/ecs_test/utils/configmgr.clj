(ns ecs-test.utils.configmgr
  (:use (clojure pprint)))

;TODO provide default values, validate config, etc

(defn load-config
  ([cfg-file]
    (let [cfg (load-file cfg-file)]
      (println "Config contents:")
      (pprint cfg)
      cfg))
  ([]
    (load-config "config.clj")))

