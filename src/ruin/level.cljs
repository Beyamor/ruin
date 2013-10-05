(ns ruin.level
  (:use [ruin.base :only [null-tile]])
  (:require [ruin.array2d :as a2d]))

(defn get-items
  [level x y]
  (get-in level [:items x y]))

(defn add-item
  [level x y item]
  (update-in level [:items x y] (fnil conj []) item))

(defn remove-first-item
  [level x y]
  (let [items (get-items level x y)]
    (if (empty? items)
      [nil level]
      [(peek items)
       (update-in level [:items] pop)])))


(defn add-item-at-random-pos
  [level item]
  (add-item level
            (rand-int (:width level))
            (rand-int (:height level))
            item))

(defn get-tile
  [{:keys [height width] :as level} x y]
  (if (and x y
           (>= x 0) (< x width)
           (>= y 0) (< y height))
    (a2d/get (:tiles level) x y)
    null-tile))

(defn set-tile
  [{:keys [height width] :as level} x y tile]
  (when (and x y
             (>= x 0) (< x width)
             (>= y 0) (< y height))
    (a2d/set! (:tiles level) x y tile))
  level)

(defn mark-as
  [level x y mark]
  (set-tile level x y
            (assoc (get-tile level x y) mark true)))

(defn create
  [width height tiles]
  {:width width
   :height height
   :tiles tiles
   :items {}})

(defn fov
  [level & {:keys [vision]
            :or {vision :diamond}}]
  (let [topology (case vision
                   :diamond 4
                   :hexagon 6
                   :square 8)]
    (js/ROT.FOV.DiscreteShadowcasting.
      (fn [x y]
        (-> level (get-tile x y) :blocks-light? not))
      (js-obj "topology" topology))))

(defn visible-tiles
  [level x y sight-radius fov]
  (let [visibles (atom #{})]
    (.compute fov
              x y sight-radius
              (fn [x y radius visibility]
                (swap! visibles conj [x y])))
    @visibles))
