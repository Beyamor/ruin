(ns ruin.generate)

(defn vec-2d
  [width height]
  (vec
    (for [i (range width)]
      (mapv (constantly 0) (range height)))))

(defn cellular
  [& {:keys [width height iterations val->tile]}]
  (let [generator (js/ROT.Map.Cellular. width height)
        result (atom (vec-2d width height))]
    (.randomize generator 0.5)
    (dotimes [i (dec iterations)]
      (.create generator))
    (.create generator
             (fn [x y v]
               (swap! result assoc-in [x y] (get val->tile v))))
    @result))
