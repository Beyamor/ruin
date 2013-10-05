(ns ruin.item
  (:use [ruin.util :only [apply-map]]
        [ruin.base :only [glyph]]))

(def definitions (atom {}))

(defn create
  [item]
  (if-let [{item-glyph :glyph :as item} (get @definitions item)]
    (-> item
      (assoc :glyph (apply-map glyph item-glyph)))
    (throw (js/Error (str "Unknown item " item)))))

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
