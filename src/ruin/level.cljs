(ns ruin.level
  (:use [ruin.base :only [null-tile]])
  (:require [ruin.array2d :as a2d]
            [ruin.display :as d]))

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
   :tiles tiles})

(defn draw-tiles
  [level
   display
   & {:keys [top left display-width display-height visible-tiles] :or {left 0 top 0}}]
  (let [display-width (or display-width (:width display))
        display-height (or display-height (:height display))]
    (doseq [x (range display-width)
            y (range display-height)
            :let [tile-x (+ left x)
                  tile-y (+ top y)
                  tile (get-tile level tile-x tile-y)]
            :when (:explored? tile)
            :let [foreground (if (or (nil? visible-tiles) (contains? visible-tiles [tile-x tile-y]))
                               (get-in tile [:glyph :foreground])
                               "darkGray")]]
      (d/draw-glyph! display x y (-> tile
                                   :glyph
                                   (assoc :foreground foreground))))))

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
