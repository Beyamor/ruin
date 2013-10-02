(ns demo.core
  (:require [ruin.display :as d]
            [ruin.game :as g]
            [ruin.core :as ruin]))

(def start-screen
  {:enter (fn [game]
            (print "Entered start screen")
            game)
   :exit (fn [game]
           (print "Exited start screen"))
   :render (fn [{:keys [display]}]
             (doto display
               (d/draw-text! 1 1 "ClojureScript Roguelike"
                             :foreground "yellow")
               (d/draw-text! 1 2 "Press [Enter] to start!")))})_

(ruin/run
  :width 80
  :height 20
  :onload
  (fn [game]
    (-> game
      (g/switch-screen start-screen))))
