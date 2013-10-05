(ns ruin.base
  (:use [ruin.util :only [apply-map]])
  (:require-macros [lonocloud.synthread :as ->]))

(def templates (atom {}))

(defn deftemplate
  [category title & {:as properties}]
  (swap! templates assoc-in [category (keyword title)]
         (merge {:name (name title)}
                properties))) 

(defn get-template 
  [category title]
  (if-let [template (get-in @templates [category title])]
    template
    (throw (js/Error. (str "Unrecognized template: " category " - " title)))))

(defn add-mixin-properties
  [e mixins]
  (reduce
    (fn [e mixin]
      (reduce
        (fn [e [property value]]
          (-> e
            (->/when (not (or (#{:name :init} property)
                              (contains? e property)))
                     (assoc property value))))
        e mixin))
    e mixins))

(defn init-mixins
  [e mixins]
  (reduce
    (fn [e {:keys [init]}]
      (-> e
        (->/when init
                 (init e))))
    e mixins))

(defn glyph
  [& {:keys [char foreground background]
      :or {char " " :foreground "white" :background "black"}}]
  {:char char
   :foreground foreground
   :background background})

(defn tile
  [& {:keys [walkable? diggable? blocks-light?]
      :or {walkable? false diggable? false}
      :as properties}]
  {:glyph (apply-map glyph properties)
   :walkable? walkable?
   :diggable? diggable?
   :blocks-light? blocks-light?})

(def null-tile (tile (glyph :char ".")))
