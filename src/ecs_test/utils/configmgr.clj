(ns ecs-test.utils.configmgr
  (:use (clojure pprint)))

(def ^:dynamic *config*)

;TODO provide default values, validate config, etc

(defn load-config
  ([cfg-file]
    (let [cfg (load-file cfg-file)]
      (println "Config contents:")
      (pprint cfg)
      cfg))
  ([]
    (load-config "config.clj")))

(defn config-get [keys-vec]
  (if (bound? (var *config*))
      (get-in *config* keys-vec)
      (throw (Exception. "*config* must first be bound with #'ecs-test.utils.configmgr/with-config"))))

(defmacro with-config [cfg & exprs]
  `(binding [*config* ~cfg]
     ~@exprs))

