(ns ruin.entities
  (:require [ruin.entity :as e]))

(defn index-of
  [entities entity]
  (get @(.-indices entities) (e/id entity)))

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
  (->>
    (get @(.-indices entities) id)
    (aget (.-list entities))))

(defn create
  []
  (js-obj
    "list" (array)
    "indices" (atom {})))

(defn add!
  [entities entity]
  (when entity
    (let [list (.-list entities)
          indices (.-indices entities)]
      (swap! indices assoc (e/id entity) (alength list))
      (.push list entity)))
  entities)

(defn remove!
  [entities entity]
  (when entity
    (let [list (.-list entities)
          indices (.-indices entities)]
      (when-let [index (index-of entities entity)]
        (.splice list index 1)
        (swap! indices dissoc (e/id entity))
        (loop [index index]
          (when (< index (alength list))
            (let [id (e/id (aget list index))]
              (swap! indices assoc id index)
              (recur (inc index))))))))
  entities)

(defn update!
  [entities entity]
  (when entity
    (when-let [index (index-of entities entity)]
      (aset (.-list entities) index entity)))
  entities)
