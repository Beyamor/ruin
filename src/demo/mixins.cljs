(ns demo.mixins
  (:use [cljs.core.async :only [<! timeout]])
  (:require [ruin.level :as l]
            [demo.tiles :as ts]
            [ruin.game :as g]
            [ruin.scene :as s])
  (:use-macros [cljs.core.async.macros :only [go]])
  (:require-macros [lonocloud.synthread :as ->]))

(defn dig
  [level x y]
  (-> level
    (->/when (:diggable? (l/get-tile level x y))
             (l/set-tile x y ts/floor-tile))))

(def is-player
  {:name :is-player
   :is-player? true})

(defn get-player-movement-direction
  [key-events]
  (go (loop [[event-type key-code] (<! key-events)]
        (if (= :down event-type)
         (cond 
           (= key-code js/ROT.VK_LEFT) [-1 0]
           (= key-code js/ROT.VK_RIGHT) [1 0]
           (= key-code js/ROT.VK_UP) [0 -1]
           (= key-code js/ROT.VK_DOWN) [0 1]
           :else (recur (<! key-events)))
          (recur (<! key-events))))))

(defn move
  [e {:keys [level] :as scene} dx dy]
  (let [x (+ (:x e) dx)
        y (+ (:y e) dy)]
  (when-not (s/entity-at-position? scene x y)
       (let [tile (l/get-tile level x y)]
         (cond
           (:walkable? tile)
           {:entity-update
            (-> e
              (assoc :x x)
              (assoc :y y))}

           (:diggable? tile)
           {:level-update
            (dig level x y)})))))

(def player-actor
  {:name :player-actor
   :group :actor
   :act
   (fn [e {:keys [scene key-events]} x y]
     (go (let [[dx dy] (<! (get-player-movement-direction key-events))]
           (move e scene dx dy))))})

(def fungus-actor
  {:name :fungus-actor
   :group :actor
   :act
   (fn [e game]
     {:entity-update
      (-> e
        (update-in [:x] + -1 (rand-int 3))
        (update-in [:y] + -1 (rand-int 3)))})})
