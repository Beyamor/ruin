(ns demo.tiles
  (:require [ruin.core :as ruin]))

(def floor-tile (ruin/tile
                  :char "."
                  :walkable? true))

(def wall-tile (ruin/tile
                 :char "#"
                 :foreground "goldenrod"
                 :diggable? true))
