(ns demo.entities
  (:use [ruin.entity :only [defentity]])
  (:require [ruin.entity :as e]))

(defentity :player
           :glyph {:char "@"}
           :mixins [:player-actor
                    :is-player
                    :attacker
                    :destructible-player
                    :message-recipient
                    :sight
                    :inventory-holder
                    :eater
                    :equipper]
           :properties {:attack 10
                        :sight-radius 8
                        :max-hp 10
                        :inventory-size 3
                        :max-fullness 500})

(defentity :fungus
           :name "fungus"
           :glyph {:char "F"
                   :foreground "green"}
           :mixins [:destructible
                    :fungus-actor]
           :properties {:max-hp 10})

(defentity :bat
           :glyph { :char "B"
                   :foreground "darkviolet"}
           :properties {:max-hp 4
                        :attack 4}
           :mixins [:task-actor
                    :attacker
                    :destructible
                    :corpse-dropper])

(defentity :newt
           :glyph {:char "N"
                   :foreground "orange"}
           :properties {:max-hp 3
                        :attack 2}
           :mixins [:task-actor
                    :attacker
                    :destructible
                    :corpse-dropper])

(defentity :kobold
           :glyph {:char "K"
                   :foreground "skyblue"}
           :properties {:max-hp 6
                        :attack 4
                        :sight-radius 5
                        :tasks [:hunt :wander]}
           :mixins [:task-actor
                    :sight
                    :attacker
                    :destructible
                    :corpse-dropper])
