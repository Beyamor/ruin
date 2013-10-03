(ns demo.entities
  (:require [ruin.entity :as e]))

(def player
  (e/define
    :char "@"
    :foreground "white"
    :background "black"
    :mixins [:player-actor
             :is-player
             :simple-attacker
             :destructible]))

(def fungus
  (e/define
    :char "F"
    :foreground "green"
    :mixins [:destructible
             :fungus-actor]))
