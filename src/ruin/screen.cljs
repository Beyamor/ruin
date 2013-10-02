(ns ruin.screen
  (:require-macros [lonocloud.synthread :as ->]))

(defprotocol Screen
  (enter [this game])
  (render [this game])
  (exit [this game]))

(defn enter-map
  [{:keys [enter]} game]
  (-> game
    (->/when enter enter)))

(defn render-map
  [{:keys [render]} game]
  (when render
    (render game)))

(defn exit-map
  [{:keys [exit]} game]
  (-> game
    (->/when exit exit)))

(extend-protocol Screen
  cljs.core.PersistentHashMap
  (enter [this game] (enter-map this game))
  (render [this game] (render-map this game))
  (exit [this game] (exit-map this game))

  cljs.core.PersistentArrayMap
  (enter [this game] (enter-map this game))
  (render [this game] (render-map this game))
  (exit [this game] (exit-map this game)))

(defn create
  [& {:as screen}]
  screen)
