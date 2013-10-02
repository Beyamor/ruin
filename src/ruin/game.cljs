(ns ruin.game
  (:require [ruin.display :as d]
            [ruin.screen :as s])
  (:require-macros [lonocloud.synthread :as ->]
                   [cljs.core.async.macros :refer [go]]))

(defn create
  [width height]
  (let [display (d/create width height)]
    {:display display
     :container (:container display)}))

(defn switch-screen
  [{old-screen :screen :as game} new-screen]
  (-> game
    (->/when old-screen
             (->> (s/exit old-screen)))
    (assoc :screen new-screen)
    (->> (s/enter new-screen))
    (->/aside game
              (d/clear! (:display game))
              (s/render new-screen game))))
