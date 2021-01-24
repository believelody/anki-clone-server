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
          (is (vector? cards)))))
    (testing "fetch-by-id, return a card by ID from a deck"
      (let [new-deck (merge (gen/generate (spec/gen ::d/deck)))
            deck-id (d/create! *conn* user-id new-deck)
            new-card-id (dtm/squuid)
            new-card {:card/id new-card-id
                      :card/deck [:deck/id deck-id]
                      :card/front "What is clojure ?"
                      :card/back "A programming language"}]
        @(dtm/transact *conn* [new-card])
        (let [card (sut/fetch-by-id (dtm/db *conn*) deck-id new-card-id)]
          (is (spec/valid? ::sut/card card)))))
    (testing "fetch-by-id, return nil if not found"
      (let [new-deck (merge (gen/generate (spec/gen ::d/deck)))
            deck-id (d/create! *conn* user-id new-deck)
            new-card-id (dtm/squuid)
            new-card {:card/id new-card-id
                      :card/deck [:deck/id deck-id]
                      :card/front "What is clojure ?"
                      :card/back "A programming language"}]
        @(dtm/transact *conn* [new-card])
        (let [card (sut/fetch-by-id (dtm/db *conn*) 1 2)]
          (is (nil? card)))))
  (testing "create!, create a new deck"
    (let [new-deck (merge (gen/generate (spec/gen ::sut/deck)))
          deck-id (sut/create! *conn* user-id new-deck)
          new-card {:card/id (dtm/squuid)
                    :card/deck [:deck/id deck-id]
                    :card/front "What is clojure ?"
                    :card/back "A programming language"}
          card-id (sut/create! *conn* deck-id new-card)]
      (is (uuid? card-id))))
  (testing "edit!, edits an existing deck and returns the updated card"
    (let [new-deck (gen/generate (spec/gen ::d/deck))
          deck-id (sut/create! *conn* user-id new-deck)
          new-card {:card/deck [:decl/id deck-id]
                       :card/front "What is Clojure ?"
                       :card/back "A programming language"}
          card-id (sut/create! *conn* deck-id new-card)
          edited-card (sut/edit! *conn* deck-id card-id {:card/back "A functional programming language"})]
      (is (spec/valid? ::sut/card edited-card))))
  (testing "delete!, delete an existing deck"
    (let [deck-id (d/create! *conn* user-id (gen/generate (spec/gen ::d/deck)))
          card-id (sut/create! *conn* deck-id (gen/generate (spec/gen ::sut/card)))
          deleted-card (sut/delete! *conn* deck-id card-id)]
      (is (= true (spec/valid? ::sut/card deleted-card)))
      (is (= nil (sut/fetch-by-id (dtm/db *conn*) deck-id card-id)))))))
