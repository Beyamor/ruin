(ns ruin.game
  (:require [ruin.display :as d]
            [ruin.scene :as s])
  (:require-macros [lonocloud.synthread :as ->]
                   [cljs.core.async.macros :refer [go]]))

(defn create
  [width height key-events]
  (let [display (d/create width height)]
    {:display display
     :container (:container display)
     :key-events key-events}))

(defn refresh
  [{:keys [scene display] :as game}]
  (d/clear! display)
  (s/render scene game))

(defn change-scene
  [{old-scene :scene :as game} new-scene]
  (let [new-scene (s/create new-scene)]
    (-> game
      (->/when old-scene
               (->> (s/exit old-scene)))
      (assoc :scene new-scene)
      (assoc :camera {:x 0 :y 0})
      (->> (s/enter new-scene))
      (->/aside game
                (refresh game)))))
