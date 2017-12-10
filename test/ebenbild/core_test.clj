(ns ebenbild.core-test
  (:require [clojure.test :refer :all]
            [ebenbild.core :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

;Any is not good at creating simple stuff, so we add them
;we exclude NaN as it doesn't fulfill the identity prop with = and acts weird as array map key
(def real-any
  (gen/one-of [gen/any gen/int gen/string gen/keyword (gen/double* {:NaN? false})]))

(defspec prop-identity
  200
  (prop/for-all [x real-any]
                (true? ((like x) x))))

(defspec prop-unlike
  200
  (prop/for-all [x real-any]
                (not= ((like x) x) ((unlike x) x))))

(defspec prop-should-not-fail
  200
  (prop/for-all [x real-any
                 y real-any]
                (let [r ((like x) y)]
                  (or (true? r) (false? r)))))

(defspec prop-substring
  200
  (prop/for-all [s1 gen/string
                 s2 gen/string
                 s3 gen/string]
    (and (true? (like? s1 (str s1 s2 s3)))
         (true? (like? s2 (str s1 s2 s3)))
         (true? (like? s3 (str s1 s2 s3))))))

(defspec prop-map
  50
  (prop/for-all [m (gen/not-empty (gen/map real-any real-any))]
                (let [[k v] (first m)]
                  (and ((like {k v}) m)
                       (not ((like {k v}) (dissoc m k)))))))

(defspec like-one-test
         200
         (prop/for-all [x real-any
                        y real-any
                        z real-any]
                       (and (true? (like? (like-one x y z) x))
                            (true? (like? (like-one x y z) y))
                            (true? (like? (like-one x y z) z)))))

(defspec like-all-test
  200
  (prop/for-all [x real-any
                 y real-any
                 z real-any]
    (and (false? (like? (like-all x y z) x))
         (false? (like? (like-all x y z) y))
         (false? (like? (like-all x y z) z)))))

(deftest readme-examples
  (testing "Examples from the Readme"
    (is (true? (like? even? 4)))
    (is (false? (like? even? 5)))
    (is (true? (like? "AB" "elvABuunre")))
    (is (false? (like? "AB" "CANRIBAean")))
    (is (true? (like? #"[a-z]" "a")))
    (is (false? (like? #"[a-z]" "az")))
    (is (true? (like? :a :a)))
    (is (false? (like? :a/a :a)))
    (is (true? (like? :a :a/a)))
    (is (true? (like? :a/a :a/a)))
    (is (false? (like? :b/a :a/a)))
    (is (true? (like? {:a "A"} {:a "BAB" :b 123})))
    (is (true? (like? {:a {:b "A"}} {:a {:b "LAL" :c 1}})))
    (is (false? (like? {:a 1} {:a "A"})))
    (is (true? (like? ANY 1)))
    (is (true? (like? ANY "Foo")))
    (is (true? (like? [1 2 3] '(1 2 3))))
    (is (true? (like? ["1" {:a 1} ["A"]] ["A1A" {:a 1 :b 2 :c 3} ["XA"]])))
    (is (false? (like? [1 2] [1 2 3])))
    (is (true? ((like-one "A" "B" "C") "C")))
    (is (false? ((like-one "A" "B" "C") "E")))
    (is (true? ((like-all "A" "B" "C") "ABC")))
    (is (false? ((like-all "A" "B" "C") "ABD")))
    (let [f (like {:Type (like-one :simple :complex)})]
      (is (true? (f {:Type :simple})))
      (is (true? (f {:Type :complex})))
      (is (false? (f {:Type :other}))))
    (is (true? ((apply like-all ["A" "B" "C"]) "ABC")))
    (is (false? ((apply like-one ["A" "B" "C"]) "D")))))

