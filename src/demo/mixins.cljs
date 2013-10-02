(ns demo.mixins
  (:require [demo.level :as l]))

(def moveable
  {:name :moveable
   :try-move
   (fn [e level x y]
     (let [tile (get-in level [:tiles x y])]
       (cond
         (:walkable? tile)
         {:entity (-> e
                    (assoc :x x)
                    (assoc :y y))}
         (:diggable? tile)
         {:level (l/dig level x y)})))})
