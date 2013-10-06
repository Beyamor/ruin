(ns demo.items
  (:use [ruin.mixin :only [defmixin]]
        [ruin.item :only [defitem]]
        [ruin.util :only [assoc-if-missing defaults]])
  (:require-macros [lonocloud.synthread :as ->]))

(defn food-description
  [{:keys [name consumptions max-consumptions]}]
  (if (not= consumptions max-consumptions)
    (str "partly eaten " name)
    name))

(defmixin :edible
          :init #(-> %
                   (defaults
                     :max-consumptions 5
                     :food-value 5
                     :description food-description
                     :edible? true)
                   (->/as i
                          (assoc-if-missing :consumptions (:max-consumptions i)))))

(defmixin :equipable
          :init #(-> %
                   (defaults
                     :attack 0
                     :defense 0
                     :wieldable? false
                     :wearable? false)))

(defitem :apple
         :glyph {:char "@"
                 :foreground "red"}
         :properties {:food-vaue 15
                      :max-consumptions 2}
         :mixins [:edible])

(defitem :pear
         :glyph {:char "&"
                 :foreground "limegreen"}
         :properties {:food-vaue 20
                      :max-consumptions 2}
         :mixins [:edible])

(defitem :rock
         :glyph {:char "*"
                 :foreground "grey"})

(defitem :corpse
         :glyph {:char "%"}
         :properties {:food-value 75
                      :max-consumptions 1}
         :mixins [:edible]) 

(defitem :dagger
         :glyph {:char ")"
                 :foreground "gray"}
         :properties {:attack 5
                      :wieldable? true}
         :mixins [:equipable])

(defitem :sword
         :glyph {:char ")"
                 :foreground "white"}
         :properties {:attack 10
                      :wieldable? true}
         :mixins [:equipable])

(defitem :staff
         :glyph {:char ")"
                 :foreground "yellow"}
         :properties {:attack 5
                      :defense 3
                      :wieldable? true}
         :mixins [:equipable])

(defitem :tunic
         :glyph {:char "["
                 :foreground "green"}
         :properties {:defense 2
                      :wearable? true}
         :mixins [:equipable])

(defitem :chainmail
         :glyph {:char "["
                 :foreground "white"}
         :properties {:defense 4
                      :wearable? true}
         :mixins [:equipable])

(defitem :platemail
         :glyph {:char "["
                 :foreground "aliceblue"}
         :properties {:defense 6
                      :wearable? true}
         :mixins [:equipable])
