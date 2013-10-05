(ns ruin.item.macros)

(defmacro defitem
  [item & {:as properties}]
  `(swap! ruin.item.definitions
          assoc ~(keyword item)
          (merge
            {:name ~(name item)}
            ~properties)))
