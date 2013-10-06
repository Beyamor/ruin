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
         :properties {:food-vaue 2
                      :max-consumptions 2}
         :mixins [:edible])

(defitem :rock
         :glyph {:char "*"
                 :foreground "grey"})

(defitem :corpse
         :glyph {:char "%"}
         :properties {:food-value 75
                      :consumptions 1}
         :mixins [:edible]) 

(defitem :dagger
         :glyph {:char ")"
                 :foreground "gray"}
         :attack 5
         :wieldable? true
         :mixins [:equipable])

(defitem :sword
         :glyph {:char ")"
                 :foreground "white"}
         :attack 10
         :wieldable? true
         :mixins [:equpable])

(defitem :staff
         :glyph {:char ")"
                 :foreground "yellow"}
         :attack 5
         :defense 3
         :wieldable? true
         :mixins [:equipable])

(defitem :tunic
         :character "["
         :foreground "green"
         :defense 2
         :wearable? true
         :mixins [:equipable])

(defitem :chainmail
         :character "["
         :foreground "white"
         :defense 4
         :wearable? true
         :mixins [:equipable])

(defitem :platemail
         :character "["
         :foreground "aliceblue"
         :defense 6
         :wearable? true
         :mixins [:equipable])
