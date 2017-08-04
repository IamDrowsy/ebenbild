(ns ebenbild.core
  (:require [clojure.string :as str])
  (:import (clojure.lang Keyword Fn)
           (java.util.regex Pattern)
           (java.util Map)))

(defn update-all
  "Updates all map vals with the given fn"
  [m f]
  (reduce (fn [result k] (update result k f))
          m
          (keys m)))

(def ANY ::any)

(defprotocol EbenbildPred
  (->pred [this]))

(extend-protocol EbenbildPred
  Number
  (->pred [this] (fn [x] (= x this)))
  Keyword
  (->pred [this]
    (cond
      (= ANY this) (constantly true)
      (namespace this)  (fn [x] (if (keyword? x) (= this x) #_(this x)))
      :else (fn [x] (if (keyword? x) (= (name this) (name x)) #_(this x)))))
  Fn
  (->pred [this] this)
  String
  (->pred [this] (fn [x]
                   (and (string? x)
                        (str/includes? x this))))
  Pattern
  (->pred [this]
    (fn [x]
        (and (string? x)
             (re-matches this x))))
  Map
  (->pred [this]
    (let [p-map (update-all this ->pred)
          ks (keys p-map)]
      (fn [x]
        (and (map? x)
             (every? (fn [k]
                       ((get p-map k) (get x k)))
                     ks)))))
  Object
  (->pred [this]
    (fn [x] (= this x)))
  nil
  (->pred [this]
    nil?))

(defn like
  "Creates a predicate from different objects.
   Uses the EbenbildPred Protocol.
   Default Options are:
Fn -> just assumes its already a pred,
String -> matches if the string is included,
Pattern -> matches the exact pattern,
Number -> matches the number
Keyword -> matches other keywords with equal (if given no namespace will match only on name), otherwise calls itself on the arg.
Map -> calls to pred on all keys and matches if all vals of the map are matching.
ANY -> matches everything."
  [data]
  (->pred data))

(defn like?
  "Creates a predicate using 'like' and calls it with the second arg.
  When using the predicate more then one time, consider using 'like' instead."
  [data compare-to]
  ((like data) compare-to))