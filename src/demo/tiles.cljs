(ns demo.tiles
  (:require [ruin.base :as ruin]))

(def floor-tile (ruin/tile
                  :char "."
                  :walkable? true))

(def wall-tile (ruin/tile
                 :char "#"
                 :foreground "goldenrod"
                 :diggable? true))
