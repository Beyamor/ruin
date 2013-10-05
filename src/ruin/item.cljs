(ns ruin.item)

(def definitions (atom {}))

(defn create
  [item]
  (if-let [item (get @definitions item)]
    item
    (throw (js/Error (str "Unknown item " item)))))

(defn describe
  [item]
  (:name item))

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
