(ns ruin.scene
  (:require-macros [lonocloud.synthread :as ->]))

(defprotocol Scene
  (enter [this game])
  (render [this game])
  (exit [this game])
  (handle-input [this game input]))

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

(defn handle-input-map
  [{:keys [handle-input]} game event]
  (-> game
    (->/when handle-input (handle-input event))))

(extend-protocol Scene
  cljs.core.PersistentHashMap
  (enter [this game] (enter-map this game))
  (render [this game] (render-map this game))
  (exit [this game] (exit-map this game))
  (handle-input [this game event] (handle-input-map this game event))

  cljs.core.PersistentArrayMap
  (enter [this game] (enter-map this game))
  (render [this game] (render-map this game))
  (exit [this game] (exit-map this game))
  (handle-input [this game event] (handle-input-map this game event)))

(defn create
  [& {:as scene}]
  scene)
