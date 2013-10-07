(ns demo.levelling
  (:use [ruin.mixin :only [has-mixin?]])
  (:require [ruin.entity :as e])
  (:require-macros [lonocloud.synthread :as ->]))

(defn next-level
  [lvl]
  (* lvl lvl 10))

(defn climb-levels
  [entity points]
  (loop [{:keys [experience level] :as entity} entity points points levels-gained 0]
    (let [points-to-level (next-level level)
          levelled? (>= (+ experience points) points-to-level)]
      (if levelled?
        (recur (-> entity
                 (update-in [:level] inc)
                 (assoc :experience points-to-level)
                 (update-in [:stat-points] + (:stat-points-per-level entity)))
               (- points (- points-to-level experience))
               (inc levels-gained))
        [(-> entity
           (update-in [:experience] + points))
         levels-gained]))))

(defn give-experience
  [entity points]
  (let [[entity levels-gained] (climb-levels entity points)
        gained-levels? (pos? levels-gained)
        entity (if gained-levels?
                 (assoc entity :hp (:max-hp entity))
                 entity)]
    [entity gained-levels?]))
