(ns poker.db)

(def default-db
  {:players  {:1 {:pl 0 :name "example"  :buy 10}} ;stores player information
   :winnings {:sec 20 :thi 10}                     ;stores 2nd and third place winnings amounts (first is calculated)
   :winners  {:fir "" :sec "" :thi ""}})           ;store winners names for dropdowns
