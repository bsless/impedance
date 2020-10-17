(ns impedance.bench
  (:require
   [impedance.transform :refer [transform]]
   [criterium.core :as cc]
   [meander.epsilon :as m]))

(def f (transform {:a {:b ?x :c ?y} "x" ?z} {:x ?x :y ?y :z ?z}))
(def f' (transform :checked {:a {:b ?x :c ?y} "x" ?z} {:x ?x :y ?y :z ?z}))
(def f'' (transform :unchecked {:a {:b ?x :c ?y} "x" ?z} {:x ?x :y ?y :z ?z}))

(defn meander-transform-small
  [in]
  (m/match
    in
    {:a {:b ?x :c ?y} "x" ?z}
    {:x ?x :y ?y :z ?z}))

(def ^:const in-small {:a {:b 1 :c 2} "x" 3})

(defn bench-small
  []
  (println "Bench transform default") (cc/bench (f in-small))
  (println "Bench transform checked") (cc/bench (f' in-small))
  (println "Bench transform unchecked") (cc/bench (f'' in-small))
  (println "Bench meander") (cc/bench (meander-transform-small in-small))
  )

(comment

  (bench-small)
  ;;; Bench transform default
  ;;;              Execution time mean : 107.636019 ns
  ;;; Bench transform checked
  ;;;              Execution time mean : 76.051257 ns
  ;;; Bench transform unchecked
  ;;;              Execution time mean : 73.565921 ns
  ;;; Bench meander
  ;;;              Execution time mean : 430.781144 ns

  )
