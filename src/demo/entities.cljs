(ns demo.entities
  (:require [ruin.entity :as e]))

(def player
  (e/define
    :name "Player"
    :char "@"
    :foreground "white"
    :background "black"
    :mixins [:player-actor
             :is-player
             :attacker
             :destructible
             :message-recipient]
    :attack-strength 10
    :max-hp 40))

(def fungus
  (e/define
    :name "Fungus"
    :char "F"
    :foreground "green"
    :mixins [:destructible
             :fungus-actor]
    :max-hp 10))
