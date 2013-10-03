(ns demo.entities
  (:require [demo.mixins :as mixins]
            [ruin.entity :as e]))

(def player
  (e/define
    :char "@"
    :foreground "white"
    :background "black"
    :mixins [mixins/player-actor mixins/is-player mixins/simple-attacker mixins/destructible]))

(def fungus
  (e/define
    :char "F"
    :foreground "green"
    :mixins [mixins/destructible]))
