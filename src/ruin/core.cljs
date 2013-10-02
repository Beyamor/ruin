(ns ruin.core
  (:use [cljs.core.async :only [chan sliding-buffer put!]]
        [ruin.util :only [apply-map]])
  (:require [ruin.game :as g]
            [ruin.scene :as s])
  (:require-macros [lonocloud.synthread :as ->]
                   [cljs.core.async.macros :refer [go]]))

;
; Miscellanea
;
(defn glyph
  [& {:keys [char foreground background]
      :or {char " " :foreground "white" :background "black"}}]
  {:char char
   :foreground foreground
   :background background})

(defn tile
  [& {:keys [walkable? diggable?]
      :or {walkable? false diggable? false}
      :as properties}]
  {:glyph (apply-map glyph properties)
   :walkable? walkable?
   :diggable? diggable?})

(def null-tile (tile (glyph :char ".")))

;
; Core engine structure
;

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
       (let [game (atom (g/create width height))
             key-events (watch-key-events)]
         (.appendChild (.-body js/document) (-> @game :container))
         (swap! game g/change-scene first-scene)
         (go (loop [event (<! key-events)]
               (swap! game handle-input event)
               (recur (<! key-events))))))))
