(ns ebenbild.core-test
  (:require [clojure.test :refer :all]
            [ebenbild.core :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

;Any is not good at creating simple stuff, so we add them
(def real-any
  (gen/one-of [gen/any gen/int gen/string gen/keyword gen/double]))

(defspec prop-identity
         200
         (prop/for-all [x real-any]
                       ((like x) x)))

(defspec prop-should-not-fail
         200
         (prop/for-all [x real-any
                        y real-any]
                       (or ((like x) y) true)))

(defspec prop-substring
         200
         (prop/for-all [s1 gen/string
                        s2 gen/string
                        s3 gen/string]
           (and (like? s1 (str s1 s2 s3))
                (like? s2 (str s1 s2 s3))
                (like? s3 (str s1 s2 s3)))))


(defspec prop-map
         100
         (prop/for-all [m (gen/not-empty (gen/map real-any real-any))]
                       (let [[k v] (first m)]
                         (and ((like {k v}) m)
                              (not ((like {k v}) (dissoc m k)))))))

(deftest readme-examples
  (testing "Examples from the Readme"
    (is (like? even? 4))
    (is (not (like? even? 5)))
    (is (like? "AB" "elvABuunre"))
    (is (not (like? "AB" "CANRIBAean")))
    (is (like? #"[a-z]" "a"))
    (is (not (like? #"[a-z]" "az")))
    (is (like? {:a "A"} {:a "BAB" :b 123}))
    (is (like? {:a {:b "A"}} {:a {:b "LAL" :c 1}}))
    (is (not (like? {:a 1} {:a "A"})))
    (is (like? ANY 1))
    (is (like? ANY "Foo"))))



