(ns demo.entities
  (:require [ruin.entity :as e]))

(def player
  (e/define
    :char "@"
    :foreground "white"
    :background "black"
    :mixins [:player-actor
             :is-player
             :attacker
             :destructible]
    :attack-strength 10
    :max-hp 40))

(def fungus
  (e/define
    :char "F"
    :foreground "green"
    :mixins [:destructible
             :fungus-actor]
    :max-hp 10))
