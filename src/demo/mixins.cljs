(ns demo.mixins
  (:require [ruin.level :as l]
            [demo.tiles :as ts]
            [ruin.scene :as s])
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
   (fn [e {:keys [level] :as scene} x y]
     (when-not (s/entity-at-position? scene x y)
       (let [tile (l/get-tile level x y)]
         (cond
           (:walkable? tile)
           {:update-entity
            (-> e
              (assoc :x x)
              (assoc :y y))}

           (:diggable? tile)
           {:update-level
            (dig level x y)}))))})

(def fungus-actor
  {:name :fungus-actor
   :group :actor
   :act
   (fn [e scene]
     {:update-entity
      (-> e
        (assoc :x (+ -1 (rand-int 3)))
        (assoc :y (+ -1 (rand-int 3))))})})
