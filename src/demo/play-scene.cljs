(ns demo.play-scene
  (:use [cljs.core.async :only [chan put! <!]])
  (:require [ruin.game :as g]
            [ruin.display :as d]
            [ruin.entity :as e]
            [ruin.core :as ruin]
            [ruin.scene :as s]
            [demo.entities :as es]
            [demo.tiles :as ts]
            [ruin.level :as l]
            [ruin.generate :as generate])
  (:use-macros [ruin.util.macros :only [aside]]
               [cljs.core.async.macros :only [go]])
  (:require-macros [lonocloud.synthread :as ->]
                   [ruin.entities.macros :as es+]))

(defn random-floor-position
  [level]
  (let [random-x #(rand-int (:width level))
        random-y #(rand-int (:height level))
        tiles (:tiles level)]
    (loop [x (random-x) y (random-y)]
      (if (= ts/floor-tile (l/get-tile level x y))
        [x y]
        (recur (random-x) (random-y))))))

(def get-player
  #(s/first-entity-with % :is-player?))

(defn move-moveables
  [game dx dy]
  (->>
    (fn [{:keys [try-move] :as e} scene]
      (let [new-x (+ (:x e) dx)
            new-y (+ (:y e) dy)]
        (try-move e scene new-x new-y)))
    (s/update-by-mixins (:scene game) :moveable)
    (assoc game :scene)))

(defn render
  [{{display-width :width display-height :height :as display} :display
    {:keys [entities] {level-width :width level-height :height :as level} :level :as scene} :scene
    :as game}]
  (let [player (get-player scene)
        center-x (:x player)
        center-y (:y player)
        left (-> center-x (- (/ display-width 2)) (max 0) (min (- level-width display-width)))
        top (-> center-y (- (/ display-height 2)) (max 0) (min (- level-height display-height)))
        tiles (get-in game [:scene :level :tiles])]
    (l/draw-tiles level display :left left :top top)
    (s/draw-entities scene display :left left :top top)))

(defn go-play
  [{:keys [key-events scene]
    {:keys [scheduler]} :scene
    :as game}]
  (go (loop [actor-id (.next scheduler) game game]
        (if actor-id
          (let [actor (s/get-by-id scene actor-id)]
            (->>
              (let [update ((:act actor) actor game)
                    update (if (map? update) update (<! update))]
                (s/update scene update))
              (assoc game :scene)
              (recur (.next scheduler))))
          (throw (js/Error. "Whoa, ran out of actors to update"))))))

;(go (loop [[event-type key-code] (<! key-events) game game]
;      (->>
;        (-> game
;          (->/when (= event-type :down)
;                   (->/when (= key-code js/ROT.VK_LEFT) (move-moveables -1 0))
;                   (->/when (= key-code js/ROT.VK_RIGHT) (move-moveables 1 0))
;                   (->/when (= key-code js/ROT.VK_UP) (move-moveables 0 -1))
;                   (->/when (= key-code js/ROT.VK_DOWN) (move-moveables 0 1)))
;          (->/aside game
;                    (g/refresh game)))
;        (recur (<! key-events))))))

(defn add-entity
  [{:keys [entities scheduler actions] :as scene} entity]
  (.push (:entities scene) entity)
  (when (e/has-mixin? entity :actor)
    (.add scheduler (e/id entity) true))
  scene)

(defn entities-at-position
  [scene x y]
  (for [[id e] (:entities scene)
        :when (and (= (:x e) x)
                   (= (:y e) y))]
    e))

(defn random-free-position
  [{:keys [level] :as scene}]
  (loop [[some-x some-y] (random-floor-position level)]
    (if (s/entity-at-position? scene some-x some-y)
      (recur (random-floor-position level))
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
    scene (range 10)))

(defn play-scene
  []
  (let [width 80
        height 40
        level (l/create width height
                        (generate/cellular
                          :width width
                          :height height
                          :iterations 3
                          :val->tile {1 ts/floor-tile
                                      0 ts/wall-tile}))
        [player-x player-y] (random-floor-position level)
        player (-> (es/player)
                 (assoc :x player-x)
                 (assoc :y player-y))
        scheduler (js/ROT.Scheduler.Simple.)
        actions (chan)]
    (->
      (s/create
        {:render render
         :go go-play
         :level level
         :scheduler scheduler
         :actions actions})
      (add-entity player)
      add-fungi)))
