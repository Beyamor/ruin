(ns ruin.base
  (:use [ruin.util :only [apply-map]]))

;
; Miscellanea
;
(defn glyph
  [& {:keys [char foreground background]
      :or {char " " :foreground "white" :background "black"}}]
  {:char char
   :foreground foreground
   :background background})

(defn tile
  [& {:keys [walkable? diggable?]
      :or {walkable? false diggable? false}
      :as properties}]
  {:glyph (apply-map glyph properties)
   :walkable? walkable?
   :diggable? diggable?})

(def null-tile (tile (glyph :char ".")))
