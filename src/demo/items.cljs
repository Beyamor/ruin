(ns demo.items
  (:use [ruin.mixin :only [defmixin]]
        [ruin.item :only [defitem]]
        [ruin.util :only [assoc-if-missing]])
  (:require-macros [lonocloud.synthread :as ->]))

(defn food-description
  [{:keys [name consumptions max-consumptions]}]
  (if (not= consumptions max-consumptions)
    (str "partly eaten " name)
    name))

(defmixin :edible
          :init #(-> %
                   (->> (merge {:max-consumptions 5
                                :food-value 5
                                :description food-description
                                :edible? true}))
                   (->/as i
                          (assoc-if-missing :consumptions (:max-consumptions i)))))

(defitem :apple
         :glyph {:char "@"
                 :foreground "red"}
         :properties {:food-vaue 2
                      :max-consumptions 2}
         :mixins [:edible])

(defitem :rock
         :glyph {:char "*"
                 :foreground "grey"})
