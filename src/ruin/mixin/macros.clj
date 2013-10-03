(ns ruin.mixin.macros)

(defmacro defmixin
  [name & {:as properties}]
  (let [name (keyword name)]
    `(swap! ruin.mixin.mixins
            assoc ~name
            (merge
              {:name ~name}
              ~properties))))
