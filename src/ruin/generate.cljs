(ns ruin.generate)

(defn array-2d
  [width height]
  (let [tiles (array)]
    (dotimes [x width]
      (.push tiles (array))
      (dotimes [y height]
        (.push (aget tiles x) nil)))
    tiles))

(defn cellular
  [& {:keys [width height iterations val->tile]}]
  (let [generator (js/ROT.Map.Cellular. width height)
        result (array-2d width height)]
    (.randomize generator 0.5)
    (dotimes [i (dec iterations)]
      (.create generator))
    (.create generator
             (fn [x y v]
               (aset (aget result x) y (get val->tile v))))
    result))
