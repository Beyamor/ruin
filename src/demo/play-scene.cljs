(ns demo.play-scene
  (:require [ruin.game :as g]
            [ruin.display :as d]
            [ruin.core :as ruin]
            [ruin.scene :as s]
            [demo.entities :as es]
            [demo.tiles :as tiles]
            [demo.level :as l]
            [ruin.generate :as generate])
  (:require-macros [lonocloud.synthread :as ->]))

(defn move-player
  [game dx dy]
  (let [player (get-in game [:scene :player])
        new-x (+ (:x player) dx)
        new-y (+ (:y player) dy)
        {updated-player :entity
         updated-level :level} ((:try-move player)
                                  player (:scene game) new-x new-y)]
    (-> game
      (->/when updated-player
               (assoc-in [:scene :player] updated-player))
      (->/when updated-level
               (assoc-in [:scene :level] updated-level)))))

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
               (->/when (= key-code js/ROT.VK_DOWN) (move-player 0 1)))
      (->/aside game
                (g/refresh game)))))

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
        [player-x player-y] (l/random-floor-position level)
        player (-> es/player
                 (assoc :x player-x)
                 (assoc :y player-y))]
    (->PlayScene level player)))


