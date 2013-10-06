(ns demo.mixins
  (:use [cljs.core.async :only [<! timeout]]
        [ruin.mixin :only [defmixin has-mixin?]]
        [ruin.util :only [assoc-if-missing defaults filter-map]]
        [demo.helpers :only [kill]])
  (:require [ruin.level :as l]
            [demo.tiles :as ts]
            [ruin.game :as g]
            [ruin.scene :as s]
            [ruin.entity :as e]
            [ruin.entities :as es]
            [demo.helpers :as helpers]
            [demo.screens :as screens]
            [demo.inventory :as inv]
            [ruin.item :as i]
            [demo.hunger :as hunger]
            [ruin.random :as random])
  (:use-macros [cljs.core.async.macros :only [go]])
  (:require-macros [lonocloud.synthread :as ->]))

(defn weapon
  [entity]
  (when (:weapon entity)
    (get (:items entity) (:weapon entity))))

(defn armor
  [entity]
  (when (:armor entity)
    (get (:items entity) (:armor entity))))

(defn dig
  [level x y]
  (-> level
    (->/when (:diggable? (l/get-tile level x y))
             (l/set-tile x y ts/floor-tile))))

(defmixin
  :is-player
  :is-player? true)

(def player-movement-directions
  {js/ROT.VK_LEFT [-1 0]
   js/ROT.VK_RIGHT [1 0]
   js/ROT.VK_UP [0 -1]
   js/ROT.VK_DOWN [0 1]})

(defn try-move
  [entity {:keys [level entities]} dx dy]
  (let [x (+ (:x entity) dx)
        y (+ (:y entity) dy)]
    (let [tile (l/get-tile level x y)
          target (es/first-at-position entities x y)]
      (cond
        ; target and we can attack, do so
        (and target (has-mixin? entity :attacker)
             (or (has-mixin? entity :player-actor) (has-mixin? target :player-actor)))
        (e/call entity :try-attack target)

        ; target and we can't attack, do nothing
        target
        nil

        ; if the tile's free, walk on it
        (:walkable? tile)
        (let [items (l/get-items level x y)]
          (-> [:update (e/set-pos entity x y)]
            (->/when (not (empty? items))
                     (->/let [message (if (= (count items) 1)
                                        (str "You see " (i/describe-a (first items)) ".")
                                        (str "There are several items here."))]
                             (concat [:send [entity message]])))))


        ; otherwise, try digging it
        (and (:diggable? tile)
             (has-mixin? entity :player-actor))
        [:update-level (dig level x y)]))))

(defn continue
  []
  [:action-continues? true])

(defn send-and-continue
  [this message]
  (concat
    [:clear-messages this
     :send [this message]]
    (continue)))

(defn handle-equipping
  [this display key-events caption no-items-message equipment-type slot accessor]
  (go (let [items (filter-map equipment-type (:items this))]
        (if (empty? items)
          (send-and-continue this no-items-message)
          (let [initial-selection (or (slot this) :none)
                response (<! (screens/item-selection
                               items display key-events caption
                               :can-select-none? true
                               :initial-selection initial-selection))]
            (cond
              (= response :cancel)
              (continue)

              (= response :none)
              (if (nil? (slot this))
                (continue)
                [:update (assoc this slot nil)
                 :clear-messages this
                 :send [this (str "Unequipped the " (i/describe (accessor this)) ".")]]) 

              :else
              (let [item (get (:items this) response)]
                [:update (assoc this slot response)
                 :clear-messages this
                 :send [this (str "Equipped the " (i/describe item))]])))))))

(defmixin
  :player-actor
  :group :actor
  :act (fn [{:keys [x y] :as this}
            {:keys [key-events display]
             {:keys [level] :as scene} :scene
             :as game}]
         (let [this (hunger/apply this)]
           (cond
             ; check for starvation
             (hunger/starved? this)
             (hunger/kill this)

             ; otherwise, perform an action
             :else
             (go (loop [[event-type key-code shift?] (<! key-events)]
                   (if (= event-type :down)
                     (cond
                       ; movement
                       (contains? player-movement-directions key-code)
                       (let [[dx dy] (get player-movement-directions key-code)]
                         (concat
                           [:clear-messages this]
                           (try-move this scene dx dy)))

                       ; inventory viewing
                       (= key-code js/ROT.VK_I)
                       (do
                         (g/refresh game)
                         (<! (screens/item-viewing (:items this) display key-events "Inventory"))
                         [:action-continues? true])

                       ; dropping things
                       (= key-code js/ROT.VK_D)
                       (if-not (empty? (:items this))
                         (do (g/refresh game)
                           (let [response (<! (screens/multiple-item-selection
                                                (:items this) display key-events "Choose the items you wish to drop"))]
                             (if (or (= response :cancel)
                                     (empty? response))
                               (continue)
                               (let [[this level] (inv/drop-multiple this level response)]
                                 [:update this
                                  :update-level level]))))
                         (send-and-continue this "You have nothing to drop."))

                       ; picking things up
                       (= key-code js/ROT.VK_COMMA)
                       (let [items-on-tile (zipmap (range) (l/get-items level x y))]
                         (cond
                           (empty? items-on-tile)
                           (send-and-continue this "There is nothing to pick up.")

                           (= 1 (count items-on-tile))
                           (let [item (l/get-item level (:x this) (:y this) 0)
                                 [this updated-level succeeded?] (inv/pick-up this level 0)]
                             (if succeeded?
                               [:update this
                                :update-level updated-level
                                :clear-messages this
                                :send [this (str "You picked up " (i/describe-a item) ".")]]
                               (send-and-continue this "Inventory is full.")))

                           :else
                           (let [response (<! (screens/multiple-item-selection
                                                items-on-tile display key-events "Choose the items you which to pick up"))]
                             (if (or (= :cancel response)
                                     (empty? response))
                               (continue)
                               (let [[this level got-all?] (inv/pick-up-multiple this level response)]
                                 (-> [:update this
                                      :update-level level
                                      :clear-messages this
                                      :send [this "You picked up the items."]]
                                   (->/when (not got-all?)
                                            (concat
                                              [:send [[this "Inventory is full. Not all items were picked up."]]]))))))))

                       ; eating things
                       (= key-code js/ROT.VK_E)
                       (let [edible-items (filter-map :edible? (:items this))]
                         (if-not (empty? edible-items)
                           (let [response (<! (screens/item-selection
                                                edible-items display key-events "Choose the item you wish to eat"))]
                             (if (= response :cancel)
                               (continue)
                               (hunger/eat this response)))
                           (send-and-continue this "Nothing to eat.")))

                       ; wielding crap
                       (and (not shift?) (= key-code js/ROT.VK_W))
                       (<! (handle-equipping this display key-events
                                             "Choose the item you wish to wield"
                                             "Nothing to wield."
                                             :wieldable? :weapon weapon))
                       ; wearing carp
                       (and shift? (= key-code js/ROT.VK_W))
                       (<! (handle-equipping this display key-events
                                             "Choose the item you wish to wear"
                                             "Nothing to wear."
                                             :wearable? :armor armor))

                       :else
                       (recur (<! key-events)))
                     (recur (<! key-events)))))))))

