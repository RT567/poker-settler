(ns poker.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 ::players
 (fn [db]
   (:players db))) ;get the whole players map

(re-frame/reg-sub
 ::winners
 (fn [db]
   (:winners db)))

(re-frame/reg-sub
 ::wins
 (fn [db]
   (:winnings db)))

(re-frame/reg-sub
 ::player-for-id
 (fn [query-v _]
   (re-frame/subscribe [::players])) ;could use sugar
 (fn [players [_ id] _]
   ((keyword (str id)) players)))




;;more complicated calculations

;;calculates 1st place amount
(re-frame/reg-sub
 ::win-amounts
 :<- [::players]
 (fn [players _]
   (reduce (fn [i e] (+ i (:buy e))) 0 (mapv val players))))

;;generate player listings for dropdown
;id's of the listings will be "names"
(re-frame/reg-sub
 ::dropdown-format
 :<- [::players]
 (fn [players _]
   (reduce (fn [i {:keys [name]}] (if (not= "" name) (concat i [{:id name :label name}]) i)) [] (mapv val players))))

;adds a "placement" key/value pair to each map indicating where the player placed (1st 2nd 3rd or tied last)
(defn give-pl [players {:keys [fir sec thi]}]
  (let [give-place (fn [a] (cond
                             (= (:name a) fir) (assoc a :pl 1)
                             (= (:name a) sec) (assoc a :pl 2)
                             (= (:name a) thi) (assoc a :pl 3)
                             :default (assoc a :pl 0)))]
    (map #(give-place %) players)))

;calculates the appropriate "position" for each winner
;e.g. -40 means they should be paid $40
;position of 0 means they are owed nothing and owe no one anything
(defn winner-positions [[& info-in] secon thir]
  (let [firs (reduce (fn [i e] (+ i (:buy e))) 0 info-in) ;total amount
        winner (fn [placement info] (filter #(= placement (:pl %)) info))
        paid (fn [placement info] (if-let [a (not-empty (winner placement info))]
                                    (apply :buy a)
                                    0))
        a (- firs secon thir (paid 1 info-in))
        b (- secon (paid 2 info-in))
        c (- thir (paid 3 info-in))]
    (vector a b c)))

;add positions to all players
;if you weren't in the top 3 placements you lose all your money, e.g. position = - buy-in
;if you were in the top 3, get the appropriate position added to your map
(defn add-pos [winner-amounts info-in]
  (let [find-pos (fn [{:keys [pl buy] :as map}]
                   (cond
                     (= pl 0) (assoc map :position (- 0 buy))
                     (= pl 1) (assoc map :position (first winner-amounts))
                     (= pl 2) (assoc map :position (second winner-amounts))
                     (= pl 3) (assoc map :position (last winner-amounts))))]
    (map #(find-pos %) info-in)))

;this seems like terrible code? I dunno..
;implementing some algorithm that settles all positions not = 0
;also add a :pay key/value to people need to pay others
(defn madness [info-in] 
  (let [keep-em (filter #(= 0 (:position %)) info-in)                          ;store the elements with position = 0 to add them back later
        uh (sort-by #(:position %) (filter #(not= 0 (:position %)) info-in))   ;get rid of 0 elements, sort by amount owed
        losr (first uh)                                                        ;get the person who owes the most
        getr (last uh)                                                         ;get the person who is owed the most
        smaller (min (Math/abs (:position losr)) (Math/abs (:position getr)))  ;determine how much they should transfer
        return (butlast (rest uh))                                             ;take them out of the current list
        updated-losr (update-in (update-in losr [:position] #(+ % smaller)) [:pay] #(concat % (vector (:name getr) smaller))) ;update the losers map
        updated-getr (update-in getr [:position] #(- % smaller))]              ;update the getters map
    (concat (conj return updated-getr updated-losr) keep-em)))                 ;conj together the 0 position people, the getter, the loser and everyone else

;checks if everyone is at a position of 0 for looping
(defn all-square? [info-in]
  (empty? (filter #(not= 0 (:position %)) info-in)))

;make sure the data we have been given isnt nonsense
(defn check [players]
  (let [all-pl (= 6 (apply + (map :pl players)))
        total-pl (zero? (reduce (fn [i e] (+ i (:buy e))) 0 players))]
    all-pl
    ;(and all-pl total-pl)
    ))

;keep applying "madness" until everyone is at a position of 0
(defn final-frontier [secon thir info-in]
  (let [with-pos-sorted (sort-by #(:position %) (add-pos (winner-positions info-in secon thir) info-in))]
    (if (check with-pos-sorted)
      (loop [m with-pos-sorted]
        (if (all-square? m)
          m
          (recur (madness m))))
      (prn "bad input"))))

;create some meaningful output for displaying
(defn collect-payments [input]
  (let [name-pay (map #(vector (:name %) (:pay %)) input)
        non-nil (filter #(second %) name-pay)
        formatted (map (fn [[payer actions]] (map (fn [[p a]] (str payer " pays " p " $" a ".  ")) (partition 2 actions))) non-nil)]
    formatted))

;access for the view
(re-frame/reg-sub
 ::determine-payments
 :<- [::players]
 :<- [::winners]
 :<- [::wins]
 (fn [[players winners wins]]
   (collect-payments (final-frontier (:sec wins) (:thi wins) (give-pl (mapv val players) winners)))))




