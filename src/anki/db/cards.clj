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
