(ns demo.core
  (:require [ruin.display :as d]
            [ruin.game :as g]
            [ruin.core :as ruin]
            [ruin.scene :as s]
            [ruin.generate :as generate])
  (:require-macros [lonocloud.synthread :as ->]))

;
; Tiles
;
(def floor-tile (ruin/tile
                  (ruin/glyph :char ".")))

(def wall-tile (ruin/tile
                 (ruin/glyph :char "#"
                             :foreground "goldenrod")))

;
; Scenes
;
(declare start-scene play-scene)

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

(letfn [(move-camera [game dx dy]
          (-> game
            (->/in [:camera]
                   (->/in [:x]
                          (+ dx)
                          (max 0)
                          (min (get-in game [:scene :level :width])))
                   (->/in [:y]
                          (+ dy)
                          (max 0)
                          (min (get-in game [:scene :level :height]))))))]
  (defrecord PlayScene
    [level]
    s/Scene
    (enter [_ game] game)
    (render [_ {:keys [display] {cam-x :x cam-y :y} :camera :as game}]
      (let [min-x cam-x
            max-x (+ cam-x (:width display))
            min-y cam-y
            max-y (+ cam-y (:height display))
            tiles (get-in game [:scene :level :tiles])]
        (dotimes [x (- max-x min-x)]
          (dotimes [y (- max-y min-y)]
            (let [tile (get tiles [(+ x min-x) (+ y min-y)])]
              (d/draw-tile! display x y tile))))))
    (handle-input [_ game [event-type key-code]]
      (-> game
        (->/when (= event-type :key-down)
                 (->/when (= key-code js/ROT.VK_LEFT) (move-camera -1 0))
                 (->/when (= key-code js/ROT.VK_RIGHT) (move-camera 1 0))
                 (->/when (= key-code js/ROT.VK_UP) (move-camera 0 -1))
                 (->/when (= key-code js/ROT.VK_DOWN) (move-camera 0 1)))))
    (exit [_ game] game)))

(defn play-scene
  []
  (let [width 200
        height 200]
    (->PlayScene
      {:width width
       :height height
       :tiles
       (generate/cellular
         :width width
         :height height
         :iterations 3
         :val->tile {1 floor-tile
                     0 wall-tile})})))

(ruin/run
  :width 80
  :height 24
  :first-scene start-scene)
