(ns ruin.entities
  (:require [ruin.entity :as e]))

(defn index-of
  [entities entity]
  (let [id (e/id entity)]
    (loop [i 0]
      (when (< i (alength entities))
             (let [entity (aget entities i)]
               (if (= (e/id entity) id)
                 i
                 (recur (inc i))))))))

(defn first-with
  [entities property]
  (loop [i 0]
    (when (< i (alength entities))
      (let [e (aget entities i)]
        (if (contains? e property)
          e
          (recur (inc i)))))))

(defn get-by-id
  [entities id]
  (loop [i 0]
    (when (< i (alength entities))
      (let [entity (aget entities i)]
        (if (= (e/id entity) id)
          entity
          (recur (inc i)))))))

(defn create
  []
  (array))

(defn add!
  [entities entity]
  (.push entities entity)
  entities)

(defn remove!
  [entities entity]
  (when-let [index (index-of entities entity)]
    (.splice entities index 1))
  entities)
