(ns ruin.entity.macros)

(defmacro defentity
  [entity & {:as definition}]
  `(swap! ruin.entity.definitions
          assoc ~(keyword entity)
          (merge
            {:name ~(name entity)}
            ~definition)))
