(ns impedance.transform-test
  (:require
   [clojure.test :as t]
   [impedance.transform :as sut]))

(t/deftest transform
  (t/testing "Transformers of different modes all return the same result."
    (let [f   (sut/transform            {:a {:b ?x :c ?y} "x" ?z} {:x (inc ?x) :y ?y :z ?z})
          f'  (sut/transform :checked   {:a {:b ?x :c ?y} "x" ?z} {:x (inc ?x) :y ?y :z ?z})
          f'' (sut/transform :unchecked {:a {:b ?x :c ?y} "x" ?z} {:x (inc ?x) :y ?y :z ?z})
          in {:a {:b 1 :c 2} "x" 3}
          out {:x 2, :y 2, :z 3}]
      (t/is (= out (f in)))
      (t/is (= out (f' in)))
      (t/is (= out (f'' in))))))
