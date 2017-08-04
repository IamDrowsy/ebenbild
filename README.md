# ebenbild

A tiny library to get predicates from example data. 

Can be used to match maps by examples.
To find all persons whose name includes "Bob" you could write 
```clojure
(def persons [{:name "Bob" :age 31} {:name "Al" :age 32} {:name "Cory" :age 44}])

(filter (like {:name "Bob"}) persons)
=> ({:name "Bob" :age 31})
```

## Usage
Ebenbild consists of two functions: `like` and `like?`.

* `(like arg)` returns a predicate that matches **look-a-likes** of the given arg.
* `(like? arg compare-to)` generates a predicate using `(like arg)` and runs it calls it on `compare-to`.
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
 
 You can extend `like` by extending the `EbenbildPred` Protocol, but the following behaviour is built in:

 
## License

Copyright © 2017 Albrecht Schmidt

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
