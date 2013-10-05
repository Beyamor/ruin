(ns demo.screens
  (:use [cljs.core.async :only [<!]]
        [ruin.util :only [int->letter]])
  (:require [ruin.display :as d]
            [ruin.item :as i]
            [demo.inventory :as inv])
  (:use-macros [cljs.core.async.macros :only [go]]))

(defn render-item-selection
  [{:keys [items selection]} display]
  (doto display
    (d/draw-text! 0 0 "Inventory"))
  (dotimes [i (count items)]
    (let [index (-> items keys (nth i))
          item (get items index)]
      (->>
        [(int->letter i)
         (if (= index selection) "+" "-")
         (i/describe item)]
        (interpose " ")
        (apply str)
        (d/draw-text! display 0 (+ 2 i))))))

(defn item-selection
  [items display key-events]
  (let [state {:items items
               :selection (-> items keys first)}]
    (render-item-selection state display)
    (go
      (loop [[event-type key-code] (<! key-events) state state]
        (cond
          (and (= :down event-type) (= key-code js/ROT.VK_ESCAPE))
          nil

          (and (>= key-code js/ROT.VK_A) (<= key-code js/ROT.VK_Z))
          (let [index (- key-code js/ROT.VK_A)]
            (if (contains? (:items state) index)
              (let [updated-state (assoc state :selection index)]
                (render-item-selection updated-state display)
                (recur (<! key-events) updated-state))
              (recur (<! key-events) state)))

          :else
          (recur (<! key-events) state))))))
