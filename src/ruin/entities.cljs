(ns ruin.entities
  (:require [ruin.entity :as e]))

(defn index-of
  [entities entity]
  (get @(.-indices entities) (e/id entity)))

(defn get-by-id
  [entities id]
  (->>
    (get @(.-indices entities) id)
    (aget (.-list entities))))

(defn create
  []
  (js-obj
    "list" (array)
    "indices" (atom {})
    "positions" (atom {})))

(defn add-to-positions
  [positions entity]
  (update-in positions [(:x entity) (:y entity)] (fnil conj #{}) (e/id entity)))

(defn remove-from-positions
  [positions entity]
  (let [x (:x entity)
        y (:y entity)
        id (e/id entity)]
    (if (get-in positions [x y id])
      (update-in positions [x y] disj id)
      positions)))

(defn update-position
  [positions old-entity new-entity]
  (if (or (not= (:x old-entity) (:x new-entity))
          (not= (:y old-entity) (:y new-entity)))
    (-> positions
      (remove-from-positions old-entity)
      (add-to-positions new-entity))
    positions))

(defn add!
  [entities entity]
  (when entity
    (let [list (.-list entities)
          indices (.-indices entities)
          positions (.-positions entities)]
      (swap! indices assoc (e/id entity) (alength list))
      (swap! positions add-to-positions entity)
      (.push list entity)))
  entities)

(defn remove!
  [entities entity]
  (when entity
    (let [list (.-list entities)
          indices (.-indices entities)
          positions (.-positions entities)]
      (when-let [index (index-of entities entity)]
        (.splice list index 1)
        (swap! indices dissoc (e/id entity))
        (swap! positions remove-from-positions entity)
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
      (let [entity-list (.-list entities)
            old-entity (aget entity-list index)]
        (swap! (.-positions entities) update-position old-entity entity)
        (aset entity-list index entity))))
  entities)

(defn first-match
  [entities pred?]
  (let [entity-list (.-list entities)]
    (loop [i 0]
      (when (< i (alength entity-list))
        (let [entity (aget entity-list i)]
          (if (pred? entity)
            entity
            (recur (inc i))))))))

(defn first-with
  [entities property]
  (first-match entities #(contains? % property)))

(defn at-position
  [entities x y]
  (when-let [ids (get-in @(.-positions entities) [x y])]
    (map #(get-by-id entities %) ids)))

(defn first-at-position
  [entities x y]
  (first (at-position entities x y)))
