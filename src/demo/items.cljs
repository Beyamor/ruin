(ns demo.items
  (:use-macros [ruin.item.macros :only [defitem]]))

(defn food-description
  [{:keys [name consumptions max-consumptions]}]
  (if (not= consumptions max-consumptions)
    (str "partly eaten " name)
    name))

(defitem
  apple
  :glyph {:char "@"
          :foreground "red"}
  :edible true
  :food-value 5
  :consumptions 2
  :max-consumptions 2
  :description food-description)

(defitem
  rock
  :glyph {:char "*"
          :foreground "grey"})
