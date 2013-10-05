(ns demo.screens
  (:use [cljs.core.async :only [<!]]
        [ruin.util :only [int->letter]])
  (:require [ruin.display :as d]
            [ruin.item :as i]
            [demo.inventory :as inv])
  (:use-macros [cljs.core.async.macros :only [go]]))

(defn close-command?
  [event-type key-code]
  (and (= event-type :down)
       (= key-code js/ROT.VK_ESCAPE)))

(defn select-command?
  [event-type key-code]
  (and (>= key-code js/ROT.VK_A) (<= key-code js/ROT.VK_Z)))

(defn key->selection
  [key-code]
  (- key-code js/ROT.VK_A))

(defn render-item-collection
  [{:keys [items selection]} display caption]
  (doto display
    (d/draw-text! 0 0 caption))
  (dotimes [i (count items)]
    (let [index (-> items keys (nth i))
          item (get items index)
          selected? (and selection (= index selection))]
      (->>
        [(int->letter i)
         (if selected? "+" "-")
         (i/describe item)]
        (interpose " ")
        (apply str)
        (d/draw-text! display 0 (+ 2 i))))))

(defn item-viewing
  [items display key-events caption]
  (render-item-collection
    {:items items} display caption)
  (go 
    (loop [[event-type key-code] (<! key-events)]
      (if (close-command? event-type key-code)
        nil
        (recur (<! key-events))))))

(defn item-selection
  [items display key-events caption]
  (let [state {:items items
               :selection (-> items keys first)}]
    (render-item-collection state display caption)
    (go
      (loop [[event-type key-code] (<! key-events) state state]
        (cond
          (close-command? event-type key-code)
          (:selection state)

          (and (select-command? event-type key-code)
               (contains? (:items state) (key->selection key-code)))
          (let [index (key->selection key-code)
                updated-state (assoc state :selection index)]
            (render-item-collection updated-state display caption)
            (recur (<! key-events) updated-state))

          :else
          (recur (<! key-events) state))))))
