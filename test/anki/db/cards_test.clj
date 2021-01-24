(ns anki.db.cards-test
  (:require [anki.db.cards :as sut]
            [anki.db.decks :as d]
            [anki.db.with-db :refer [with-db *conn*]]
            [datomic.api :as dtm]
            [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [clojure.test :refer [is deftest testing use-fixtures]]))

(use-fixtures :each with-db)

(deftest cards
  (let [user-id (:user/id (dtm/entity (dtm/db *conn*) [:user/email "test@test.com"]))]
    (testing "browse, returns ampty vector if user has not created any card for the deck"
      (let [new-deck (merge (gen/generate (spec/gen ::d/deck))
                            {:deck/author [:user/id user-id]})
            deck-id (d/create! *conn* user-id new-deck)
            cards (sut/browse (dtm/db *conn*) deck-id)]
        (is (empty? cards))
        (is (vector? cards))))
    (testing "browse, returns a list of cards for a single deck"
      (let [new-deck (merge (gen/generate (spec/gen ::d/deck)))
            deck-id (d/create! *conn* user-id new-deck)
            new-card {:card/id (dtm/squuid)
                       :card/deck [:deck/id deck-id]
                       :card/front "What is clojure ?"
                       :card/back "A programming language"}]
        @(dtm/transact *conn* [new-card])
        (let [cards (sut/browse (dtm/db *conn*) deck-id)
              card (first cards)]
          (is (seq cards))
          (is (spec/valid? ::sut/card))
          (is (vector? cards)))))))
