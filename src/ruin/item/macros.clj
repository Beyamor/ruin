(ns ruin.item.macros)

(defmacro defitem
  [item & {:as glyph}]
  `(swap! ruin.item.definitions
          assoc ~(keyword item)
          {:name ~(name item)
           :glyph ~glyph}))
