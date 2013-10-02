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

(defn get-player
  [scene]
  (->> scene
    :entities
    vals
    (filter :is-player?)
    first))

(defn ids-with
  [scene property]
  (for [[id e] (:entities scene)
        :when (contains? e property)]
    id))

(defn move-player
  [game dx dy]
  (->>
    (reduce
      (fn [scene id]
        (let [e (get-in scene [:entities id])
              new-x (+ (:x e) dx)
              new-y (+ (:y e) dy)
              {updated-e :entity
               updated-level :level} ((:try-move e)
                                        e scene new-x new-y)]
          (-> scene
            (->/when updated-e
                     (assoc-in [:entities id] updated-e))
            (->/when updated-level
                     (assoc :level updated-level)))))
      (:scene game) (ids-with (:scene game) :try-move))
    (assoc game :scene)))

(defrecord PlayScene
  [level entities]
  s/Scene
  (enter [_ game] game)
  (exit [_ game] game)

  (render [_ {{display-width :width display-height :height :as display} :display
              {:keys [entities] {level-width :width level-height :height} :level :as scene} :scene
              :as game}]
    (let [player (get-player scene)
          center-x (:x player)
          center-y (:y player)
          left (-> center-x (- (/ display-width 2)) (max 0) (min (- level-width display-width)))
          top (-> center-y (- (/ display-height 2)) (max 0) (min (- level-height display-height)))
          tiles (get-in game [:scene :level :tiles])]
      (doseq [x (range display-width)
              y (range display-height)
              :let [tile (get-in tiles [(+ left x) (+ top y)])]]
        (d/draw-tile! display x y tile))
      (doseq [[id e] entities
              :let [x (:x e)
                    y (:y e)]]
        (d/draw-glyph! display (- x left) (- y top) (:glyph e)))))

  (handle-input [_ game [event-type key-code]]
    (-> game
      (->/when (= event-type :key-down)
               (->/when (= key-code js/ROT.VK_LEFT) (move-player -1 0))
               (->/when (= key-code js/ROT.VK_RIGHT) (move-player 1 0))
               (->/when (= key-code js/ROT.VK_UP) (move-player 0 -1))
               (->/when (= key-code js/ROT.VK_DOWN) (move-player 0 1)))
      (->/aside game
                (g/refresh game)))))

(defn add-entity
  [scene e]
  (update-in scene [:entities] assoc (:id e) e))

(defn entities-at-position
  [scene x y]
  (for [[id e] (:entities scene)
        :when (and (= (:x e) x)
                   (= (:y e) y))]
    e))

(defn entity-at-position?
  [scene x y]
  (loop [es (seq (:entities scene))]
    (if (seq es)
      (let [[[id e] & es] es]
        (if (and (= (:x e) x)
                 (= (:y e) y))
          true
          (recur es)))
      false)))

(defn random-free-position
  [{:keys [level] :as scene}]
  (loop [[some-x some-y] (l/random-floor-position level)]
    (if (entity-at-position? scene some-x some-y)
      (recur (l/random-floor-position level))
      [some-x some-y])))

(defn add-fungi
  [scene]
  (reduce
    (fn [scene _]
      (let [[x y] (random-free-position scene)]
        (add-entity scene
                    (-> (es/fungus)
                      (assoc :x x)
                      (assoc :y y)))))
    scene (range 1000)))

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
        player (-> (es/player)
                 (assoc :x player-x)
                 (assoc :y player-y))]
    (->
      (->PlayScene level {})
      (add-entity player)
      add-fungi)))
