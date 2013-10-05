(ns ruin.display
  (:require [ruin.color :as color]
            [ruin.level :as l]
            [ruin.entity :as e]
            [ruin.entities :as es])
  (:require-macros [lonocloud.synthread :as ->]))

(defn create
  [width height]
  (let [display (js/ROT.Display. (js-obj "width" width "height" height))
        container (.getContainer display)]
    {:display display
     :container container
     :width width
     :height height}))

(defn- colored-text
  [foreground background text]
  (str "%c{" (color/->color foreground) "}%b{" (color/->color background) "}" text))

(defn draw-text!
  [{:keys [display]} x y text
   & {:keys [foreground background]
      :or {foreground "white" background "black"}}]
  (.drawText display x y
             (colored-text foreground background text)))

(defn clear!
  [{:keys [display]}]
  (.clear display))

(defn draw-char!
  [{:keys [display]} x y char & {:keys [foreground background]}]
  (.draw display x y char foreground background))

(defn draw-glyph!
  [display x y {:keys [char foreground background]}]
  (draw-char! display x y char :foreground foreground :background background))

(defn draw-tile!
  [display x y {:keys [glyph]}]
  (draw-glyph! display x y glyph))

(defn highlight-visible-tiles
  [visible-tiles]
  (fn [x y tile]
    (-> tile
    (->/when (not (contains? visible-tiles [x y]))
             (assoc-in [:glyph :foreground] "darkGrey")))))

(defn draw-tiles!
  [display level
   & {{screen-left :x screen-top :y screen-width :width screen-height :height
       :or {screen-left 0 screen-top 0}} :screen
      tile-transform :transform 
      tile-filter :only
      :keys [left top]
      :or {tile-filter identity left 0 top 0}}]
  (let [screen-width (or screen-width (:width display))
        screen-height (or screen-height (:height display))]
    (doseq [screen-x (range screen-left (+ screen-left screen-width))
            screen-y (range screen-top (+ screen-top screen-height))
            :let [tile-x (+ screen-x left)
                  tile-y (+ screen-y top)
                  tile (l/get-tile level tile-x tile-y)]
            :when (tile-filter tile)
            :let [tile (if tile-transform
                         (tile-transform tile-x tile-y tile)
                         tile)]]
      (draw-tile! display screen-x screen-y tile))))

; TODO: maybe move this somewhere else. ruin.entity?
(defn visible?
  [visible-tiles]
  (fn [entity]
    (contains? visible-tiles (e/pos entity))))

(defn draw-entities!
  [display
   entities
    & {{screen-left :x screen-top :y screen-width :width screen-height :height
       :or {screen-left 0 screen-top 0}} :screen
       entity-filter :only
       :keys [left top]
       :or {entity-filter identity left 0 top 0}}]
  (let [screen-width (or screen-width (:width display))
        screen-height (or screen-height (:height display))]
    (doseq [screen-x (range screen-left (+ screen-left screen-width))
            screen-y (range screen-top (+ screen-top screen-height))
            :let [world-x (+ screen-x left)
                  world-y (+ screen-y top)]
            entity (es/at-position entities world-x world-y)
            :when (entity-filter entity)]
      (draw-glyph! display screen-x screen-y (:glyph entity)))))
