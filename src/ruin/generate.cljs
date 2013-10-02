(ns ruin.generate
  (:require [ruin.array2d :as a2d]))

(defn cellular
  [& {:keys [width height iterations val->tile]}]
  (let [generator (js/ROT.Map.Cellular. width height)
        result (a2d/create width height)]
    (.randomize generator 0.5)
    (dotimes [i (dec iterations)]
      (.create generator))
    (.create generator
             (fn [x y v]
               (a2d/set! result x y (val->tile v))))
    result))
