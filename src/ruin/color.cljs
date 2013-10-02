(ns ruin.color)

(defprotocol Color
  (->color [this]))

(extend-protocol Color
  cljs.core.PersistentVector
  (->color [[r g b]]
    (js/ROT.Color.toRGB (array r g b)))

  js/String
  (->color [this]
    this))
