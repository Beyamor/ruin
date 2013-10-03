(ns ruin.mixin)

(def mixins (atom {}))

(defn realize
  [name]
  (get @mixins name))
