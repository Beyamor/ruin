(ns demo.level
  (:require [demo.tiles :as ts])
  (:require-macros [lonocloud.synthread :as ->]))

(defn get-tile
  [level x y]
  (aget (aget (:tiles level) x) y))

(defn set-tile
  [level x y tile]
  (aset (aget (:tiles level) x) y tile)
  level)

(defn dig
  [level x y]
  (-> level
    (->/when (:diggable? (get-tile level x y))
             (set-tile x y ts/floor-tile))))

(defn random-floor-position
  [level]
  (let [random-x #(rand-int (:width level))
        random-y #(rand-int (:height level))
        tiles (:tiles level)]
    (loop [x (random-x) y (random-y)]
      (if (= ts/floor-tile (get-tile level x y))
        [x y]
        (recur (random-x) (random-y))))))

