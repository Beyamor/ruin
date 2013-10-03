(ns ruin.scene
  (:require [ruin.display :as d]
            [ruin.entity :as e])
  (:require-macros [lonocloud.synthread :as ->]
                   [ruin.entities.macros :as es+]))

(defn enter
  [{:keys [enter]
    :or {enter identity}} game]
  (enter game))

(defn exit
  [{:keys [exit]
    :or {exit identity}} game]
  (exit game))

(defn go
  [{:keys [go]} game]
  (if go
    (go game)
    (throw (js/Error. "Whoa, scene needs to go, baby"))))

(defn render
  [{:keys [render]} game]
  (when render
    (render game)))

(defn create
  [{:as scene}]
  (->
    {:entities (array)}
    (merge scene)))

(defn draw-entities
  [scene
   {display-width :width display-height :height :as display}
   & {:keys [left top]
      :or {left 0 :right 0}}]
  (es+/do-each [e (:entities scene)
                :let [x (:x e)
                      y (:y e)]
                :when (and (>= x left) (<= x (+ left display-width))
                           (>= y top) (<= y (+ top display-height)))]
               (d/draw-glyph! display (- x left) (- y top) (:glyph e))))

(defn first-entity-with
  [{:keys [entities]} property]
  (loop [i 0]
    (when (< i (alength entities))
      (let [e (aget entities i)]
        (if (contains? e property)
          e
          (recur (inc i)))))))

(defn- update-entity-at-index
  [scene index updated-entity]
  (aset (:entities scene) index updated-entity)
  scene)

(defn entity-index
  [{:keys [entities]} entity]
  (let [id (e/id entity)]
    (loop [i 0]
      (when (< i (alength entities))
             (let [entity (aget entities i)]
               (if (= (e/id entity) id)
                 i
                 (recur (inc i))))))))

(defn- update-entity
  [scene updated-entity]
  (update-entity-at-index
    scene
    (entity-index scene updated-entity)
    updated-entity))

(defn update
  [scene {:keys [entity-update level-update]}]
  (-> scene
    (->/when entity-update
             (update-entity entity-update))
    (->/when level-update
             (assoc :level level-update))))

(defn update-by-mixins
  [scene mixin f]
  (let [entities (:entities scene)]
    (loop [i 0 scene scene]
      (if (< i (alength entities))
        (let [entity (aget entities i)]
          (->>
            (-> scene
              (->/when (e/has-mixin? entity mixin)
                       (->/let [{:keys [update-entity update-level]} (f entity scene)]
                               (->/when update-entity
                                        (update-entity-at-index i update-entity))
                               (->/when update-level
                                        (assoc :level update-level)))))
            (recur (inc i))))
        scene))))

(defn entity-at-position?
  [{:keys [entities]} x y]
  (loop [i 0]
    (if (< i (alength entities))
      (let [entity (aget entities i)]
        (if (and (= (:x entity) x)
                 (= (:y entity) y))
          true
          (recur (inc i))))
      false)))

(defn get-by-id
  [{:keys [entities]} id]
  (loop [i 0]
    (when (< i (alength entities))
      (let [entity (aget entities i)]
        (if (= (e/id entity) id)
          entity
          (recur (inc i)))))))
