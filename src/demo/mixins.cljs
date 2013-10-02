(ns demo.mixins
  (:require [demo.level :as l]))

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
         {:level (l/dig level x y)})))})
