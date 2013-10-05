(ns ruin.scene.macros)

(defmacro defscene
  [scene body]
  `(swap! ruin.scene.scene-definitions
          assoc ~(keyword scene)
          (fn []
            (let [scene# ~body]
              (if (map? scene#)
                scene#
                (throw (js/Error. (str "Whoa, " ~(keyword scene) " is not a scene"))))))))
