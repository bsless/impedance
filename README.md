[![Clojars Project](https://img.shields.io/clojars/v/bsless/impedance.svg)](https://clojars.org/bsless/impedance)

# Impedance

Transform Clojure maps at speed.

## Rationale

In electronics, impedance mismatch is a case where an electrical load's
or source's impedance doesn't match that of the driver/sink system
(respectively). Impedance matching allows us to maximize the power
transfer across that boundary.

A similar phenomenon happens in software, where in the boundary between
processes there is a mismatch between the representation model in one
and the other. This library, like a transformer in electronics, allows
us to easily transform an input map to look how our system would expect
and maximize the information flow.

### Just maps

Maps are pervasive in Clojure for information modeling. While you can
cheat and access vectors associatively, the library does not yet support
mapping over sequences.

## Why not Meander?

Meander is great. It's feature rich and beautiful. It is a whole
arsenal. On the other hand, Impedance is a simple hatchet. Good for one
thing, sharp enough, and simple. It's also faster.

### Differences

- No scanning or Cartesian product. Only leaves can be matched.

### Performance differences

As tested with criterium:

```clojure
{:a {:b ?x :c ?y} "x" ?z} ;; from
{:x ?x :y ?y :z ?z} ;; to
;;; Bench transform default
;;;              Execution time mean : 107.636019 ns
;;; Bench transform checked
;;;              Execution time mean : 76.051257 ns
;;; Bench transform unchecked
;;;              Execution time mean : 73.565921 ns
;;; Bench meander
;;;              Execution time mean : 430.781144 ns
```

## Usage

### Dependency

```clojure
[bsless/impedance "0.0.0-alpha"]
```

### A basic transformer

```clojure
(require '[impedance.transform :as t])
(def f (t/transform {:a {:b ?x :c ?y} "x" ?z} {:x (inc ?x) :y ?y :z ?z}))
(f {:a {:b 1 :c 2} "x" 3}) ;; => {:x 2, :y 2, :z 3}
```

### Creating a transformer programmatically

Warning: uses `eval`. Use at your own risk.

```clojure
(def from {:a {:b ?x :c ?y} "x" ?z})
(def to {:x (inc ?x) :y ?y :z ?z})
(def f (t/eval-transform from to))
(f {:a {:b 1 :c 2} "x" 3}) ;; => {:x 2, :y 2, :z 3}
```

### Inferred Context - Poor Man's jq

An alternate syntax for transformations is the inferred context one:

```clojure
(require '[impedance.context :as c])
(c/with-inferred-context ctx
  {:a {:b (or ^:? [:foo :bar] ^:? [:fizz :buzz])
       :c ^:? [:fizz :bazz]}
   :x ^:? [:foo :quux]})
```

Here, specify only the shape of the desired output, and use the `:?`
metadata on vectors to mark them as paths referring to the map `ctx`.

This syntax can be used to create and define functions:

```clojure
(fntx :checked
      {:a {:b (or ^:? [:foo :bar] ^:? [:fizz :buzz])
           :c ^:? [:fizz :bazz]}
       :x ^:? [:foo :quux]})
(defntx f :unchecked
  {:a {:b (or ^:? [:foo :bar] ^:? [:fizz :buzz])
       :c ^:? [:fizz :bazz]}
   :x ^:? [:foo :quux]})
```

### Context compiler modes

All context compilers and transformers receive an optional argument for mode, which can be one of the following:
- `:poly` - will use `clojure.core/get` to get values out of a collection. Highly polymorphic at the slight cost of performance.
- `:unchecked` - will use direct method invoke of `.valAt` on an object. Will throw if an object doesn't implement `ILookup` and NPE for `nil`.
- `:checked` - same as `:unchecked` but `nil` checks before every call to `.valAt`.

## License

Copyright © 2020 Ben Sless

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
