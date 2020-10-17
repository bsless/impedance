(ns impedance.transform
  (:require
   [impedance.bindings :as b]))

(comment
  (defmacro defnormalizer
    "map from->to"
    {:indentation 'def}
    ([name mode m]
     {:pre (map? m)}
     (let [arg (with-meta (gensym "rec__") {:tag "clojure.lang.IPersistentMap"})
           targets (keys m)
           sources (vals m)
           aliases (zipmap
                    (repeatedly (count m) (comp keyword gensym))
                    (map b/maybe-wrap targets))
           bindings (binding [b/*mode* mode] (b/aliased-fields->bindings arg aliases))
           building (zipmap (map symbol (keys aliases)) (map b/maybe-wrap sources))
           body
           (reduce-kv
            (fn [m a path]
              (assoc-in m path a))
            {}
            building)]
       `(defn ~name
          [~arg]
          (let [~@bindings]
            ~body))))))

(comment
  (defnormalizer foo :unchecked
    {:a :A
     :foo [:bar :bazz]
     :fizz [:bar :buzz]
     :B :b
     [:x :y] :z
     [:x :w] :u}))

(defn- map-or-vec?
  [x]
  (or (map? x) (vector? x)))

;;; https://www.reddit.com/r/Clojure/comments/7wl7nt/get_a_list_of_all_paths_in_a_nested_map/

(defn- paths
  ([m]
   (paths [] [] m))
  ([ps ks m]
   (reduce-kv
    (fn [ps k v]
      (if (map-or-vec? v)
        (paths ps (conj ks k) v)
        (conj ps (conj ks k v))))
    ps
    m)))

(comment
  (paths
   '{:a {:b ?x
         :c ?y}
     "x" ?z}))

(defn- pattern->aliases
  [pattern]
  (let [ps (paths pattern)
        paths (map (comp vec butlast) ps)
        aliases (map last ps)]
    (zipmap aliases paths)))

(comment
  (pattern->aliases
   '{:a {:b ?x
         :c ?y}
     "x" ?z}))

(defn- emit-transform
  "Generates code for a function which transforms from `from` map shape to `to`.
  Can generate different modes of transformer:
  - `:poly`: uses `get` to extract values from maps.
  - `:unchecked`: uses .valAt for ILookup access directly. Will NPE on `nil`.
  - `:checked`: like unchecked but with nil checks."
  [mode from to]
  (let [arg (with-meta (gensym "rec__") {:tag "clojure.lang.IPersistentMap"})
        aliases (pattern->aliases from)
        bindings (binding [b/*mode* mode] (b/aliased-fields->bindings arg aliases))]
    `(fn
       [~arg]
       (let [~@bindings]
         ~to))))

(defn eval-transform
  "Like [[transform]] but allows creating a transformer at run time.
  Evil warning: uses `eval`."
  [mode from to]
  (eval (emit-transform mode from to)))

(defmacro transform
  "Return a function which transforms from `from` map shape to `to`.
  Can generate different modes of transformer:
  - `:poly`: uses `get` to extract values from maps.
  - `:unchecked`: uses .valAt for ILookup access directly. Will NPE on `nil`.
  - `:checked`: like unchecked but with nil checks."
  ([from to]
   (emit-transform :poly from to))
  ([mode from to]
   (emit-transform mode from to)))

(comment
  (emit-transform
   :poly
   '{:a {:b ?x
         :c ?y}
     "x" ?z}
   '{:x ?x :y ?y :z ?z})

  (def in {:a {:b 1 :c 2} "x" 3})

  (def f (eval-transform :poly '{:a {:b ?x :c ?y} "x" ?z} '{:x (inc ?x) :y ?y :z ?z}))

  (f in))
