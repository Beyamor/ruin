(ns ruin.util)

(defn apply-map
  [f m]
  (->> m
    seq
    (apply concat)
    (apply f)))
