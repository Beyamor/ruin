(ns demo.items
  (:use-macros [ruin.item.macros :only [defitem]]))

(defitem
  apple
  :char "@"
  :foreground "red")

(defitem
  rock
  :char "*"
  :foreground "grey")
