(ns ecs-test.systems.core
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

(defn apply-compfn [compfn ent]
  "Should use for calling all compfns on entities"
  ;delta-loc [pos dir]
  ;(apply compfn (map get-comps ents)))
  (compfn (get-comps ent)))

(defn apply-if [aspectfn compfn & ents]
  "Applies a Component fn to one or more entities if they all meet aspect-fn"
  (if (every? aspectfn ents)
      (do
        (println (str "ENTS: " ents))
        (apply (partial apply-compfn compfn) ents))))

;(def e (make-entity (make-comp Position 1 1 1)))
;(println (apply-if (or* (has-all-comps :Position :Direction)
;                        (has-all-comps :Direction))
;                   delta-loc
;                   [e]))

                   
