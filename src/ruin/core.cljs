(ns ruin.core
  (:use [cljs.core.async :only [chan sliding-buffer put!]])
  (:require [ruin.game :as g]
            [ruin.screen :as s])
  (:require-macros [lonocloud.synthread :as ->]
                   [cljs.core.async.macros :refer [go]]))

(set! *print-fn*
      (fn [& args]
        (->> args (map str) (interpose " ") (apply str) (.log js/console))))

(defn watch-key-events
  []
  (let [events (chan (sliding-buffer 1))
        bind-event (fn [event event-type]
                     (.addEventListener
                       js/window event
                       (fn [e]
                         (put! events
                               [event-type (.-keyCode e)]))))]
    (bind-event "keydown" :key-down)
    (bind-event "keyup" :key-up)
    (bind-event "keypress" :key-press)
    events))


(defn- on-window-load
  [f]
  (set! (.-onload js/window) f))

(defn- handle-input
  [{:keys [screen] :as game} event]
  (if screen
    (s/handle-input screen game event)
    game))

(defn run
  [& {:keys [width height onload]}]
  (on-window-load
    #(cond
       (not (.isSupported js/ROT))
       (js/alert "The rot.js library isn't supported by your browser.")

       :else
       (let [game (atom (g/create width height))
             key-events (watch-key-events)]
         (.appendChild (.-body js/document) (-> @game :container))
         (swap! game onload)
         (go (loop [event (<! key-events)]
               (swap! game handle-input event)
               (recur (<! key-events))))))))
