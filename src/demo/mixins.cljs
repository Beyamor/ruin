(ns demo.mixins
  (:require [ruin.level :as l]
            [demo.tiles :as ts])
  (:require-macros [lonocloud.synthread :as ->]))

(defn dig
  [level x y]
  (-> level
    (->/when (:diggable? (l/get-tile level x y))
             (l/set-tile x y ts/floor-tile))))

(def is-player
  {:name :is-player
   :is-player? true})

(def player-moveable
  {:name :player-moveable
   :group :moveable
   :try-move
   (fn [e {:keys [level]} x y]
     (let [tile (l/get-tile level x y)]
       (cond
         (:walkable? tile)
         {:entity (-> e
                    (assoc :x x)
                    (assoc :y y))}
         (:diggable? tile)
         {:level (dig level x y)})))})
