(ns demo.play-scene
  (:use [cljs.core.async :only [chan put! <!]]
        [ruin.util :only [aremove]]
        [demo.entities :only [player fungus]])
  (:require [ruin.game :as g]
            [ruin.display :as d]
            [ruin.entity :as e]
            [ruin.core :as ruin]
            [ruin.scene :as s]
            [ruin.entities :as es]
            [demo.tiles :as ts]
            [ruin.level :as l]
            [ruin.generate :as generate])
  (:use-macros [ruin.util.macros :only [aside]]
               [cljs.core.async.macros :only [go]])
  (:require-macros [lonocloud.synthread :as ->]
                   [ruin.entities.macros :as es+]))

(defn is-empty-floor?
  [scene x y]
  (and (= ts/floor-tile (l/get-tile (:level scene) x y))
       (not (s/entity-at-position scene x y))))

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
  #(es/first-with (:entities %) :is-player?))

(defn is-player-id?
  [scene id]
  (= id (e/id (get-player scene))))

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
  [{:keys [key-events]
    {:keys [scheduler]} :scene
    :as game}]
  (go (loop [actor-id (.next scheduler) game game]
        (if actor-id
          (let [{:keys [entities] :as scene} (:scene game)
                actor (es/get-by-id entities actor-id)
                is-player? (is-player-id? (:scene game) actor-id)]
            (when is-player?
              (g/refresh game))
            (->>
              (let [update (e/call actor :act game)
                    update (if (map? update) update (<! update))]
                (s/update scene update))
              (assoc game :scene)
              (recur (.next scheduler))))
          (throw (js/Error. "Whoa, ran out of actors to update"))))))

(defn add-entity
  [{:keys [entities scheduler] :as scene} entity]
  (es/add! entities entity)
  (when (e/has-mixin? entity :actor)
    (.add scheduler (e/id entity) true))
  scene)

(defn remove-entity
  [{:keys [entities scheduler] :as scene} entity]
  (es/remove! entities entity)
  (when (e/has-mixin? entity :actor)
    (.remove scheduler (e/id entity)))
  scene)

(defn random-free-position
  [{:keys [level] :as scene}]
  (loop [[some-x some-y] (random-floor-position level)]
    (if (is-empty-floor? scene some-x some-y)
      [some-x some-y]
      (recur (random-floor-position level)))))

(defn add-fungi
  [scene]
  (reduce
    (fn [scene _]
      (let [[x y] (random-free-position scene)]
        (add-entity scene
                    (-> (fungus)
                      (assoc :x x)
                      (assoc :y y)))))
    scene (range 50)))

(defn play-scene
  []
  (let [width 200
        height 200
        level (l/create width height
                        (generate/cellular
                          :width width
                          :height height
                          :iterations 3
                          :val->tile {1 ts/floor-tile
                                      0 ts/wall-tile}))
        [player-x player-y] (random-floor-position level)
        player (-> (player)
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
