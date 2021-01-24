(ns anki.db.decks
  (:require [datomic.api :as d]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [anki.db.core :refer [conn]]))

;; Deck spec
(s/def :deck/id uuid?)
(s/def :deck/title (s/and string? #(seq %)))
(s/def :deck/tags (s/coll-of string?
                             :kind set?
                             :min-count 0))
(s/def ::deck
  (s/keys
   :req [:deck/title]
   :opt [:deck/id]))

(gen/generate (s/gen ::deck))

(defn browse [db user-id]
  (d/q '[:find [(pull ?deck [*]) ...]
         :in $ ?uid
         :where
         [?user :user/id ?uid]
         [?deck :deck/author ?user]]
       db user-id))

;; Passing a . after find clause returns a single item
(defn fetch-by-id [db user-id deck-id]
  (d/q '[:find (pull ?deck [*]) .
         in $ ?uid ?did
         :where
         [?user :user/id ?uid]
         [?deck :deck/id ?did]
         [?deck :deck/author ?user]]
       db user-id deck-id))

(defn create! [conn user-id deck-params]
  (if (s/valid? ::deck deck-params)
    (let [deck-id (d/squuid)
          tx-data (merge deck-params {:deck/author [:user/id user-id]
                                      :deck/id deck-id})]
      (d/transact conn [tx-data])
      deck-id)
    (throw (ex-info "Deck is invalid"
                    {:anki/error-id :validation
                     :error "Invalid deck input values"}))))

(defn edit! [conn user-id deck-id deck-params]
  (if (fetch-by-id (d/db conn) user-id deck-id)
    (let [tx-data (merge deck-params {:deck/id deck-id})
          db-after (:db-after @(d/transact conn [tx-data]))]
      (fetch-by-id db-after user-id deck-id))
    (throw (ex-info "Update deck failed"
                    {:anki/error-id :server-error
                     :error "Unable to update deck"}))))

(defn delete! [conn user-id deck-id])
