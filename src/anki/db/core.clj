(ns anki.db.core
  (:require [datomic.api :as d]
            [anki.db.schema :refer [schema]]))


(def database-uri "datomic:free://localhost:4334/anki")

(defn create-conn [db-uri]
  (d/create-database db-uri)
  (let [conn (d/connect db-uri)]
    conn))

(def conn (create-conn database-uri))

(def tx @(d/transact conn schema))

(comment
  tx)
