(ns ruin.display
  (:require [ruin.color :as color]))

(defn create
  [width height]
  (let [display (js/ROT.Display. (js-obj "width" width "height" height))
        container (.getContainer display)]
    {:display display
     :container container
     :width width
     :height height}))

(defn- colored-text
  [foreground background text]
  (str "%c{" (color/->color foreground) "}%b{" (color/->color background) "}" text))

(defn draw-text!
  [{:keys [display]} x y text
   & {:keys [foreground background]
      :or {foreground "white" background "black"}}]
  (.drawText display x y
             (colored-text foreground background text)))

(defn clear!
  [{:keys [display]}]
  (.clear display))

(defn draw-char!
  [{:keys [display]} x y char & {{:keys [foreground background]} :glyph}]
  (.draw display x y char foreground background))

(defn draw-tile!
  [{:keys [display]} x y {{:keys [char foreground background]} :glyph}]
  (.draw display x y char foreground background))
