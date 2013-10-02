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
