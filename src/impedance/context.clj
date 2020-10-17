(ns impedance.context
  (:require
   [impedance.bindings :as b]
   [clojure.walk :as walk]))

(defn- iobj?
  [x]
  (instance? clojure.lang.IObj x))

(defn- extract-paths
  [expr]
  (->> expr
       (tree-seq coll? seq)
       (filter (fn [x] (and (iobj? x) (:? (meta x)))))))

(comment
  (def some-expr '{:a {:b (or ^:? [:foo :bar] ^:? [:fizz :buzz])
                       :c ^:? [:fizz :bazz]}
                   :x ^:? [:foo :quux]})
  (extract-paths some-expr))

(defn- compile-context
  [ctx expr]
  (let [paths (extract-paths expr)
        syms (map b/join-syms paths)
        paths->names (zipmap paths syms)
        compiled-expr (walk/postwalk
                       (fn [x]
                         (if (:? (meta x))
                           (paths->names x)
                           x))
                       expr)
        bindings (b/trie-field->binding ctx paths)]
    `(let [~@bindings]
       ~compiled-expr)))

(comment
  (compile-context
   'ctx
   '{:a {:b (or ^:? [:foo :bar] ^:? [:fizz :buzz])
         :c ^:? [:fizz :bazz]}
     :x ^:? [:foo :quux]}))

(defmacro with-inferred-context
  ([ctx expr]
   (let [mode (or (b/mode (meta expr)) b/*mode*)]
     `(with-inferred-context ~mode ~ctx ~expr)))
  ([mode ctx expr]
   (binding [b/*mode* mode] (compile-context ctx expr))))

(comment
  (with-inferred-context ctx
    {:a {:b (or ^:? [:foo :bar] ^:? [:fizz :buzz])
         :c ^:? [:fizz :bazz]}
     :x ^:? [:foo :quux]})

  (with-inferred-context ctx
    ^:unchecked {:a {:b (or ^:? [:foo :bar] ^:? [:fizz :buzz])
                     :c ^:? [:fizz :bazz]}
                 :x ^:? [:foo :quux]})

  (with-inferred-context :checked ctx
    {:a {:b (or ^:? [:foo :bar] ^:? [:fizz :buzz])
         :c ^:? [:fizz :bazz]}
     :x ^:? [:foo :quux]}))

(defmacro fntx
  ([expr]
   `(fntx ~b/*mode* ~expr))
  ([mode expr]
   (let [g (gensym "ctx__")]
     `(fn [~g]
        (with-inferred-context ~mode ~g ~expr)))))

(comment
  (fntx :checked
    {:a {:b (or ^:? [:foo :bar] ^:? [:fizz :buzz])
         :c ^:? [:fizz :bazz]}
     :x ^:? [:foo :quux]}))

(defmacro defntx
  ([name expr]
   `(defntx ~name ~b/*mode* ~expr))
  ([name mode expr]
   `(def ~name (fntx ~mode ~expr))))

(comment
  (defntx f :unchecked
    {:a {:b (or ^:? [:foo :bar] ^:? [:fizz :buzz])
         :c ^:? [:fizz :bazz]}
     :x ^:? [:foo :quux]}))
