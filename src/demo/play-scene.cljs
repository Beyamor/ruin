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
  [{:keys [entities] :as scene}]
  (loop [i 0]
    (when (< i (alength entities))
      (let [e (aget entities i)]
        (if (:is-player? e)
          e
          (recur (inc i)))))))

(defn move-player
  [game dx dy]
  (let [entities (get-in game [:scene :entities])]
    (->>
      (areduce entities i scene (:scene game)
               (let [e (aget entities i)]
                 (-> scene
                   (->/when (:try-move e)
                            (->/let [new-x (+ (:x e) dx)
                                     new-y (+ (:y e) dy)
                                     {updated-e :entity
                                      updated-level :level} ((:try-move e)
                                                               e scene new-x new-y)]
                                    (->/aside _
                                              (when updated-e
                                                (aset entities i updated-e)))
                                    (->/when updated-level
                                             (assoc :level updated-level)))))))
      (assoc game :scene))))

(defrecord PlayScene
  [level entities]
  s/Scene
  (enter [_ game] game)
  (exit [_ game] game)

  (render [_ {{display-width :width display-height :height :as display} :display
              {:keys [entities] {level-width :width level-height :height :as level} :level :as scene} :scene
              :as game}]
    (let [player (get-player scene)
          center-x (:x player)
          center-y (:y player)
          left (-> center-x (- (/ display-width 2)) (max 0) (min (- level-width display-width)))
          top (-> center-y (- (/ display-height 2)) (max 0) (min (- level-height display-height)))
          tiles (get-in game [:scene :level :tiles])]
      (doseq [x (range display-width)
              y (range display-height)
              :let [tile (l/get-tile level (+ left x) (+ top y))]]
        (d/draw-tile! display x y tile))
      (dotimes [i (alength entities)]
        (let [e (aget entities i)
              x (:x e)
              y (:y e)]
          (when (and (>= x left) (<= x (+ left display-width))
                     (>= y top) (<= y (+ top display-height)))
            (d/draw-glyph! display (- x left) (- y top) (:glyph e)))))))

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
  (.push (:entities scene) e)
  scene)

(defn entities-at-position
  [scene x y]
  (for [[id e] (:entities scene)
        :when (and (= (:x e) x)
                   (= (:y e) y))]
    e))

(defn entity-at-position?
  [scene x y]
  (let [entities (:entities scene)]
    (loop [i 0]
      (if (< i (alength entities))
        (let [e (aget entities i)]
          (if (and (= (:x e) x)
                   (= (:y e) y))
            true
            (recur (inc i))))
        false))))

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
      (->PlayScene level (array))
      (add-entity player)
      add-fungi)))
