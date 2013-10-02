(ns demo.core
  (:require [ruin.display :as d]
            [ruin.game :as g]
            [ruin.core :as ruin])
  (:require-macros [lonocloud.synthread :as ->]))

(declare start-scene play-scene win-scene lose-scene)

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
                (g/switch-scene play-scene))))})

(def play-scene
  {:render
   (fn [{:keys [display]}]
     (doto display
       (d/draw-text! 3 4 "This game is so much fun!"
                     :foreground "red" :background "white")
       (d/draw-text! 4 6 "Press [Enter] to win or [Esc] to lose!")))

   :handle-input
   (fn [game [event-type keycode]]
     (-> game
       (->/when (= event-type :key-down)
                (->/when (= keycode js/ROT.VK_RETURN)
                         (g/switch-scene win-scene))
                (->/when (= keycode js/ROT.VK_ESCAPE)
                         (g/switch-scene lose-scene)))))})

(def win-scene
  {:render
   (fn [{:keys [display]}]
     (dotimes [i 22]
       (let [background [(rand-int 256)
                         (rand-int 256)
                         (rand-int 256)]]
         (doto display
           (d/draw-text! 2 (inc i) "You win!"
                         :background background)))))})

(def lose-scene
  {:render
   (fn [{:keys [display]}]
     (dotimes [i 22]
       (doto display
         (d/draw-text! 2 (inc i) "You lose!"
                       :foreground "red"))))})

(ruin/run
  :width 80
  :height 24
  :onload
  (fn [game]
    (-> game
      (g/switch-scene start-scene))))
