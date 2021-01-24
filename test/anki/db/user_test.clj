(ns anki.db.user-test
  (:require [anki.db.user :as sut]
            [datomic.api :as d]
            [clojure.test :refer [is deftest testing use-fixtures]]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [anki.db.with-db :refer [with-db *conn*]]))

(use-fixtures :each with-db)

(deftest user
  (testing "create!"
    (let [user-params {:user/email "john@doe.com"
                       :user/password "password"}
          uid (sut/create! *conn* user-params)]
      (is (not (nil? uid)))
      (is (= true (uuid? uid)))))
  (testing "fetch-by-id"
    (let [uid (sut/create! *conn* (gen/generate (s/gen ::sut/user)))
          user (sut/fetch-by-id (d/db *conn*) uid)]
      (is (= true (s/valid? ::sut/user user)))))
  (testing "edit!"
    (let [uid (sut/create! *conn* (gen/generate (s/gen ::sut/user)))
          user (sut/edit! *conn* uid {:user/username "lowa"})]
      (is (= true (s/valid? ::sut/user user)))
      (is (= "lowa" (:user/username user))))))
