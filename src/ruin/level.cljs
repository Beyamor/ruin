(ns ruin.level
  (:use [ruin.core :only [null-tile]]
        [ruin.util :only [remove-index apply-map]])
  (:require [ruin.array2d :as a2d]))

(defn get-items
  ([level x y]
    (get-in level [:items x y])))

(defn get-item
  [level x y index]
  (nth (get-items level x y) index))

(defn add-item
  [level x y item]
  (update-in level [:items x y] (fnil conj []) item))

(defn remove-item
  [level x y index]
  [(update-in level [:items x y] remove-index index)
   (first (get-items level x y))])

(defn remove-items
  [level x y indexes]
  (let [indexes (set indexes)
        items (get-items level x y)
        [keep remove] (loop [i 0 keep [] remove []]
                        (if (< i (count items))
                          (let [item (nth items i)
                                [keep remove] (if (contains? indexes i)
                                                [keep (conj remove item)]
                                                [(conj keep item) remove])]
                            (recur (inc i) keep remove))
                          [keep remove]))]
    [(assoc-in level [:items x y] keep) remove]))

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
  [level {:keys [vision]
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
  [level x y sight-radius & {:as fov-options}]
  (let [visibles (atom #{})]
    (.compute (fov level fov-options)
              x y sight-radius
              (fn [x y radius visibility]
                (swap! visibles conj [x y])))
    @visibles))

(defn path-step
  [level from-x from-y to-x to-y can-walk?]
  (let [path (js/ROT.Path.AStar. to-x to-y can-walk?)
        counter (atom 0)
        result (atom nil)]
    (.compute path from-x from-y
              (fn [x y]
                (when (= 1 @counter)
                  (reset! result [x y]))
                (swap! counter inc)))
    @result))
