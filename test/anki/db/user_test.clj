(ns anki.db.user-test
  (:require [anki.db.user :as SUT]
            [datomic.api :as d]
            [clojure.test :refer [is deftest testing use-fixtures]]
            [anki.db.with-db :refer [with-db *conn*]]))

(use-fixtures :each with-db)

(deftest user
  (testing "create!"
    (let [user-params {:user/email "john@doe.com"
                       :user/password "password"}
          uid (SUT/create! *conn* user-params)]
      (is (not (nil? uid)))
      (is (= true (uuid? uid))))))
