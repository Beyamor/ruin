(ns ruin.entity
  (:use [ruin.util :only [apply-map dissoc-all]]
        [ruin.base :only [deftemplate get-template init-mixins add-mixin-properties]])
  (:require [ruin.mixin :as mixin]
            [ruin.base :as base])
  (:require-macros [lonocloud.synthread :as ->]))

(def defentity (partial deftemplate :entity))

(def id-store (atom 0))

(def next-id #(swap! id-store inc))

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
                          (keyword (:name mixin))))
              :mixin-groups (set
                              (for [mixin mixins
                                    :when (:group mixin)]
                                (keyword (:group mixin))))}
      (merge properties)
      (add-mixin-properties mixins)
      (init-mixins mixins))))

(defn create
  [entity]
  (create* (get-template :entity entity)))

(defn call
  [e property & args]
  (apply (get e property) e args))

(defn pos
  [{:keys [x y]}]
  [x y])

(defn set-pos
  [e x y]
  (-> e (assoc :x x) (assoc :y y)))
