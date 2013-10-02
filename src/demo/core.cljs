(ns demo.core
  (:require [ruin.display :as d]
            [ruin.game :as g]
            [ruin.core :as ruin]
            [ruin.scene :as s]
            [demo.entities :as es]
            [demo.tiles :as tiles]
            [ruin.generate :as generate])
  (:require-macros [lonocloud.synthread :as ->]))

;
; Tiles
;
; 
; Verbin'
;
(defn random-floor-position
  [level]
  (let [random-x #(rand-int (:width level))
        random-y #(rand-int (:height level))
        tiles (:tiles level)]
    (loop [x (random-x) y (random-y)]
      (if (= tiles/floor-tile (get-in tiles [x y]))
        [x y]
        (recur (random-x) (random-y))))))

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

(letfn [(move-player [game dx dy]
          (let [player (get-in game [:scene :player])
                new-x (+ (:x player) dx)
                new-y (+ (:y player) dy)
                {updated-player :entity
                 updated-level :level} ((:try-move player)
                                          player (get-in game [:scene :level]) new-x new-y)]
            (-> game
              (->/when updated-player
                       (assoc-in [:scene :player] updated-player))
              (->/when updated-level
                       (assoc-in [:scene :level] updated-level)))))]

  (defrecord PlayScene
    [level player]
    s/Scene
    (enter [_ game] game)
    (exit [_ game] game)

    (render [_ {{display-width :width display-height :height :as display} :display
                {{center-x :x center-y :y} :player
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
                 (->/when (= key-code js/ROT.VK_LEFT) (move-player -1 0))
                 (->/when (= key-code js/ROT.VK_RIGHT) (move-player 1 0))
                 (->/when (= key-code js/ROT.VK_UP) (move-player 0 -1))
                 (->/when (= key-code js/ROT.VK_DOWN) (move-player 0 1)))))))

(defn play-scene
  []
  (let [width 200
        height 200
        level {:width width
               :height height
               :tiles
               (generate/cellular
                 :width width
                 :height height
                 :iterations 3
                 :val->tile {1 tiles/floor-tile
                             0 tiles/wall-tile})}
        [player-x player-y] (random-floor-position level)
        player (-> es/player
                 (assoc :x player-x)
                 (assoc :y player-y))]
    (->PlayScene level player)))

(ruin/run
  :width 80
  :height 24
  :first-scene start-scene)
