(ns demo.core
  (:require [ruin.display :as d]
            [ruin.game :as g]
            [ruin.core :as ruin]
            [ruin.scene :as s]
            [ruin.generate :as generate])
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
                (g/change-scene (play-scene)))))})

(defrecord PlayScene
  [level]
  s/Scene
  (enter [_ game] game)

  (render [_ {:keys [display] :as game}]
    (doseq [[[x y] tile] (get-in game [:scene :level])]
      (d/draw-tile! display x y tile)))

  (handle-input [_ game [event-type keycode]]
    (-> game
      (->/when (= event-type :key-down)
               (->/when (= keycode js/ROT.VK_RETURN)
                        (g/change-scene win-scene))
               (->/when (= keycode js/ROT.VK_ESCAPE)
                        (g/change-scene lose-scene)))))

  (exit [_ game] game))

(def floor-tile (ruin/tile
                  (ruin/glyph :char ".")))

(def wall-tile (ruin/tile
                  (ruin/glyph :char "#"
                              :foreground "goldenrod")))

(defn play-scene
  []
  (->PlayScene
    (generate/cellular
      :width 80
      :height 24
      :iterations 3
      :conversion {1 floor-tile
                   0 wall-tile})))

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
  :first-scene start-scene)
