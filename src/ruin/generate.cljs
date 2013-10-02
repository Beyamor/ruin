(ns ruin.generate)

(defn cellular
  [& {:keys [width height iterations val->tile]}]
  (let [generator (js/ROT.Map.Cellular. width height)
        result (atom {})]
    (.randomize generator 0.5)
    (dotimes [i (dec iterations)]
      (.create generator))
    (.create generator
             (fn [x y v]
               (swap! result assoc [x y] (get val->tile v))))
    @result))
