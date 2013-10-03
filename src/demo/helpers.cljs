(ns demo.helpers
  (:require [ruin.scene :as s]
            [ruin.level :as l]
            [demo.tiles :as ts]))

(defn is-empty-floor?
  [scene x y]
  (and (= ts/floor-tile (l/get-tile (:level scene) x y))
       (not (s/entity-at-position scene x y))))
