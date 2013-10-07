(ns ruin.scene
  (:use [ruin.mixin :only [has-mixin?]]
        [ruin.base :only [deftemplate get-template]]
        [ruin.util :only [apply-map]])
  (:require [ruin.display :as d]
            [ruin.entity :as e]
            [ruin.level :as l]
            [ruin.entities :as es])
  (:require-macros [lonocloud.synthread :as ->]
                   [ruin.entities.macros :as es+])
  (:refer-clojure :exclude [remove]))

(defn defscene
  [name properties]
  (apply-map deftemplate :scene name properties))

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
                    (es/add entity))
             (->/when on-add
                      (on-add entity)))))

(defn remove
  [{:keys [on-remove] :as scene} entity]
  (-> scene
    (->/when entity
             (->/when on-remove
                      (on-remove entity))
             (->/in [:entities]
                    (es/remove entity)))))

(defn create*
  [scene]
  (->
    {:entities (es/create)
     :messages {}}
    (merge scene)))

(def scene-definitions (atom {}))

(defn create
  [scene]
  (create* (get-template :scene scene)))

(defn send-message
  [scene target message]
  (let [id (e/id target)]
    (-> scene
      (->/when (has-mixin? target :message-recipient)
               (update-in [:messages id] (fnil conj []) message)))))

(defn send-messages
  [scene messages]
  (reduce
    (fn [scene [target message]]
      (send-message scene target message))
    scene messages))

(defn get-messages
  [scene target]
  (get-in scene [:messages (e/id target)]))

(defn clear-messages
  [scene target]
  (update-in scene [:messages] dissoc (e/id target)))

(defn- update-entity
  [scene updated-entity]
  (update-in scene [:entities] es/update updated-entity))

(defn update
  [scene instructions]
  (let [instructions (partition 2 instructions)]
    (reduce
      (fn [scene [instruction value]]
        (case instruction
          :add (add scene value)
          :update (update-entity scene value)
          :send (let [[target message] value]
                  (send-message scene target message))
          :clear-messages (clear-messages scene value)
          :remove (remove scene value)
          :drop-item (let [[x y item] value]
                       (update-in scene [:level] l/add-item x y item))
          :update-level (assoc scene :level value)
          scene))
      scene instructions)))

(defn update-by-mixins
  [scene mixin f]
  (es+/each
    [scene scene
     entity (:entities scene)
     :when (has-mixin? entity mixin)]
    (update scene (f entity scene))))
