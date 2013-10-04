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

(defn send-message
  [scene target message]
  (let [id (e/id target)]
    (-> scene
      (->/when (e/has-mixin? target :message-recipient)
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
  (es/update! (:entities scene) updated-entity)
  scene)

(defn update
  [scene {updated-entity :update
          added-entity :add
          removed-entity :remove
          updated-level :update-level
          messages :send
          messages-to-clear :clear-messages
          :as updates}]
  (-> scene
    (->/when updates
             (->/when updated-entity
                      (update-entity updated-entity))
             (->/when updated-level
                      (assoc :level updated-level))
             (->/when removed-entity
                      (remove removed-entity))
             (->/when added-entity
                      (add added-entity))
             (->/when messages-to-clear
                      (clear-messages messages-to-clear))
             (->/when messages
                      (send-messages messages)))))

(defn update-by-mixins
  [scene mixin f]
  (es+/each
    [scene scene
     entity (:entities scene)
     :when (e/has-mixin? entity mixin)]
    (update scene (f entity scene))))
