(ns demo.helpers
  (:require [ruin.scene :as s]
            [ruin.level :as l]
            [ruin.entities :as es]
            [demo.tiles :as ts]))

(defn is-empty-floor?
  [{:keys [level entities]} x y]
  (and (= ts/floor-tile (l/get-tile level x y))
       (not (es/at-position? entities x y))))

(defn kill
  ([{:keys [on-death] :as entity}]
    (if on-death
      (on-death entity)
      [:remove entity]))
  ([entity message]
   (let [result
   (concat
     [:send [entity message]]
     (kill entity))]
     result)))

(defn can-see?
  [{:keys [x y sight-radius]} {target-x :x target-y :y} level]
  (let [dx (- target-x x)
        dy (- target-y y)]
    (when (>= (* sight-radius sight-radius)
              (+ (* dx dx) (* dy dy)))
      (contains?
        (l/visible-tiles level x y sight-radius)
        [target-x target-y]))))
