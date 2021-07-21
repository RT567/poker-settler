(ns poker.views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com :refer [at v-box h-box box input-text gap single-dropdown]]
   [poker.styles :as styles]
   [poker.events :as events]
   [poker.routes :as routes]
   [poker.subs :as subs]))


;; home

(defn home-title []
    [re-com/title
     :src   (at)
     :label (str "Poker settler")
     :level :level1])

;random garbage
;leaf nodes should just subscribe to teir 3 functions (node functions or something?)
;extractor functions take the map out of the memory
;some other functions run on the data from the extractor functions via subscriptions
;view functions should be subscribed to the "other functions"
;view functions need to be supplying the info to app-db
;view -> event -> effect -> db -> extractor -> node? -> view

;blueprint for creating player elements
;it needs a unique id
;subscribes to the map in players for the given id
;dispatches changes to name and buy feilds of the map for the given id
(defn player [id]
  (fn []
    (let [player-map @(re-frame/subscribe [::subs/player-for-id id])
          sid (keyword (str id))
          {:keys [name buy]} player-map]
      [v-box
       :src (at)
       :width "110px"
       :style {:background-color "#9ec6ff"
               :border-radius "10px"}
       :height "110px"
       :align :center
       :children [[box
                   :child (str "Name")]
                  [input-text
                   :placeholder "enter name"
                   :width "100px"
                   :height "20px"
                   :model name                                       
                   :on-change #(re-frame/dispatch [::events/update % :players sid :name])] ;previously i had seperate event handlers for updating values in app-db but i made it generic? good idea?
                  [re-com/gap :size "10px"]
                  [box
                   :child (str "Buy-in $")]
                  [input-text
                   :width "100px"
                   :height "20px"
                   :model (str buy)
                   :on-change #(re-frame/dispatch [::events/update (js/parseInt %) :players sid :buy ])]
                  [gap :size "5px"]]])))

;calculate 1st amount, ask for 2nd and third amounts with input-texts
(defn winnings [] 
  (fn [] 
    (let [total @(re-frame/subscribe [::subs/win-amounts])
          wins @(re-frame/subscribe [::subs/wins])
         {:keys [sec thi]} wins]
     [v-box
      :gap "5px"
      :align :center
      :children [[gap :size "20px"]
                 [box :child (str "First place: $" (- total sec thi))]
                 [h-box :src (at)
                  :children [[box :child "Second place: $"]
                             [input-text
                              :height "20px"
                              :width "50px"
                              :model (str sec)
                              :on-change #(re-frame/dispatch [::events/update (js/parseInt %) :winnings :sec])]]]
                 [h-box :src (at)
                  :children [[box :child "Third place: $"]
                             [input-text
                              :height "20px"
                              :width "50px"
                              :model (str thi)
                              :on-change #(re-frame/dispatch [::events/update (js/parseInt %) :winnings :thi])]]]]])))

;;ask who came in which position from the given players using dropdowns
;dispatch udpates to the winners section of app-db
(defn winner-selector []
  [v-box 
   :align :center
   :justify :end
   :width "100px"
   :children [[single-dropdown :src (at)
               :width "100px"
               :tooltip "Select winner"
               :tooltip-position :left-center
               :model (str (:fir @(re-frame/subscribe [::subs/winners])))
               :choices @(re-frame/subscribe [::subs/dropdown-format])
               :on-change #(re-frame/dispatch [::events/update % :winners :fir])]
              [single-dropdown :src (at)
               :width "100px"
               :tooltip "Select 2nd place"
               :tooltip-position :left-center
               :model (str (:sec @(re-frame/subscribe [::subs/winners])))
               :choices @(re-frame/subscribe [::subs/dropdown-format])
               :on-change #(re-frame/dispatch [::events/update % :winners :sec])]
              [single-dropdown :src (at)
               :width "100px"
               :tooltip "Select 3rd place"
               :tooltip-position :left-center
               :model (str (:thi @(re-frame/subscribe [::subs/winners])))
               :choices @(re-frame/subscribe [::subs/dropdown-format])
               :on-change #(re-frame/dispatch [::events/update % :winners :thi])]]])

;display all the transfers that should take place
(defn payout-displayer []
  (let [raw @(re-frame/subscribe [::subs/determine-payments])]
    [v-box
     :align :center
     :children (mapv (fn [e] [box :child (apply str e)]) raw)])) ;is this too much for a view element? best practice?

;gross.. hard-coding a "circle"
;display everything in an acceptable format
(defn player-arranger []
  [v-box :src (at)
   :align :center
   :width "800px"
   :size "auto"
   :children [[h-box 
               :align :center
               :children [[player 1]]]
              [h-box
               :width "500px"
               :size "1 0 auto"
               :justify :between
               :children [[player 2]
                          [player 3]]]
              [gap :size "30px"]
              [h-box
               :width "800px"
               :size "1 0 auto"
               :justify :between
               :children [[player 4]
                          [winnings]
                          [winner-selector]
                          [player 5]]]
              [gap :size "70px"]
              [h-box
               :width "800px"
               :size "1 0 auto"
               :justify :between
               :children [[player 6]
                          [payout-displayer]
                          [player 7]]]
              [gap :size "30px"]
              [h-box
               :width "500px"
               :size "1 0 auto"
               :justify :between
               :children [[player 8]
                          [player 9]]]
              [h-box
               :align :center
               :children [[player 10]]]
              ]])

(defn home-panel []
  [re-com/v-box
   :src      (at)
   :gap      "30px"
   :align    :center
   :children [[home-title]
              [player-arranger]]])


(defmethod routes/panels :home-panel [] [home-panel])

;; about

(defn about-title []
  [re-com/title
   :src   (at)
   :label "This is the About Page."
   :level :level1])

(defn link-to-home-page []
  [re-com/hyperlink
   :src      (at)
   :label    "go to Home Page"
   :on-click #(re-frame/dispatch [::events/navigate :home])])

(defn about-panel []
  [re-com/v-box
   :src      (at)
   :gap      "1em"
   :children [[about-title]
              [link-to-home-page]]])

(defmethod routes/panels :about-panel [] [about-panel])

;; main

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [re-com/v-box
     :src      (at)
     :height   "100%"
     :children [(routes/panels @active-panel)]]))
