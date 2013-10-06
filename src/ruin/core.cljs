(ns ruin.core
  (:use [cljs.core.async :only [chan sliding-buffer put!]]
        [ruin.util :only [apply-map]])
  (:require [ruin.game :as g]
            [ruin.scene :as s])
  (:require-macros [lonocloud.synthread :as ->]
                   [cljs.core.async.macros :refer [go]]))

(set! *print-fn*
      (fn [& args]
        (->> args (map str) (interpose " ") (apply str) (.log js/console))))

(defn watch-key-events
  []
  (let [events (chan (sliding-buffer 2))
        bind-event (fn [event event-type]
                     (.addEventListener
                       js/window event
                       (fn [e]
                         (put! events
                               [event-type (.-keyCode e) (.-shiftKey e)]))))]
    (bind-event "keydown" :down)
    (bind-event "keyup" :up)
    (bind-event "keypress" :press)
    events))


(defn- on-window-load
  [f]
  (set! (.-onload js/window) f))

(defn- handle-input
  [{:keys [scene] :as game} event]
  (if scene
    (s/handle-input scene game event)
    game))

(defn run
  [& {:keys [width height first-scene]}]
  (on-window-load
    #(cond
       (not (.isSupported js/ROT))
       (js/alert "The rot.js library isn't supported by your browser.")

       :else
       (let [key-events (watch-key-events)
             game (g/create width height key-events)]
         (.appendChild (.-body js/document) (-> game :container))
         (go (loop [game (g/change-scene game first-scene)]
               (recur (<! (s/go (:scene game) game)))))))))
