(ns demo.core
  (:use [demo.play-scene :only [play-scene]])
  (:require [ruin.display :as d]
            [ruin.game :as g]
            [ruin.core :as ruin]
            [ruin.scene :as s]
            [demo.entities :as es]
            [demo.tiles :as tiles]
            [demo.level :as l]
            [ruin.generate :as generate])
  (:require-macros [lonocloud.synthread :as ->]))

(def start-scene
  {:render
   (fn [{:keys [display]}]
     (doto display
       (d/draw-text! 1 1 "ClojureScript Roguelike"
                     :foreground "yellow")
       (d/draw-text! 1 2 "Press [Enter] to start!")))

   :handle-input
   (fn [game [event-type keycode]]
     (-> game
       (->/when (and (= event-type :key-down)
                     (= keycode js/ROT.VK_RETURN))
                (g/change-scene (play-scene)))))})

(ruin/run
  :width 80
  :height 24
  :first-scene (s/create start-scene))
