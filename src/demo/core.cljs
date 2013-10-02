(ns demo.core
  (:require [ruin.display :as d]
            [ruin.core :as ruin]))

(ruin/run
  :width 80
  :height 20
  :onload
  (fn [{:keys [display]}]
    (dotimes [i 15]
      (let [foreground [(- 255 (* i 20))
                        (- 255 (* i 20))
                        (- 255 (* i 20))]
            background [(* i 20) (* i 20) (* i 20)]]
        (doto display
          (d/draw-text! :x 2 :y i :foreground foreground :background background :text "Hello, world!"))))))
