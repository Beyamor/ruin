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
  [entity]
  (not (full? entity)))

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

(defn remove
  [entity which]
  [(update-in entity [:items] dissoc which)
   (get-in entity [:items which])])

(defn pick-up
  [{:keys [x y] :as entity} level]
  (loop [entity entity level level]
    (let [[item level] (l/remove-first-item x y)]
    (if (or (not item) (full? entity))
      [entity level]
      (recur (add entity item) level)))))

(defn drop
  [{:keys [x y] :as entity} level which]
  (let [[entity item] (remove entity which)]
    [entity (l/add-item level x y)]))
