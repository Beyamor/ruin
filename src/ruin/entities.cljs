(ns ruin.entities
  (:require [ruin.entity :as e]))

(defn index-of
  [entities entity]
  (let [list (.-list entities)
        id (e/id entity)]
    (loop [i 0]
      (when (< i (alength list))
        (let [entity (aget list i)]
          (if (= (e/id entity) id)
            i
            (recur (inc i))))))))

(defn first-with
  [entities property]
  (let [list (.-list entities)]
    (loop [i 0]
      (when (< i (alength list))
        (let [e (aget list i)]
          (if (contains? e property)
            e
            (recur (inc i))))))))

(defn get-by-id
  [entities id]
  (let [list (.-list entities)]
    (loop [i 0]
      (when (< i (alength list))
        (let [entity (aget list i)]
          (if (= (e/id entity) id)
            entity
            (recur (inc i))))))))

(defn create
  []
  (js-obj
    "list" (array)
    "indices" (js-obj)))

(defn add!
  [entities entity]
  (when entity
    (let [list (.-list entities)
          indices (.-indices entities)]
      (aset indices (e/id entity) (alength list))
      (.push list entity)))
  entities)

(defn remove!
  [entities entity]
  (when entity
    (let [list (.-list entities)
          indices (.-indices entities)]
      (when-let [index (index-of entities entity)]
        (.splice list index 1)
        (loop [index index]
          (when (< index (alength list))
            (let [id (e/id (aget list index))]
              (aset indices id index)))))))
  entities)

(defn update!
  [entities entity]
  (when entity
    (when-let [index (index-of entities entity)]
      (aset (.-list entities) index entity)))
  entities)
