(ns ecs-test.core
  (:require [ecs-test.utils.misc :refer [splitlast class->keyword
                                         generate-id]]))

; Components and Entities are both Unique
(defprotocol Unique
  (get-id [self]))

;; Components

;(defprotocol IComponent
;  (get-entity-id [this]))

(defmacro defcomponent [name fields]
  `(defrecord ~name [~'id ~@fields]
    Unique ;IComponent
    (get-id [this#] (:id this#))))

(defmacro make-comp 
  [ctype & values]
  (let [rec-sym (symbol (str "->" ctype))]
    `(~rec-sym ~(generate-id) ~@values)))

;; Entities

(defprotocol IEntity
  (get-comp [this ctype])
  (get-comps [this])   ; a map of {CompName comp}
  (assoc-comp [this partial-comp])
  (dissoc-comp [this ctype]))

(defrecord Entity [id comps]
  Unique
    (get-id [this] (:id this))
  IEntity
    (get-comp [this ctype] ((:comps this) ctype))
    (get-comps [this] (:comps this))
    (assoc-comp [this c]
      (let [id (get-id this)]
        (->Entity id (assoc (:comps this)
                            (class->keyword (class c))
                            c))))
    (dissoc-comp [this ctype]
      (->Entity (get-id this) (dissoc (:comps this) ctype))))
 
;TODO protocols can't take variadic args
; so I have to do this here
(defn assoc-comps [ent & partial-comps]
  (reduce #(assoc-comp % %2) ent partial-comps))

(defn make-entity [& comps]
  (reduce assoc-comps
          (->Entity (generate-id) {})  ; initial value
          comps))

(comment
  "IDEAS:
  - Aspects: given to systems when created. Basically are filters for
    entities, i.e. entitiy has Components of type X, Y and Z. Uses
    logical operators for inclusion/exclusion of Component types.
  - Organization: DON'T STORE COMPONENTS AS A HASHMAP on entities.
    Think of how to do this the way artemis does:

    MyComponent myComponent = Components[4][25];

    This gets all components of type 4, and finds the one for entity 25
    + COnsider a secondary id for entities, an 'active id', in addition to UUID.
    + Artemis uses this for recycling id's, so max active id represents the most
      entities active in the system at once
  - Clojure ants demo
    + Passive things (i.e. in the world) are refs, active things that change the
      world are agents. Systems are (basically) agents, i.e. the 'evaporator' and
      'animator' as well as each ant, are agents. Ant behaviors are defined in specific
      agent functions. Think about how this would map to ECS.
    + The animation in (render) fn is just a giant dosync; the whole world is a bunch
      of refs and when it is rendered it gets a CONSISTENT VIEW OF THE WORLD because
      of STM system.
      = You VIEW the world in TIME SLICES (i.e. transactions, via refs), but ACT ON IT
        asynchronously (via agents).
  - Constructors, particularly defrecord for Components:
    + Only need to have the ABSOLUTELY REQUIRED fields in the defrecord. Optional/later added
      fields can be added later (because records implement the map interface!)
  "
) 
