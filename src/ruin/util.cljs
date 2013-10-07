(ns ruin.util)

(defn flatten-map
  [m]
  (->> m seq (apply concat)))

(defn apply-map
  ([f m]
   (apply f (flatten-map m)))
  ([f x m]
   (apply f x (flatten-map m)))
  ([f x y m]
   (apply f x y (flatten-map m))))

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

(def int->letter
  (into {}
        (for [i (range 26)]
          [i (String/fromCharCode (+ i 65))])))

(defn contains-val?
  [coll v]
  (loop [coll coll]
    (if (empty? coll)
      false
      (if (= (first coll) v)
        true
        (recur (rest coll))))))

(defn defaults
  [m & {:as defaults}]
  (merge defaults m))

(defn with-defaults
  [& {:as defaults}]
  (fn [m]
    (merge defaults m)))

(defn filter-map
  [pred? m]
  (select-keys m
               (for [[k v] m
                     :when (pred? v)]
                 k)))

(defn seq->map
  [coll]
  (->> coll (partition 2) hash-map))
