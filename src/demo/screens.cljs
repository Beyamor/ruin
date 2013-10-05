(ns demo.screens
  (:use [cljs.core.async :only [<!]]
        [ruin.util :only [int->letter]])
  (:require [ruin.display :as d]
            [ruin.item :as i]
            [demo.inventory :as inv])
  (:use-macros [cljs.core.async.macros :only [go]]))

(def number-of-items (comp count :items :entity))

(defn inventory-index-i
  [state i]
  (nth (-> state :entity :items keys) i))

(defn render-inventory
  [state display]
  (doto display
    (d/draw-text! 0 0 "Inventory"))
  (dotimes [i (number-of-items state)]
    (let [index (inventory-index-i state i)
          item (get-in state [:entity :items index])]
      (print index (get-in state [:entity :items]))
      (doto display
        (d/draw-text! 0 (+ 2 i)
                      (str (int->letter i) " " (i/describe item)))))))

(defn initial-inventory-state
  [entity]
  {:entity entity
   :selection 0})

(defn inventory
  [entity display key-events]
  (let [state (initial-inventory-state entity)]
    (print (number-of-items state))
    (render-inventory state display)
    (go
      (loop [[event-type key-code] (<! key-events)]
        (cond
          (and (= :down event-type) (= key-code js/ROT.VK_ESCAPE))
          nil

          :else
          (recur (<! key-events)))))))