(defmixin
  :fungus-actor
  :group :actor
  :init #(-> %
           (defaults :growths 5))
  :act (fn [{:keys [growths] :as this} {:keys [scene]}]
         (when (and (pos? growths)
                    (<= (rand) 0.02))
           (let [x (+ (:x this) -1 (rand-int 3))
                 y (+ (:y this) -1 (rand-int 3))]
             (when (helpers/is-empty-floor? scene x y)
               (apply concat
                      [:update (-> this
                                 (update-in [:growths] dec))
                       :add (-> (e/create :fungus)
                              (e/set-pos x y))]
                      (for [entity (es/nearby (:entities scene) x y)]
                        [:send [entity "The fungus is spreading!"]])))))))

(def init-health #(-> %
                    (defaults :max-hp 10
                              :defense 0)
                    (->/as e
                           (assoc-if-missing :hp (:max-hp e)))))

(defn try-drop-corpse
  [{:keys [x y corpse-drop-rate] :as entity}]
  (when (< (* 100 (Math/random)) corpse-drop-rate)
    [:drop-item [x y (-> (i/create :corpse)
                       (assoc-in [:glyph :foreground]
                                 (get-in entity [:glyph :foreground])))]]))

(defn take-damage
  [this attacker damage]
  (let [new-hp (- (:hp this) damage)]
    (if (> new-hp 0)
      [:update (assoc this :hp new-hp)]
      (concat
        [:update (assoc this :hp 0)
         :send [attacker (str "You kill the " (:name this) ".")]]
        (when (has-mixin? this :corpse-dropper)
          (try-drop-corpse this))
        (kill this)))))

(defmixin
  :destructible
  :init init-health
  :group :destructible)

(defmixin
  :destructible-player
  :init init-health
  :group :destructible
  :on-death (fn [this]
              [:send [this "You have died... Press [Enter] to continue"]
               :player-killed? true]))

(defmixin
  :attacker
  :group :attacker
  :init #(-> %
           (defaults
             :attack 1))
  :try-attack (fn [this target]
                (when (has-mixin? target :destructible)
                  (let [equipper? (has-mixin? this :equipper)
                        total-attack (-> (:attack this)
                                       (->/when (and equipper? (weapon this))
                                                (+ (:attack (weapon this))))
                                       (->/when (and equipper? (armor this))
                                                (+ (:attack (armor this)))))
                        equpper? (has-mixin? target :equipper)
                        total-defense (-> (:defense this)
                                        (->/when (and equipper? (weapon this))
                                                 (+ (:defense (weapon this))))
                                        (->/when (and equipper? (armor this))
                                                 (+ (:defense (armor this)))))
                        damage (->
                                 (- total-attack total-defense)
                                 (max 0)
                                 rand-int
                                 inc)]
                    (concat
                      [:send [this (str "You strike the " (:name target) " for " damage " damage!")]
                       :send [target (str "The " (:name this) " strikes you for " damage " damage!")]]
                      (take-damage target this damage))))))

(defmixin
  :message-recipient
  :get-messages (fn [this scene]
                  (s/get-messages scene this)))

(defmixin
  :sight
  :group :sight
  :init #(-> % 
           (defaults
             :sight-radius 5)))

(defmixin
  :wander-actor
  :group :actor
  :act (fn [this {:keys [scene]}]
         (let [[dx dy] (if (random/coin-flip)
                         [(random/plus-minus) 0]
                         [0 (random/plus-minus)])]
           (try-move this scene dx dy))))

(defmixin
  :inventory-holder
  :init #(-> %
           (defaults
             :inventory-size 10
             :items {})))

(defmixin
  :eater
  :init #(-> %
           (defaults
             :max-fullness 100
             :hunger 1)
           (->/as e
                  (assoc-if-missing :fullness (/ (:max-fullness e) 2)))))

(defmixin
  :corpse-dropper
  :init #(-> %
           (defaults
             :corpse-drop-rate 100)))

(defmixin
  :equipper)
