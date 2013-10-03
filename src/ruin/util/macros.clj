(ns ruin.util.macros)

(defmacro aside
  [& body]
  `(fn [x#]
     (do ~@body)
     x#))
