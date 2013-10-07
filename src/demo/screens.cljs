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

(defn select-none-command?
  [event-type key-code]
  (and (= event-type :down)
       (= key-code js/ROT.VK_0)))

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
  [items display key-events caption
   & {:keys [can-select-none? initial-selection]}]
  (let [state {:items items
               :selection (or initial-selection (-> items keys first))}
        render (fn [{:keys [items selection]}]
                 (render-item-collection items display caption #(= selection %)))]
    (render state)
    (go
      (loop [[event-type key-code] (<! key-events) state state]
        (cond
          (confirm-command? event-type key-code)
          (:selection state)

          (close-command? event-type key-code)
          :cancel

          (and can-select-none?
               (select-none-command? event-type key-code))
          (let [updated-state (assoc state :selection :none)]
            (render updated-state)
            (recur (<! key-events) updated-state))

          (select-none-command? event-type key-code)
          (let [updated-state (assoc state :selection :none)]
            (render state)
            (recur (<! key-events) updated-state))

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
            :cancel)

          (close-command? event-type key-code)
          :cancel

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

(defn render-stat-selection
  [options {:keys [points-remaining]} display]
  (doto display
    d/clear!
    (d/draw-text! 0 0 "Choose a stat to increase"))
  (dotimes [i (count options)]
    (->>
      (str (int->letter i) " - " (get options i))
      (d/draw-text! display 0 (+ i 2))))
  (->>
    (str "Remaining points: " points-remaining)
    (d/draw-text! display 0 (+ 4 (count options)))))

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

(defn stat-gain-selection
  [options number-of-points display key-events]
  (let [state {:points-remaining number-of-points
               :decisions []}
        render #(render-stat-selection options % display)]
    (render state)
    (go (loop [[event-type key-code] (<! key-events) state state]
          (if (zero? (:points-remaining state))
            (:decisions state)
            (cond
              (and (select-command? event-type key-code)
                   (contains? options (key->selection key-code)))
              (let [updated-state (-> state
                                    (update-in [:points-remaining] dec)
                                    (update-in [:decisions] conj (key->selection key-code)))]
                (render updated-state)
                (recur (<! key-events) updated-state))

              :else
              (recur (<! key-events) state)))))))
