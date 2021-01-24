(ns anki.db.cards
  (:require [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [anki.db.core :refer [conn]]
            [datomic.api :as d]))

(spec/def :card/id uuid?)
(spec/def :card/front string?)
(spec/def :card/back string?)
(spec/def :card/progress (spec/and int? #(> % 0) #(<= % 100)))
(spec/def :card/next-study-date inst?)
(spec/def ::card
  (spec/keys :req [:card/front :card/back]
             :opt [:card/id :card/progress :card/next-study-date]))

(gen/generate (spec/gen ::card))

(defn browse [db deck-id]
  (d/q '[:find [(pull ?cards [*]) ...]
         :in $ ?deck-id
         :where
         [?deck :deck/id ?deck-id]
         [?cards :card/deck ?deck]]
       db deck-id))

(defn fetch-by-id [db deck-id card-id]
  (d/q '[:find [(pull ?card [*]) .]
         :in $ ?deck-id ?card-id
         :where
         [?deck :deck/id ?deck-id]
         [?card :card/id ?card-id]
         [?card :card/deck ?deck]]
       db deck-id card-id))

(defn create! [conn deck-id card-params]
  (if (spec/valid? ::card card-params)
    (let [card-id (d/squuid)
          tx-data (-> card-params
                      (assoc :card/deck [:deck/id deck-id])
                      (assoc :card/id card-id))]
      (d/transact conn [tx-data])
      card-id)
    (throw (ex-info "Card is invalid"
                    {:anki/error-id :validation
                     :error "Invalid card input values"}))))

(defn edit! [conn deck-id card-id card-params]
  (if (fetch-by-id (d/db conn) deck-id card-id)
    (let [tx-data (merge card-params {:card/id card-id})
          db-after (:db-after @(d/transact conn [tx-data]))]
      (fetch-by-id db-after deck-id card-id))
    (throw (ex-info "Update card failed"
                    {:anki/error-id :server-error
                     :error "Unable to update card"}))))

(defn delete! [conn deck-id card-id]
  (when-let [deleted-card (fetch-by-id (d/db conn) deck-id card-id)]
    (d/transact conn [[:db/retractEntity [:card/id card-id]]])
    deleted-card))
