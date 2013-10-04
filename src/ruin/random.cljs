(ns ruin.random)

(def coin-flip #(-> (js/Math.random) (js/Math.round) (= 1)))

(def plus-minus #(if (coin-flip) 1 -1))
