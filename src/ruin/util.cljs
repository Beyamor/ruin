(ns ruin.util)

(defn apply-map
  [f m]
  (->> m
    seq
    (apply concat)
    (apply f)))

(defn aremove
  [a v]
  (let [i (.indexOf a v)]
    (when (not= i -1)
      (.splice i 1))
    a))
