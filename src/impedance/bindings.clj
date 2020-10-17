(ns impedance.bindings
  (:require
   [clojure.edn :as edn]
   [clojure.string :as str]))

(defn- all-subvecs
  ([xs]
   (all-subvecs xs []))
  ([xs tot]
   (if (seq xs)
     (recur (butlast xs) (conj tot (vec xs)))
     tot)))

(def types-prefix
  {clojure.lang.Keyword "#k_"
   clojure.lang.Symbol "#s_"
   java.lang.String "#S_"})

(def prefix-types
  {"#k_" keyword
   "#s_" symbol
   "#S_" identity
   "#e_" edn/read-string})

(defn- expand-key
  [k]
  (if-let [p (get types-prefix (type k))]
    (symbol (str p (name k)))
    k))

(defn join-syms
  [syms]
  (symbol (str/join "_" (map expand-key syms))))

(defn- demunge-key
  [k]
  (if-let [t (reduce-kv
              (fn [_ p t]
                (when (str/starts-with? k p)
                  (reduced t)))
              nil
              prefix-types)]
    (t (subs k 3))
    (keyword k)))

(comment
  (demunge-key "#k_foo")
  (demunge-key "#s_foo")
  (demunge-key "#S_foo")
  (demunge-key "#e_foo")
  (type (demunge-key "#e_1"))
  (demunge-key "S_foo"))


;;; Bindings

(def ^:dynamic *mode* :poly)

(defn mode
  [{:keys [poly checked unchecked]}]
  (cond
    poly :poly
    checked :checked
    unchecked :unchecked
    :else :poly))

(defn- add-binding
  [bs to from k]
  (conj bs (symbol to)
        (case *mode*
          :poly
          (if (or (keyword? k) (symbol? k))
            `(~k ~from)
            `(get ~from ~k))
          :unchecked
          `(.valAt ~(with-meta from {:tag "clojure.lang.ILookup"}) ~k)
          :checked
          `(if (nil? ~from) nil (.valAt ~(with-meta from {:tag "clojure.lang.ILookup"}) ~k)))))

(defn- trie-field->binding*
  "Emit binding expression for get from path tail, i.e.
  [:a :b :c] -> [a_b_c (get a_b :c)]."
  [ctx bs ks]
  (if (= 1 (count ks))
    (let [k (first ks)]
      (add-binding bs (expand-key k) ctx k))
    (let [root (butlast ks)
          bound (join-syms root)
          will (join-syms ks)
          tail (last ks)]
      (add-binding bs will bound tail))))

(defn trie-field->binding
  "Compile symbol `ctx` and paths `fields` to minimal extraction let
  binding."
  [ctx fields]
  (reduce
   (partial trie-field->binding* ctx)
   []
   (sort-by
    count
    (set
     (mapcat all-subvecs fields)))))

(defn aliased-fields->bindings
  "Compile symbol `ctx` and map of `alias` -> `path` to minimal let
  bindings."
  [ctx mks]
  (let [init (trie-field->binding ctx (vals mks))]
    (loop [bs init
           aliases mks]
      (if (seq aliases)
        (let [[f ks] (first aliases)]
          (recur (conj bs (symbol f) (join-syms ks)) (rest aliases)))
        bs))))

(comment
  (aliased-fields->bindings 'ctx {:f1 [:a :b :c]
                                  :f2 [:a :b :d]
                                  :f3 [:a :x :y]
                                  :f4 [:a :x "z"]}))

(defn maybe-wrap
  [x]
  (if (sequential? x)
    x
    [x]))
