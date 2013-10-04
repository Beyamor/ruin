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
             :message-recipient
             :sight]
    :attack 10
    :sight-radius 8
    :max-hp 40))

(def fungus
  (e/define
    :name "fungus"
    :char "F"
    :foreground "green"
    :mixins [:destructible
             :fungus-actor]
    :max-hp 10))

(def bat
  (e/define
    :name "bat"
    :char "B"
    :foreground "darkviolet"
    :max-hp 4
    :attack 4
    :mixins [:wander-actor
             :attacker
             :destructible]))

(def newt
  (e/define
    :name "newt"
    :char "N"
    :foreground "orange"
    :max-hp 3
    :attack 2
    :mixins [:wander-actor
             :attacker
             :destructible]))
