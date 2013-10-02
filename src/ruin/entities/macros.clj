(ns ruin.entities.macros)

(defn- expand-body
  [exprs body]
  (reduce
    (fn [body [left right]]
      (case left
        :let `(let ~right ~body)
        :when `(when ~right ~body)))
    body exprs))

(defmacro do-each
  [exprs & body]
  (let [e (first exprs)
        es (second exprs)
        more-exprs (rest (rest exprs))
        body (expand-body (->> more-exprs (partition 2) reverse) `(do ~@body))]
    `(let [es# ~es]
       (dotimes [i# (alength es#)]
         (let [~e (aget es# i#)]
           ~body)))))
