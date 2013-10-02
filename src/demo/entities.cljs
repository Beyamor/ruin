(ns demo.entities
  (:require [demo.mixins :as mixins]
            [ruin.entity :as e]))

(def player
  (e/create
    :char "@"
    :foreground "white"
    :background "black"
    :mixins [mixins/player-moveable]))

(def fungus
  (e/create
    :char "F"
    :foreground "green"))
