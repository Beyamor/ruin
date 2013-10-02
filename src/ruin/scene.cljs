(ns ruin.scene
  (:require-macros [lonocloud.synthread :as ->]))

(defn enter
  [{:keys [enter]
    :or {enter identity}} game]
  (enter game))

(defn exit
  [{:keys [exit]
    :or {exit identity}} game]
  (exit game))

(defn handle-input
  [{:keys [handle-input]} game event]
  (if handle-input
    (handle-input game event)
    game))

(defn render
  [{:keys [render]} game]
  (when render
    (render game)))

(defn create
  [{:as scene}]
  (->
    {:entities (array)}
    (merge scene)))
