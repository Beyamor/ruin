(ns ruin.entities.macros)

(letfn [(expand-body
          [exprs body]
          (reduce
            (fn [body [left right]]
              (case left
                :let `(let ~right ~body)
                :when `(when ~right ~body)))
            body exprs))]

  (defmacro do-each
    [[e es & more-exprs] & body]
    (let [body (expand-body (->> more-exprs (partition 2) reverse) `(do ~@body))]
      `(let [es# (.-list ~es)]
         (dotimes [i# (alength es#)]
           (let [~e (aget es# i#)]
             ~body))))))

(letfn [(expand-body
          [i acc exprs body]
          (reduce
            (fn [body [left right]]
              (case left
                :let `(let ~right ~body)
                :when `(if ~right
                         ~body
                         (recur (inc ~i) ~acc))))
            body exprs))]
  (defmacro each
    [[acc initial e es & more-exprs] & body]
    (let [i (gensym)
          body (expand-body i acc
                            (->> more-exprs (partition 2) reverse)
                            `(recur (inc ~i) (do ~@body)))]
      `(let [es# (.-list ~es)]
         (loop [~i 0 ~acc ~initial]
           (let [~e (aget es# ~i)]
             (if (>= ~i (alength es#))
               ~acc
               ~body)))))))
