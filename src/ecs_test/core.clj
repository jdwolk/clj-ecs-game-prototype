(ns ecs-test.core
  (:use (ecs-test unique)))

(defn splitlast [a-str regex]
  (last (clojure.string/split a-str regex)))

;; Components

(defprotocol IComponent
  (get-entity-id [this]))

(defmacro defcomponent [name fields]
  `(defrecord ~name [~'entity-id ~@fields]
    IComponent
    (get-entity-id [this#] (:entity-id this#))))

(defmacro make-comp 
  "Component is not fully realized until
   called with an id to bind as :entity-id, i.e.:
   ((make-comp SomeComp 'hi' 'there') '123456')"
  [ctype & values]
  (let [rec-sym (symbol (str "->" ctype))]
    `(fn [ent-id#]
      (~rec-sym ent-id# ~@values))))

(defn assoc-entity-id [id comp]
  (comp id))

;; Entities

(defrecord Entity [id comps])
(defprotocol IEntity
  (get-ent-id [this]) ; TODO factor out into Unique protocol
  (get-comp [this ctype])
  (get-comps [this])   ; a map of {CompName comp}
  (assoc-comp [this comp])
  (dissoc-comp [this ctype]))

(extend-type Entity
  IEntity
  (get-ent-id [this] (:id this))
  (get-comp [this ctype] ((:comps this) ctype))
  ;(get-comps [this] (vals (:comps this)))
  (get-comps [this] (:comps this))
  (assoc-comp [this partial-comp] 
    "Use for adding or updating comps"
    (let [id (get-ent-id this)
          c (assoc-entity-id id partial-comp)]
      (->Entity id (assoc (:comps this) 
                          (keyword (splitlast (str (class c)) #"\."))
                          c))))
     ;(->Entity id (assoc (:comps this) (class c) c))))
  (dissoc-comp [this ctype]
      (->Entity (get-ent-id this) (dissoc (:comps this) ctype))))

(defn make-entity [& comps]
  (reduce #(assoc-comp % %2)
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
