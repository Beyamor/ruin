(ns demo.level
  (:require [demo.tiles :as ts])
  (:require-macros [lonocloud.synthread :as ->]))

(defn dig
  [level x y]
  (-> level
    (->/in [:tiles]
           (->/as tiles
                  (->/when (:diggable? (get-in tiles [x y]))
                           (assoc-in [x y] ts/floor-tile))))))

(defn random-floor-position
  [level]
  (let [random-x #(rand-int (:width level))
        random-y #(rand-int (:height level))
        tiles (:tiles level)]
    (loop [x (random-x) y (random-y)]
      (if (= ts/floor-tile (get-in tiles [x y]))
        [x y]
        (recur (random-x) (random-y))))))

