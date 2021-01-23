(ns anki.db.decks-test
  (:require [anki.db.decks :as sut]
            [anki.db.user :as u]
            [anki.db.with-db :refer [with-db *conn*]]
            [datomic.api :as d]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [clojure.test :refer [is deftest testing use-fixtures]]))

(use-fixtures :each with-db)

(deftest decks
  (testing "browse, returns empty vector, user has not created any deck"
    (let [user-params (gen/generate (s/gen ::u/user))
          uid (u/create! *conn* user-params)
          decks (sut/browse (d/db *conn*) uid)]
      (is (= true (vector? decks)))
      (is (= true (empty? decks)))))
  (testing "browse - returns vector of decks, if available"
    (let [user-params (gen/generate (s/gen ::u/user))
          uid (u/create! *conn* user-params)
          new-deck (merge (gen/generate (s/gen ::sut/deck))
                          :deck/author [:user/id uid])]
      @(d/transact *conn* [new-deck])
      (let [decks [sut/browse (d/db *conn*) uid]]
        (is (= true (vector? decks)))
        (is (= false (empty? decks))))))
  (testing "fetch-by-id, returns a single deck by deck ID, belonging to a user"
    (let [deck-id (d/squuid)
          user-params (gen/generate (s/gen ::u/user))
          user-id (u/create! *conn* user-params)
          new-deck (merge (gen/generate (s/gen ::sut/deck))
                          {:deck/id deck-id
                           :deck/author [:user/id user-id]})]
      @(d/transact *conn* [new-deck])
      (let [deck (sut/fetch-by-id (d/db *conn*) user-id deck-id)]
        (is (= true (map? deck)))
        (is (= false (empty? deck))))))
  (testing "fetch-by-id, returns nil if deck not found"
    (let [deck-id (d/squuid)
          user-params (gen/generate (s/gen ::u/user))
          user-id (u/create! *conn* user-params)
          deck (sut/fetch-by-id (d/db *conn*) user-id deck-id)]
      (is (= false (map? deck)))
      (is (= true (nil? deck))))))
