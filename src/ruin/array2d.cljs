(ns ruin.array2d
  (:refer-clojure :exclude [get set!]))

(defn width
  [arr]
  (alength arr))

(defn height
  [arr]
  (alength (aget width 0)))

(defn set!
  [arr x y value]
  (aset (aget arr x) y value)
  arr)

(defn get
  [arr x y]
  (aget (aget arr x) y))

(defn create
  [width height]
  (let [arr (array)]
    (dotimes [x width]
      (.push arr (array))
      (dotimes [y height]
        (.push (aget arr x) nil)))
    arr))
