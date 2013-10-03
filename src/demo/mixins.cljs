(ns demo.mixins
  (:use [cljs.core.async :only [<! timeout]]
        [demo.entities :only [fungus]])
  (:require [ruin.level :as l]
            [demo.tiles :as ts]
            [ruin.game :as g]
            [ruin.scene :as s]
            [ruin.entity :as e]
            [demo.helpers :as helpers])
  (:use-macros [cljs.core.async.macros :only [go]]
               [ruin.mixin.macros :only [defmixin]])
  (:require-macros [lonocloud.synthread :as ->]))

(defn dig
  [level x y]
  (-> level
    (->/when (:diggable? (l/get-tile level x y))
             (l/set-tile x y ts/floor-tile))))

(defmixin
  is-player
  :is-player? true)

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

(defn try-move
  [entity {:keys [level] :as scene} dx dy]
  (let [x (+ (:x entity) dx)
        y (+ (:y entity) dy)]
    (let [tile (l/get-tile level x y)
          target (s/entity-at-position scene x y)]
      (cond
        ; target and we can attack, do so
        (and target (e/has-mixin? entity :attacker))
        (e/call entity :attack target)

        ; target and we can't attack, do nothing
        target
        nil

        ; if the tile's free, walk on it
        (:walkable? tile)
        {:entity-update
         (-> entity
           (assoc :x x)
           (assoc :y y))}

        ; otherwise, try digging it
        (:diggable? tile)
        {:level-update
         (dig level x y)}))))

(defmixin
  player-actor
  :group :actor
  :act
  (fn [this {:keys [scene key-events]}]
    (go (let [[dx dy] (<! (get-player-movement-direction key-events))]
          (try-move this scene dx dy)))))

(defmixin
  fungus-actor
  :group :actor
  :init #(assoc % :growths 5)
  :act
  (fn [{:keys [growths] :as this} {:keys [scene]}]
    (when (and (pos? growths)
               (<= (rand) 0.02))
      (let [x (+ (:x this) -1 (rand-int 3))
            y (+ (:y this) -1 (rand-int 3))]
        (when (helpers/is-empty-floor? scene x y)
          {:entity-update
           (-> this
             (update-in [:growths] dec))
           :entity-creation
           (->
             (fungus)
             (assoc :x x)
             (assoc :y y))})))))


(defmixin
  destructible
  :init #(assoc % :hp 1)
  :take-damage
  (fn [this damage]
    (let [new-hp (- (:hp this) damage)]
      (if (> new-hp 0)
        {:entity-update (assoc this :hp new-hp)}
        {:entity-removal this}))))

(defmixin
  simple-attacker
  :group :attacker
  :attack
  (fn [this target]
    (when (e/has-mixin? target :destructible)
      (e/call target :take-damage 1))))
