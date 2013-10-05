(ns demo.hunger
  (:require [demo.helpers :as helpers]
            [ruin.item :as i])
  (:require-macros [lonocloud.synthread :as ->])
  (:refer-clojure :exclude [apply]))

(defn modify
  [{:keys [fullness max-fullness] :as entity} value]
  (->> fullness
    (+ value)
    (min max-fullness)
    (assoc entity :fullness)))

(defn apply
  [{:keys [hunger] :as entity}]
  (modify entity (- hunger)))

(defn starved?
  [{:keys [fullness]}]
  (<= fullness 0))

(defn kill
  [entity]
  (helpers/kill entity "You have died of starvation!"))

(defn describe-state
  [{:keys [fullness max-fullness]}]
  (let [percent (* 100 (/ fullness max-fullness))]
    (cond
      (<= percent 5) "Starving"
      (<= percent 25) "Hungry"
      (>= percent 95) "Oversatiated"
      (>= percent 75) "Full"
      :else "Not Hungry")))

(defn edible-items
  [items]
  (into {}
        (for [[index item] items
              :when (:edible item)]
          [index item])))

(defn eat
  [entity which]
  (let [item (get-in entity [:items which])]
    [:send
     [entity (str "You eat " (i/describe-a item) ".")]
     :update
     (-> entity
      (update-in [:fullness] + (:food-value item))
      (->/if (> (:consumptions item) 1)
             (update-in [:items which :consumptions] dec)
             (update-in [:items] dissoc which)))]))
