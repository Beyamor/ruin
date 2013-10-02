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
            (->/in [:scene :center]
                   (->/in [:x]
                          (+ dx)
                          (max 0)
                          (min (dec (get-in game [:scene :level :width]))))
                   (->/in [:y]
                          (+ dy)
                          (max 0)
                          (min (dec (get-in game [:scene :level :height])))))))]
  (defrecord PlayScene
    [level center]
    s/Scene
    (enter [_ game] game)
    (exit [_ game] game)

    (render [_ {{display-width :width display-height :height :as display} :display
                {{center-x :x center-y :y} :center
                 {level-width :width level-height :height} :level} :scene
                :as game}]
      (let [left (-> center-x (- (/ display-width 2)) (max 0) (min (- level-width display-width)))
            top (-> center-y (- (/ display-height 2)) (max 0) (min (- level-height display-height)))
            tiles (get-in game [:scene :level :tiles])]
        (doseq [x (range display-width)
                y (range display-height)
                :let [tile (get-in tiles [(+ left x) (+ top y)])]]
          (d/draw-tile! display x y tile))
        (d/draw-char! display (- center-x left) (- center-y top) "@"
                      :foreground "white" :background "black")))

    (handle-input [_ game [event-type key-code]]
      (-> game
        (->/when (= event-type :key-down)
                 (->/when (= key-code js/ROT.VK_LEFT) (move-camera -1 0))
                 (->/when (= key-code js/ROT.VK_RIGHT) (move-camera 1 0))
                 (->/when (= key-code js/ROT.VK_UP) (move-camera 0 -1))
                 (->/when (= key-code js/ROT.VK_DOWN) (move-camera 0 1)))))))

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
                     0 wall-tile})}
      {:x 0 :y 0})))

(ruin/run
  :width 80
  :height 24
  :first-scene start-scene)
