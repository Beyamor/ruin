(ns demo.inventory
  (:use [ruin.util :only [remove-index]])
  (:require [ruin.level :as l]
            [clojure.set :as set])
  (:refer-clojure :exclude [remove drop])
  (:require-macros [lonocloud.synthread :as ->]))

(defn full?
  [{:keys [inventory-size items]}]
  (>= (count items) inventory-size))

(defn has-room?
  ([entity]
   (not (full? entity)))
  ([{:keys [items inventory-size]} more-items]
   (< (+ (count items) (count more-items)) inventory-size)))

(defn- first-unused-index
  [{:keys [inventory-size items]}]
  (loop [index 0]
    (if (contains? items index)
      (recur (inc index))
      index)))

(defn add
  [entity item]
  (when (has-room? entity)
    (update-in entity [:items] assoc (first-unused-index entity) item)))

(defn add-all
  [entity items]
  (reduce add entity items))

(defn remove
  [entity which]
  [(-> entity
     (update-in [:items] dissoc which)
     (->/when (= (:weapon entity) which)
              (assoc :weapon nil))
     (->/when (= (:armor entity) which)
              (assoc :armor nil)))
   (get-in entity [:items which])])

(defn pick-up
  [{:keys [x y] :as entity} level index]
  (if (has-room? entity)
    (let [[updated-level item] (l/remove-item level x y index)]
      [(add entity item) updated-level true])
    [entity level false]))

(defn pick-up-multiple
  [{:keys [x y] :as entity} level indices]
  (let [indices (set indices)
        [entity untouched-indices] (loop [entity entity indices indices]
                                     (if (or (empty? indices)
                                             (not (has-room? entity)))
                                       [entity indices]
                                       (recur(add entity (l/get-item level x y (first indices)))
                                              (rest indices))))
        removed-indices (set/difference indices untouched-indices)
        [level _] (l/remove-items level x y removed-indices)]
    [entity level (empty? untouched-indices)]))

(defn drop
  [{:keys [x y] :as entity} level which]
  (let [[entity item] (remove entity which)]
    [entity (l/add-item level x y item)]))

(defn drop-multiple
  [entity level whiches]
  (reduce
    (fn [[entity level] which]
      (drop entity level which))
    [entity level] whiches))
