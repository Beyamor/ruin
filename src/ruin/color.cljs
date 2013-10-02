(ns ruin.color)

(defprotocol Color
  (->rgb [this]))

(extend-protocol Color
  cljs.core.PersistentVector
  (->rgb [[r g b]]
    (js/ROT.Color.toRGB (array r g b))))
