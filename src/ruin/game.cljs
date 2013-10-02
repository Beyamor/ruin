(ns ruin.game
  (:require [ruin.display :as d]
            [ruin.scene :as s])
  (:require-macros [lonocloud.synthread :as ->]
                   [cljs.core.async.macros :refer [go]]))

(defn create
  [width height]
  (let [display (d/create width height)]
    {:display display
     :container (:container display)}))

(defn change-scene
  [{old-scene :scene :as game} new-scene]
  (-> game
    (->/when old-scene
             (->> (s/exit old-scene)))
    (assoc :scene new-scene)
    (assoc :camera {:x 0 :y 0})
    (->> (s/enter new-scene))
    (->/aside game
              (d/clear! (:display game))
              (s/render new-scene game))))
