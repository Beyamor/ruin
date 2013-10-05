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

(defn confirm-command?
  [event-type key-code]
  (and (= event-type :down)
       (= key-code js/ROT.VK_RETURN)))

(defn select-command?
  [event-type key-code]
  (and (= event-type :down)
    (>= key-code js/ROT.VK_A) (<= key-code js/ROT.VK_Z)))

(defn key->selection
  [key-code]
  (- key-code js/ROT.VK_A))

(defn render-item-collection
  [items display caption selected?]
  (doto display
    d/clear!
    (d/draw-text! 0 0 caption))
  (dotimes [i (count items)]
    (let [index (-> items keys (nth i))
          item (get items index)]
      (->>
        [(int->letter index)
         (if (selected? index) "+" "-")
         (i/describe item)]
        (interpose " ")
        (apply str)
        (d/draw-text! display 0 (+ 2 i))))))

(defn item-viewing
  [items display key-events caption]
  (render-item-collection items display caption (constantly false))
  (go 
    (loop [[event-type key-code] (<! key-events)]
      (if (close-command? event-type key-code)
        nil
        (recur (<! key-events))))))

(defn item-selection
  [items display key-events caption]
  (let [state {:items items
               :selection (-> items keys first)}
        render (fn [{:keys [items selection]}]
                 (render-item-collection items display caption #(= selection %)))]
    (render state)
    (go
      (loop [[event-type key-code] (<! key-events) state state]
        (cond
          (confirm-command? event-type key-code)
          (:selection state)

          (close-command? event-type key-code)
          nil

          (and (select-command? event-type key-code)
               (contains? (:items state) (key->selection key-code)))
          (let [index (key->selection key-code)
                updated-state (assoc state :selection index)]
            (render updated-state)
            (recur (<! key-events) updated-state))

          :else
          (recur (<! key-events) state))))))

(defn multiple-item-selection
  [items display key-events caption]
  (let [state {:items items
               :selections #{}}
        render (fn [{:keys [items selections]}]
                 (render-item-collection items display caption #(contains? selections %)))]
    (render state)
    (go
      (loop [[event-type key-code] (<! key-events) state state]
        (cond
          (confirm-command? event-type key-code)
          (if-not (empty? (:selections state))
            (:selections state)
            nil)

          (close-command? event-type key-code)
          nil

          (and (select-command? event-type key-code)
               (contains? (:items state) (key->selection key-code)))
          (let [index (key->selection key-code)
                updated-state (if (contains? (:selections state) index)
                                (update-in state [:selections] disj index)
                                (update-in state [:selections] conj index))]
            (render updated-state)
            (recur (<! key-events) updated-state))

          :else
          (recur (<! key-events) state))))))
