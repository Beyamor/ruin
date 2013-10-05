(ns demo.inventory
  (:use [ruin.util :only [remove-index]])
  (:require [ruin.level :as l])
  (:refer-clojure :exclude [empty? remove drop]))

(defn empty?
  [{:keys [items]}]
  (cljs/empty? items))

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
  [(update-in entity [:items] dissoc which)
   (get-in entity [:items which])])

(defn pick-up-multiple
  [{:keys [x y] :as entity} level indices]
  (if (has-room? entity indices)
    (let [[updated-level items] (l/remove-items level x y indices)]
      [(add-all entity items) updated-level])
    (throw (js/Error. (str "Whoa, not enough room in the inventory! "
                           (count indices) " + " (count (:items entity)) " / " (:inventory-size entity))))))

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
