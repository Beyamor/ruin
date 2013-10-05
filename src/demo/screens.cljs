(ns demo.screens
  (:use [cljs.core.async :only [<!]])
  (:require [ruin.display :as d])
  (:use-macros [cljs.core.async.macros :only [go]]))

(defn inventory
  [entity display key-events]
  (go
    (doto display
      (d/draw-text! 0 0 "Inventory"))
    (loop [[event-type key-code] (<! key-events)]
      (cond
        (and (= :down event-type) (= key-code js/ROT.VK_ESCAPE))
        nil

        :else
        (recur (<! key-events))))))
