(ns ruin.scene
  (:require [ruin.display :as d])
  (:require-macros [lonocloud.synthread :as ->]
                   [ruin.entities.macros :as es+]))

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

(defn draw-entities
  [scene
   {display-width :width display-height :height :as display}
   & {:keys [left top]
      :or {left 0 :right 0}}]
  (es+/do-each [e (:entities scene)
                :let [x (:x e)
                      y (:y e)]
                :when (and (>= x left) (<= x (+ left display-width))
                           (>= y top) (<= y (+ top display-height)))]
               (d/draw-glyph! display (- x left) (- y top) (:glyph e))))

(defn first-entity-with
  [{:keys [entities]} property]
  (loop [i 0]
    (when (< i (alength entities))
      (let [e (aget entities i)]
        (if (contains? e property)
          e
          (recur (inc i)))))))
