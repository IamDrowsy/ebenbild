# ebenbild

A tiny library to get predicates that matches look-a-likes for given data. 

## Usage
Ebenbild basically consists of two functions: `like` and `like?`.

* `(like arg)` takes some arg and returns a predicate that matches **look-a-likes** of the given arg.
* `(like? arg compare-to)` generates a predicate using `(like arg)` and runs it calls it on `compare-to`.
 When using a predicate more than one time you should use `like` instead of `like?`.
 
### So what are look-a-likes?
You can extend like by extending the `EbenbildPred` Protocol, but the following behaviour is built in:
 * `Fn` assumes that it's already a predicate and just returns it.
    * `(like? even? 4) => true`
    * `(like? even? 5) => false`
 * `String` matches all strings that includes the given string.
    * `(like? "AB" "elvABuunre") => true`
    * `(like? "AB" "CANRIBAean") => false`
 * `Pattern` matches if the pattern matches.
    * `(like? #"[a-z]" "a") => true`
    * `(like? #"[a-z]" "az") => false`
 * `Map` calls like on all vals (recursively) and matches another map if all keys are contained and their vals match.
    * `(like? {:a "A"} {:a "BAB" :b 123}) => true`
    * `(like? {:a {:b "A"}} {:a {:b "LAL" :c 1}}) true`
    * `(like? {:a 1} {:a "A"}) => false`
 * `ANY` (a symbol in the core namespace), will always return true.
    * `(like? ANY 1) => true`
    * `(like? ANY "Foo") => true`
 * everything else will fall back to a equality check using `=`.
 
## License

Copyright © 2017 Albrecht Schmidt

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
