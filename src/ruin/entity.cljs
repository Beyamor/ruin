(ns ruin.entity
  (:use [ruin.util :only [apply-map]])
  (:require [ruin.core :as core])
  (:require-macros [lonocloud.synthread :as ->]))

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

(defn create
  [& {:keys [x y name mixins]
      :or {x 0 y 0 name "" mixins []}
      :as properties}]
  (->
    {:x x
     :y y
     :name name
     :glyph (apply-map core/glyph properties)
     :mixins (set
               (for [mixin mixins]
                 (:name mixin)))
     :mixin-groups (set
                     (for [mixin mixins
                           :when (:group mixin)]
                       (:group mixin)))}
    (add-mixin-properties mixins)
    (init-mixins mixins)))

(defn has-mixin?
  [e mixin]
  (or (contains? (:mixins e) mixin)
      (contains? (:mixin-groups e) mixin)))
