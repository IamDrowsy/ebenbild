# ebenbild

[![cljdoc badge](https://cljdoc.xyz/badge/ebenbild/ebenbild)](https://cljdoc.xyz/d/ebenbild/ebenbild/CURRENT)

A tiny library to get predicates from example data. 

Can be used to match maps by examples.
To find all persons whose name includes "Bob" you could write 
```clojure
(def persons [{:name "Bob" :age 31} {:name "Al" :age 32} {:name "Cory" :age 44}])

(filter (like {:name "Bob"}) persons)
=> ({:name "Bob" :age 31})
```

## Usage
Simply add the following entry to your `:dependencies`:
```
[ebenbild "0.1.1"]
```

Ebenbild consists of two core functions: `like` and `like?`.

* `(like arg)` returns a predicate that matches **look-a-likes** of the given arg.
* `(like? arg compare-to)` generates a predicate using `(like arg)` and calls it on `compare-to`.
 **When using a predicate more than one time you should use `like` instead of `like?`.**
 
### So what are look-a-likes?
Depending on the given arg, `like` will generate predicates that match as follows:

 * `Fn` assumes that it's already a predicate and just returns it.
    * `(like? even? 4) => true`
    * `(like? even? 5) => false`
 * `String` matches all strings that includes the given string.
    * `(like? "AB" "elvABuunre") => true`
    * `(like? "AB" "CANRIBAean") => false`
 * `Pattern` matches if the pattern matches.
    * `(like? #"[a-z]" "a") => true`
    * `(like? #"[a-z]" "az") => false`
 * `Keyword` matches the same keyword, if given no namespace matches all keywords with the same name
    * `(like? :a :a) => true`
    * `(like? :a/a :a) => false`
    * `(like? :a :a/a) => true`
    * `(like? :a/a :a/a) => true`
 * `Map` calls like on all vals (recursively) and matches another map if all keys are contained and their vals match.
    * `(like? {:a "A"} {:a "BAB" :b 123}) => true`
    * `(like? {:a {:b "A"}} {:a {:b "LAL" :c 1}})  => true`
    * `(like? {:a 1} {:a "A"}) => false`
 * `IPersistentVector` calls like on all entries (recursively) and matches any sequential with the same number of elements 
 where all entries match the corresponding predicate.
    * `(like? [1 2 3] '(1 2 3)) => true`
    * `(like? ["1" {:a 1} ["A"]] ["A1A" {:a 1 :b 2 :c 3} ["XA"]]) => true`  
    * `(like? [1 2] [1 2 3]) => false`
 * `ANY` (a symbol in the core namespace), will always return true.
    * `(like? ANY 1) => true`
    * `(like? ANY "Foo") => true`
 * everything else will fall back to a equality check using `=`.
 
 You can extend `like` by extending the `EbenbildPred` Protocol.

### Other functions
Ebenbild provides some functions often used with like.

* `(unlike x)` the complement of `(like x)`
* `(like-one x y z ...)` returns a predicate that matches if any given data is a look-a-like
* `(like-all x y z ...)` returns a predicate that matches if all given data is a look-a-like
 
### Properties of ebenbild predicates
The following properties should hold for every predicate created with `like`. 
Please create an issue if you find some edge case that doesn't.

1. **Identity:** `(like? x x)`/`((like x) x)` is always true.
    1. with the exception of functions and regexes
2. **Just Predicates:** The predicate always returns `true` or `false`
3. **Failsafe:** The predicate throws no errors.


### Further notices
Because `like/unlike/like-one/like-all` returns predicates and `(like predicate)` 
returns the predicate itself, you can nest different `like`. For example
```clojure
(like {:Type (like-one :simple :complex))
``` 
returns a predicate that matches every map that has a `:Type` key with either `:simple` 
or `:complex` as the value.

Because `like-one/like-all` are functions (unlike clojure.core `or/and`) you can
apply them on seqs of data to get a predictate that matches look-a-likes for one or all of them.
 
## License

Copyright © 2017 Albrecht Schmidt

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
