(ns poker.events
  (:require
   [re-frame.core :as re-frame]
   [poker.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
            db/default-db)) ;what is this fn-traced stuff..?

;deprecated by ::update
(re-frame/reg-event-db
 ::change-feild
 (fn [db [_ id feild new-val]]
   (assoc-in db [:players id feild] new-val)))

;deprecated by ::update
(re-frame/reg-event-db
 ::change-winnings
 (fn [db [_ place new-val]]
   (assoc-in db [:winnings place] new-val)))

;multipurpose app-db updater
(re-frame/reg-event-db
 ::update
 (fn [db [_ new-val & path]]
   (assoc-in db (vec path) new-val)))

(re-frame/reg-event-fx
 ::navigate
 (fn-traced [_ [_ handler]]
            {:navigate handler}))

(re-frame/reg-event-fx
 ::set-active-panel
 (fn-traced [{:keys [db]} [_ active-panel]]
            {:db (assoc db :active-panel active-panel)}))


