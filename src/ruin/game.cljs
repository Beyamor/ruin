(ns ruin.game
  (:use [cljs.core.async :only [chan sliding-buffer put!]])
  (:require [ruin.display :as d]
            [ruin.scene :as s])
  (:require-macros [lonocloud.synthread :as ->]
                   [cljs.core.async.macros :refer [go]]))

;
;   ACTUAL GAME STUFF
;
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

;
;   AND THEN LIKE
;   THE JUNK WHAT ACTUALLY GETS IT GOING
;
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
                         (.preventDefault e)
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
             game (create width height key-events)]
         (.appendChild (.-body js/document) (-> game :container))
         (go (loop [game (change-scene game first-scene)]
               (recur (<! (s/go (:scene game) game)))))))))
