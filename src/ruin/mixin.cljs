(ns ruin.mixin
  (:use [ruin.base :only [deftemplate get-template]]))

(def defmixin (partial deftemplate :mixin))

(defn realize
  [name]
  (get-template :mixin name))

(defn has-mixin?
  [e mixin]
  (and e
       (or (contains? (:mixins e) mixin)
           (contains? (:mixin-groups e) mixin))))
