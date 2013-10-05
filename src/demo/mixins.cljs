(ns demo.mixins
  (:use [cljs.core.async :only [<! timeout]]
        [demo.entities :only [fungus]]
        [ruin.util :only [assoc-if-missing]])
  (:require [ruin.level :as l]
            [demo.tiles :as ts]
            [ruin.game :as g]
            [ruin.scene :as s]
            [ruin.entity :as e]
            [ruin.entities :as es]
            [demo.helpers :as helpers]
            [ruin.random :as random])
  (:use-macros [cljs.core.async.macros :only [go]]
               [ruin.mixin.macros :only [defmixin]])
  (:require-macros [lonocloud.synthread :as ->]))

(defn merge-messages
  [m1 m2]
  (reduce
    (fn [m [k v]]
      (if-not (contains? m k)
        (assoc m k v)
        (case k
          :send (update-in m [k] concat v)
          (throw (js/Error. (str "Unhandled duplicate key: " k))))))
    m1 m2))

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
  [entity {:keys [level entities]} dx dy]
  (let [x (+ (:x entity) dx)
        y (+ (:y entity) dy)]
    (let [tile (l/get-tile level x y)
          target (es/first-at-position entities x y)]
      (cond
        ; target and we can attack, do so
        (and target (e/has-mixin? entity :attacker)
             (or (e/has-mixin? entity :player-actor) (e/has-mixin? target :player-actor)))
        (e/call entity :try-attack target)

        ; target and we can't attack, do nothing
        target
        nil

        ; if the tile's free, walk on it
        (:walkable? tile)
        {:update (e/set-pos entity x y)}

        ; otherwise, try digging it
        (and (:diggable? tile)
             (e/has-mixin? entity :player-actor))
        {:update-level
         (dig level x y)}))))

(defmixin
  player-actor
  :group :actor
  :act (fn [this {:keys [scene key-events]}]
         (go (let [[dx dy] (<! (get-player-movement-direction key-events))]
               (merge-messages
                 {:clear-messages this}
                 (try-move this scene dx dy))))))

(defmixin
  fungus-actor
  :group :actor
  :init #(assoc % :growths 5)
  :act (fn [{:keys [growths] :as this} {:keys [scene]}]
         (when (and (pos? growths)
                    (<= (rand) 0.02))
           (let [x (+ (:x this) -1 (rand-int 3))
                 y (+ (:y this) -1 (rand-int 3))]
             (when (helpers/is-empty-floor? scene x y)
               {:update (-> this
                          (update-in [:growths] dec))
                :add (e/set-pos (fungus) x y)
                :send (for [entity (es/nearby (:entities scene) x y)]
                        [entity "The fungus is spreading!"])})))))

(def init-health #(-> %
                    (assoc-if-missing :max-hp 10)
                    (->/as with-max-hp
                           (assoc-if-missing :hp (:max-hp with-max-hp)))
                    (assoc-if-missing :defense 0)))

(defn take-damage
  [& {:keys [on-death]}]
  (fn [this attacker damage]
    (let [new-hp (- (:hp this) damage)]
      (if (> new-hp 0)
        {:update (assoc this :hp new-hp)}
        (merge
          {:update (assoc this :hp 0)}
          (on-death this attacker))))))

(defmixin
  destructible
  :init init-health
  :group :destructible
  :take-damage (take-damage
                 :on-death
                 (fn [this attacker]
                   {:remove this
                    :send [[this "You die!"]
                           [attacker (str "You kill the " (:name this) ".")]]})))
(defmixin
  destructible-player
  :init init-health
  :group :destructible
  :take-damage (take-damage
                 :on-death
                 (fn [this attacker]
                   {:send [[this "You have died... Press [Enter] to continue!"]]
                    :player-killed? true})))

(defmixin
  attacker
  :group :attacker
  :init #(-> %
           (assoc-if-missing :attack 1))
  :try-attack (fn [this target]
                (when (e/has-mixin? target :destructible)
                  (let [damage (->
                                 (- (:attack this) (:defense target))
                                 (max 0)
                                 rand-int
                                 inc)]
                    (merge-messages
                      {:send [[this (str "You strike the " (:name target) " for " damage " damage!")]
                              [target (str "The " (:name this) " strikes you for " damage " damage!")]]}
                      (e/call target :take-damage this damage))))))

(defmixin
  message-recipient
  :get-messages (fn [this scene]
                  (s/get-messages scene this)))

(defmixin
  sight
  :group :sight
  :init #(-> % (assoc-if-missing :sight-radius 5)))

(defmixin
  wander-actor
  :group :actor
  :act (fn [this {:keys [scene]}]
         (let [[dx dy] (if (random/coin-flip)
                         [(random/plus-minus) 0]
                         [0 (random/plus-minus)])]
           (try-move this scene dx dy))))
