(ns ecs-test.unique
  (:require [ecs-test.utils.misc :refer [generate-id]]))

;(defn generate-id [] (str (java.util.UUID/randomUUID)))

(defprotocol Unique
  (get-id [self]))

(defmacro defunique
  "For defining unique records;
   each record, if created with make-unique, 
   will have an :id field in its metadata."
  [name fields]
    `(defrecord ~name [~@fields]
       Unique 
       (get-id [this#] (:id (meta this#)))))

(defmacro make-unique
  "For creating 'instances' of previously
   defined unique's with defunique.
   :id field in meta info is created automatically."
  [name & values]
  (let [id-val (generate-id)
        rec-sym (symbol (str "->" name))]
    `(with-meta (~rec-sym ~@values) {:id ~id-val})))

