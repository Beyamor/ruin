(ns ruin.item)

(def definitions (atom {}))

(defn create
  [item]
  (if-let [item (get @definitions item)]
    item
    (throw (js/Error (str "Unknown item " item)))))
