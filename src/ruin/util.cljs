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

(defn assoc-if-missing
  [m k v]
  (if (contains? m k)
    m
    (assoc m k v)))

(defn dissoc-all
  [m & ks]
  (reduce dissoc m ks))

(defn remove-index
  [v index]
  (vec (concat (subvec v 0 index) (subvec v (inc index)))))
