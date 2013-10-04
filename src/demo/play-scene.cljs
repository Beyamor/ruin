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
            [demo.helpers :as helpers]
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
  #(es/first-with (:entities %) :is-player?))

(defn is-player-id?
  [scene id]
  (= id (e/id (get-player scene))))

(defn print-messages
  [display messages]
  (dotimes [i (count messages)]
    (d/draw-text! display 0 i (get messages i))))

(defn render
  [{{display-width :width display-height :height :as display} :display
    {:keys [entities visible-tiles] {level-width :width level-height :height :as level} :level :as scene} :scene
    :as game}]
  (let [player (get-player scene)
        center-x (:x player)
        center-y (:y player)
        left (-> center-x (- (/ display-width 2)) (max 0) (min (- level-width display-width)))
        top (-> center-y (- (/ display-height 2)) (max 0) (min (- level-height display-height)))]
    (doto display
      (d/draw-tiles! level :left left :top top :screen {:height (dec display-height)}
                    :only :explored? :transform (d/highlight-visible-tiles visible-tiles))
      (d/draw-entities! entities :left left :top top :screen {:height (dec display-height)}
                        :only (d/visible? visible-tiles))
      (d/draw-text! 0 (dec display-height) (str "HP: " (:hp player) "/" (:max-hp player)) )
      (print-messages (s/get-messages scene player)))))

(defn update-visible-tiles
  [{:keys [level] :as scene} player]
  (->>
    (l/fov level)
    (l/visible-tiles level (:x player) (:y player) (:sight-radius player))
    (assoc scene :visible-tiles)))

(defn update-explored-tiles
  [{:keys [visible-tiles level] :as scene}]
  (->>
    (reduce
      (fn [level [x y]]
        (l/mark-as level x y :explored?))
      level visible-tiles)
    (assoc scene :level)))

(defn update-seen-tiles
  [scene player]
  (-> scene
    (update-visible-tiles player)
    update-explored-tiles))

(defn go-play
  [{:keys [key-events display]
    {:keys [scheduler]} :scene
    :as game}]
  (go (loop [actor-id (.next scheduler) game game]
        (if actor-id
          (let [{:keys [entities] :as scene} (:scene game)
                actor (es/get-by-id entities actor-id)
                is-player? (is-player-id? (:scene game) actor-id)]
            (when is-player?
              (g/refresh game))
            (->
              (let [update (e/call actor :act game)
                    update (when update (if (map? update) update (<! update)))]
                (if update
                  (s/update scene update)
                  scene))
              (->/when is-player?
                (update-seen-tiles actor))
              (->> (assoc game :scene))
              (->> (recur (.next scheduler)))))
          (throw (js/Error. "Whoa, ran out of actors to update"))))))

(defn random-free-position
  [{:keys [level] :as scene}]
  (loop [[some-x some-y] (random-floor-position level)]
    (if (helpers/is-empty-floor? scene some-x some-y)
      [some-x some-y]
      (recur (random-floor-position level)))))

(defn add-fungi
  [scene]
  (reduce
    (fn [scene _]
      (let [[x y] (random-free-position scene)]
        (s/add scene
               (-> (fungus)
                 (assoc :x x)
                 (assoc :y y)))))
    scene (range 50)))

(defn play-scene
  []
  (let [width 100
        height 48
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
        scheduler (js/ROT.Scheduler.Simple.)]
    (->
      (s/create
        {:render render
         :go go-play
         :level level
         :scheduler scheduler
         :on-add (fn [{:keys [scheduler] :as scene} entity]
                   (when (e/has-mixin? entity :actor)
                     (.add scheduler (e/id entity) true))
                   scene)
         :on-remove (fn [{:keys [scheduler] :as scene} entity]
                      (when (e/has-mixin? entity :actor)
                        (.remove scheduler (e/id entity)))
                      scene)
         :visible-tiles #{}})
      (s/add player)
      (update-seen-tiles player)
      add-fungi)))
