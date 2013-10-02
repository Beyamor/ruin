(ns ruin.display
  (:require [ruin.color :as color]))

(defn create
  [width height]
  (let [display (js/ROT.Display. (js-obj "width" width "height" height))
        container (.getContainer display)]
    {:display display
     :container container}))

(defn- colored-text
  [foreground background text]
  (str "%c{" (color/->rgb foreground) "}%b{" (color/->rgb background) "}" text))

(defn draw-text!
  [{:keys [display]} & {:keys [x y xy foreground background text]
                        :or {foreground "white" background "black"}}]
  (let [x (or (first xy) x 0)
        y (or (first xy) y 0)]
    (.drawText display x y
               (colored-text foreground background text))))
