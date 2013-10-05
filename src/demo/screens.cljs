(ns demo.screens
  (:use [cljs.core.async :only [<!]]
        [ruin.util :only [int->letter]])
  (:require [ruin.display :as d]
            [ruin.item :as i]
            [demo.inventory :as inv])
  (:use-macros [cljs.core.async.macros :only [go]]))

(def number-of-items (comp count :items :entity))

(defn nth-inventory-index
  [state i]
  (nth (-> state :entity :items keys) i))

(defn render-inventory
  [state display]
  (doto display
    (d/draw-text! 0 0 "Inventory"))
  (dotimes [i (number-of-items state)]
    (let [index (nth-inventory-index state i)
          item (get-in state [:entity :items index])]
      (->>
        [(int->letter i)
         (if (= (:selection state) i) "+" "-")
         (i/describe item)]
        (interpose " ")
        (apply str)
        (d/draw-text! display 0 (+ 2 i))))))

(defn initial-inventory-state
  [entity]
  {:entity entity
   :selection 0})

(defn set-selection
  [state index]
  (->>
    (loop [selection 0]
      (if (= (nth-inventory-index state selection) index)
        selection
        (recur (inc selection))))
    (assoc state :selection)))

(defn inventory
  [entity display key-events]
  (let [state (initial-inventory-state entity)]
    (render-inventory state display)
    (go
      (loop [[event-type key-code] (<! key-events) state state]
        (cond
          (and (= :down event-type) (= key-code js/ROT.VK_ESCAPE))
          nil

          (and (>= key-code js/ROT.VK_A) (<= key-code js/ROT.VK_Z))
          (let [index (- key-code js/ROT.VK_A)]
            (if (contains? (-> state :entity :items) index)
              (let [updated-state (set-selection state index)]
                (render-inventory updated-state display)
                (recur (<! key-events) updated-state))
              (recur (<! key-events) state)))

          :else
          (recur (<! key-events) state))))))
