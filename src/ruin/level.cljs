(ns ruin.level
  (:use [ruin.base :only [null-tile]])
  (:require [ruin.array2d :as a2d]
            [ruin.display :as d]))

(defn get-tile
  [{:keys [height width] :as level} x y]
  (if (and (>= x 0) (< x width)
           (>= y 0) (< y height))
    (a2d/get (:tiles level) x y)
    null-tile))

(defn set-tile
  [{:keys [height width] :as level} x y tile]
  (when (and (>= x 0) (< x width)
             (>= y 0) (< y height))
    (a2d/set! (:tiles level) x y tile))
  level)

(defn create
  [width height tiles]
  {:width width
   :height height
   :tiles tiles})

(defn draw-tiles
  [level
   {display-width :width display-height :height :as display}
   & {:keys [top left] :or {left 0 top 0}}]
  (doseq [x (range display-width)
          y (range display-height)
          :let [tile (get-tile level (+ left x) (+ top y))]]
    (d/draw-tile! display x y tile)))
