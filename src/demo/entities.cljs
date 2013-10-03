(ns demo.entities
  (:require [demo.mixins :as mixins]
            [ruin.entity :as e]))

(def player
  (e/define
    :char "@"
    :foreground "white"
    :background "black"
    :mixins [mixins/player-moveable mixins/is-player]))

(def fungus
  (e/define
    :char "F"
    :foreground "green"
    :mixins [mixins/fungus-actor]))
