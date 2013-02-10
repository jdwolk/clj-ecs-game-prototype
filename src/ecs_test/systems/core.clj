(ns ecs-test.systems.core
  (:require [ecs-test.utils.logger :refer [log]])
  (:use (ecs-test core)))

(defn ents-satisfying [aspect-fn ents]
  (filter aspect-fn ents)) 

(defn has-all-comps [& comps]
  (fn [entity]
    (every? (partial get-comp entity) comps)))

(defn or* [& aspectfns]
  "Combinator for aspect functions"
  (fn [entity]
    (reduce #(or % %2) (map #(% entity) aspectfns))))

(defn compfn [c-fn & ents]
  "Should use for calling all compfns on entities"
  ;(log :debug :systems/Core "Compfn: " c-fn)
  (apply c-fn (map :comps ents)))

(defn apply-if [aspectfn c-fn & ents]
  "Applies a Component fn to one or more entities if they all meet aspect-fn"
  (if (every? aspectfn ents)
      (do
        (apply (partial compfn c-fn) ents))))

