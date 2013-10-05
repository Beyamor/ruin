(ns ruin.entity
  (:use [ruin.util :only [apply-map dissoc-all]])
  (:require [ruin.mixin :as mixin]
            [ruin.base :as base])
  (:require-macros [lonocloud.synthread :as ->]))

(def definitions (atom {}))

(def id-store (atom 0))

(def next-id #(swap! id-store inc))

(defn add-mixin-properties
  [e mixins]
  (reduce
    (fn [e mixin]
      (reduce
        (fn [e [property value]]
          (-> e
            (->/when (not (or (#{:name :init} property)
                              (contains? e property)))
                     (assoc property value))))
        e mixin))
    e mixins))

(defn init-mixins
  [e mixins]
  (reduce
    (fn [e {:keys [init]}]
      (-> e
        (->/when init
                 (init e))))
    e mixins))

(defn id
  [entity]
  (if (number? entity)
    entity
    (::id entity)))

(defn- create*
  [{:keys [name glyph mixins properties]}]
    (let [mixins (map mixin/realize mixins)]
    (->
             {::id (next-id)
              :x 0
              :y 0
              :name name
              :glyph (apply-map base/glyph glyph)
              :mixins (set
                        (for [mixin mixins]
                          (:name mixin)))
              :mixin-groups (set
                              (for [mixin mixins
                                    :when (:group mixin)]
                                (:group mixin)))}
      (merge properties)
      (add-mixin-properties mixins)
      (init-mixins mixins))))

(defn create
  [entity]
  (if-let [entity (get @definitions entity)]
    (create* entity)
    (throw (js/Error. (str "Unknown entity " entity)))))

(defn has-mixin?
  [e mixin]
  (or (contains? (:mixins e) mixin)
      (contains? (:mixin-groups e) mixin)))

(defn call
  [e property & args]
  (apply (get e property) e args))

(defn pos
  [{:keys [x y]}]
  [x y])

(defn set-pos
  [e x y]
  (-> e (assoc :x x) (assoc :y y)))
