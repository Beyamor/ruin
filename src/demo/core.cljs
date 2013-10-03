(ns demo.core
  (:use [demo.play-scene :only [play-scene]]
        [cljs.core.async :only [<!]])
  (:require [ruin.display :as d]
            [ruin.game :as g]
            [ruin.core :as ruin]
            [ruin.scene :as s]
            [demo.entities :as es]
            [demo.tiles :as tiles]
            [ruin.generate :as generate])
  (:use-macros [cljs.core.async.macros :only [go]])
  (:require-macros [lonocloud.synthread :as ->]))

(def start-scene
  {:render
   (fn [{:keys [display]}]
     (doto display
       (d/draw-text! 1 1 "ClojureScript Roguelike"
                     :foreground "yellow")
       (d/draw-text! 1 2 "Press [Enter] to start!")))

   :go
   (fn [{:keys [key-events] :as game}]
     (go (loop [[event-type key-code] (<! key-events)]
           (if (and (= event-type :down)
                    (= key-code js/ROT.VK_RETURN))
               (g/change-scene game (play-scene))
             (recur (<! key-events))))))})

(ruin/run
  :width 80
  :height 24
  :first-scene (s/create start-scene))
