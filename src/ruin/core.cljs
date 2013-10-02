(ns ruin.core
  (:require [ruin.display :as d]))

(set! *print-fn*
      (fn [& args]
        (->> args (map str) (interpose " ") (apply str) (.log js/console))))

(defn- on-window-load
  [f]
  (set! (.-onload js/window) f))

(defn run
  [& {:keys [width height onload]}]
  (on-window-load
    #(cond
       (not (.isSupported js/ROT))
       (js/alert "The rot.js library isn't supported by your browser.")

       :else
       (let [display (d/create width height)
             game {:display display}]
         (.appendChild (.-body js/document) (:container display))
         (onload game)))))
