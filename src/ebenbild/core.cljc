(ns ebenbild.core
  (:require [clojure.string :as str])
  #?(:clj (:import (clojure.lang Keyword Fn IPersistentVector)
                   (java.util.regex Pattern)
                   (java.util Map))))

(defn update-all
  "Updates all map vals with the given fn"
  [m f]
  (reduce (fn [result k] (update result k f))
          m
          (keys m)))

(def ANY ::any)

(defprotocol EbenbildPred
  (->pred [this]))

(defn- pred-vec
  "Returns a vector of predicates."
  [datas]
  (vec (map ->pred datas)))

(defn map->pred [this]
  (let [p-map (update-all this ->pred)
        ks (keys p-map)]
    (fn [x]
      (if
        (and (map? x)
             (every? (fn [k]
                       ((get p-map k) (get x k)))
                     ks))
        true
        false))))


(extend-protocol EbenbildPred
  #?(:clj java.lang.Number :cljs number)
  (->pred [this] (fn [x] (= x this)))
  #?(:clj clojure.lang.Keyword :cljs cljs.core/Keyword)
  (->pred [this]
    (cond
      (= ANY this) (constantly true)
      (namespace this)  (fn [x] (if (keyword? x) (= this x) false))
      :else (fn [x] (if (keyword? x) (= (name this) (name x)) false))))
  #?(:clj clojure.lang.Fn :cljs function)
  (->pred [this] this)
  #?(:clj java.lang.String :cljs string)
  (->pred [this] (fn [x]
                   (and (string? x)
                        (str/includes? x this))))
  #?(:clj java.util.regex.Pattern :cljs js/RegExp)
  (->pred [this]
    (fn [x]
        (if
          (and (string? x) (re-matches this x))
          true
          false)))
  #?(:clj java.util.Map :cljs cljs.core/PersistentArrayMap)
  (->pred [this]
    (map->pred this))
  #?@(:cljs [cljs.core/PersistentHashMap
             (->pred [this] (map->pred this))
             cljs.core/PersistentTreeMap
             (->pred [this] (map->pred this))])
  #?(:clj clojure.lang.IPersistentVector :cljs cljs.core/PersistentVector)
  (->pred [this]
    (let [p-vec (pred-vec this)]
      (fn [x]
        (if
          (and (sequential? x) (= (count p-vec) (count x))
               (every? identity (map (fn [p x] (p x)) p-vec x)))
          true
          false))))
  #?(:clj Object :cljs default)
  (->pred [this]
    (fn [x] (= this x)))
  nil
  (->pred [this]
    nil?))

(defn like
  "Creates a predicate from different objects.
   Uses the EbenbildPred Protocol.

   Default Options are:
* Fn -> just assumes its already a pred,
* String -> matches if the string is included,
* Pattern -> matches the exact pattern,
* Number -> matches the number
* Keyword -> matches other keywords with equal (if given no namespace will match only on name).
* Map -> calls like on all keys and matches if all vals of the map are matching.
* Vector -> calls like on all elements, matches all seqs of the same size whose elements match the given vector.
* ANY -> matches everything."
  [data]
  (->pred data))

(defn like?
  "Creates a predicate using 'like' and calls it with the second arg.
  When using the predicate more then one time, you should use 'like'."
  [data compare-to]
  ((like data) compare-to))

(defn unlike
  "Returns a predicate that is the complement of (like data)"
  [data]
  (complement (like data)))

(defn unlike?
  "Creates a predicate using 'unlike' and calls it with the second arg.
  When using the predictae more then one time, you should use 'unlike'"
  [data compare-to]
  ((unlike data) compare-to))

(defn like-one
  "Creates a predicate that checks if (at least) one of the given data matches (using like).
  It is semanticly equivalent to (or (like data1) (like data2) ...)."
  [& datas]
  (let [preds (pred-vec datas)]
    (fn [x]
      (loop [[p & ps] preds]
        (cond
          (and p (p x)) #_=> true
          (nil? p)      #_=> false
          :else         #_=> (recur ps))))))

(defn like-all
  "Creates a predicate that check if all of the given data matches (using like)
  It is semanticly equivalent to (and (like data1) (like data2) ...)."
  [& datas]
  (let [preds (pred-vec datas)]
    (fn [x]
      (loop [[p & ps] preds]
        (cond
          (and p (p x)) #_=> (recur ps)
          (nil? p)      #_=> true
          :else         #_=> false)))))

#_ (do (require 'cljs.repl)
       (require 'cljs.repl.node)
       (cemerick.piggieback/cljs-repl (cljs.repl.node/repl-env)))