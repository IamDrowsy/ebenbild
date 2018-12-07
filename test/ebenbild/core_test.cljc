(ns ebenbild.core-test
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :as test :refer-macros [deftest testing is run-tests]])
            [ebenbild.core :as e]
            [clojure.test.check :as tc]
            #?(:clj [clojure.test.check.clojure-test :refer [defspec]]
               :cljs [clojure.test.check.clojure-test :refer-macros [defspec]])
            [clojure.test.check.generators :as gen]
            #?(:clj [clojure.test.check.properties :as prop :refer [for-all]]
               :cljs [clojure.test.check.properties :as prop :refer-macros [for-all]])))

;Any is not good at creating simple stuff, so we add them
;we exclude NaN as it doesn't fulfill the identity prop with = and acts weird as array map key
(def my-any
  (gen/such-that #(not (and (double? %)
                            (#?(:clj Double/isNaN :cljs js/isNaN) %)))
                 (gen/one-of [gen/any gen/int gen/string gen/keyword gen/double])))

(def number-test-runs
  #?(:clj 200
     :cljs 100))

(defspec prop-identity
  number-test-runs
  (for-all [x my-any]
           (true? ((e/like x) x))))

(defspec prop-unlike
  number-test-runs
  (for-all [x my-any]
           (not= ((e/like x) x) ((e/unlike x) x))))

(defspec prop-should-not-fail
  number-test-runs
  (for-all [x my-any
            y my-any]
           (let [r ((e/like x) y)]
             (or (true? r) (false? r)))))

(defspec prop-substring
  number-test-runs
  (for-all [s1 gen/string
            s2 gen/string
            s3 gen/string]
    (and (true? (e/like? s1 (str s1 s2 s3)))
         (true? (e/like? s2 (str s1 s2 s3)))
         (true? (e/like? s3 (str s1 s2 s3))))))

(defspec prop-map
  (/ number-test-runs 4)
  (for-all [m (gen/not-empty (gen/map my-any my-any))]
           (let [[k v] (first m)]
             (and ((e/like {k v}) m)
                  (not ((e/like {k v}) (dissoc m k)))))))

(defspec like-one-test
  number-test-runs
         (for-all [x my-any
                   y my-any
                   z my-any]
                  (and (true? (e/like? (e/like-one x y z) x))
                       (true? (e/like? (e/like-one x y z) y))
                       (true? (e/like? (e/like-one x y z) z)))))

(defspec like-all-test
  number-test-runs
  (for-all [x my-any
            y my-any
            z my-any]
    (and (false? (e/like? (e/like-all x y z) x))
         (false? (e/like? (e/like-all x y z) y))
         (false? (e/like? (e/like-all x y z) z)))))

(deftest readme-examples
  (testing "Examples from the Readme"
    (is (true? (e/like? even? 4)))
    (is (false? (e/like? even? 5)))
    (is (true? (e/like? "AB" "elvABuunre")))
    (is (false? (e/like? "AB" "CANRIBAean")))
    (is (true? (e/like? #"[a-z]" "a")))
    (is (false? (e/like? #"[a-z]" "az")))
    (is (true? (e/like? :a :a)))
    (is (false? (e/like? :a/a :a)))
    (is (true? (e/like? :a :a/a)))
    (is (true? (e/like? :a/a :a/a)))
    (is (false? (e/like? :b/a :a/a)))
    (is (true? (e/like? {:a "A"} {:a "BAB" :b 123})))
    (is (true? (e/like? {:a {:b "A"}} {:a {:b "LAL" :c 1}})))
    (is (false? (e/like? {:a 1} {:a "A"})))
    (is (true? (e/like? e/ANY 1)))
    (is (true? (e/like? e/ANY "Foo")))
    (is (true? (e/like? [1 2 3] '(1 2 3))))
    (is (true? (e/like? ["1" {:a 1} ["A"]] ["A1A" {:a 1 :b 2 :c 3} ["XA"]])))
    (is (false? (e/like? [1 2] [1 2 3])))
    (is (true? ((e/like-one "A" "B" "C") "C")))
    (is (false? ((e/like-one "A" "B" "C") "E")))
    (is (true? ((e/like-all "A" "B" "C") "ABC")))
    (is (false? ((e/like-all "A" "B" "C") "ABD")))
    (let [f (e/like {:Type (e/like-one :simple :complex)})]
      (is (true? (f {:Type :simple})))
      (is (true? (f {:Type :complex})))
      (is (false? (f {:Type :other}))))
    (is (true? ((apply e/like-all ["A" "B" "C"]) "ABC")))
    (is (false? ((apply e/like-one ["A" "B" "C"]) "D")))))

#?(:cljs (run-tests))