(ns ruin.entities
  (:use [clojure.set :only [union]])
  (:require [ruin.entity :as e])
  (:refer-clojure :exclude [remove]))

(defn get-by-id
  [{:keys [id-map]} id]
  (get id-map id))

(defn create
  []
  {:id-map {}
   :position-map {}})

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

(defn add
  [entities entity]
  (-> entities
    (update-in [:position-map] add-to-positions entity)
    (assoc-in [:id-map (e/id entity)] entity)))

(defn remove
  [entities entity]
  (-> entities
    (update-in [:position-map] remove-from-positions entity)
    (update-in [:id-map] dissoc (e/id entity))))

(defn update
  [entities entity]
  (let [old-entity (get-by-id entities (e/id entity))]
    (-> entities
      (update-in [:position-map] update-position old-entity entity)
      (assoc-in [:id-map (e/id entity)] entity))))

(defn at-position
  [{:keys [position-map id-map]} x y]
  (when-let [ids (get-in position-map [x y])]
    (map id-map ids)))

(defn at-position?
  [{:keys [position-map]} x y]
  (not (empty? (get-in position-map [x y]))))

(defn first-at-position
  [entities x y]
  (first (at-position entities x y)))

(defn within-radius
  [entities center-x center-y radius]
  (apply union
         (for [x (range (- center-x radius) (inc (+ center-x radius)))
               y (range (- center-y radius) (inc (+ center-y radius)))]
           (at-position entities x y))))

(defn nearby
  [entities center-x center-y]
  (within-radius entities center-x center-y 5))
