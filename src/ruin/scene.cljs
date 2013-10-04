(ns ruin.scene
  (:require [ruin.display :as d]
            [ruin.entity :as e]
            [ruin.entities :as es])
  (:require-macros [lonocloud.synthread :as ->]
                   [ruin.entities.macros :as es+])
  (:refer-clojure :exclude [remove]))

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

(defn add
  [{:keys [on-add] :as scene} entity]
  (-> scene
    (->/when entity
             (->/in [:entities]
                    (es/add! entity))
             (->/when on-add
                      (on-add entity)))))

(defn remove
  [{:keys [on-remove] :as scene} entity]
  (-> scene
    (->/when entity
             (->/when on-remove
                      (on-remove entity))
             (->/in [:entities]
                    (es/remove! entity)))))

(defn create
  [{:as scene}]
  (->
    {:entities (es/create)
     :messages {}}
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

(defn- update-entity
  [scene updated-entity]
  (es/update! (:entities scene) updated-entity)
  scene)

(defn update
  [scene {:keys [entity-update level-update entity-removal entity-creation]}]
  (-> scene
    (->/when entity-update
             (update-entity entity-update))
    (->/when level-update
             (assoc :level level-update))
    (->/when entity-removal
             (remove entity-removal))
    (->/when entity-creation
             (add entity-creation))))

(defn update-by-mixins
  [scene mixin f]
  (es+/each
    [scene scene
     entity (:entities scene)
     :when (e/has-mixin? entity mixin)]
    (update scene (f entity scene))))

(defn entity-at-position
  [{:keys [entities]} x y]
  (->>
    #(and (= (:x %) x)
          (= (:y %) y))
    (es/first-match entities )))


(defn send-message
  [scene target message]
  (let [id (e/id target)]
    (update-in scene [:messages id] (fnil conj []) message)))
