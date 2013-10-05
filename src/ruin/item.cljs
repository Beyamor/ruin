(ns ruin.item
  (:use [ruin.util :only [apply-map]]
        [ruin.base :only [deftemplate get-template init-mixins add-mixin-properties]])
  (:require [ruin.base :as base]
            [ruin.mixin :as mixin]))

(def defitem (partial deftemplate :item))

(defn create
  [item]
  (let [{:keys [name mixins properties glyph]
         :as item} (get-template :item item)
        mixins (map mixin/realize mixins)]
    (->
      {:name name
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

(defn describe
  [{:keys [description name] :as item}]
  (if description
    (if (string? description)
      description
      (description item))
    name))

(defn describe-a
  ([item capitalize?]
   (let [an? (#{"a" "e" "i" "o" "u"}
                  (.. (:name item) (charAt 0) toLowerCase))
         prefix (if capitalize?
                  (if an? "An" "A")
                  (if an? "an" "a"))]
     (str prefix " " (:name item))))
  ([item]
   (describe-a item false)))
