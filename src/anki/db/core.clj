(ns anki.db.core
  (:require
   [datomic.api :as d]
   [config.core :refer [env reload-env]]
   [anki.db.schema :refer [schema]]))

(:databse-uri env)

(def database-uri "datomic:free://localhost:4334/anki")

(defn create-conn [db-uri]
  (d/create-database db-uri)
  (let [conn (d/connect db-uri)]
    conn))

(def conn (create-conn database-uri))

(def tx @(d/transact conn schema))

(comment
  (reload-env)
  (System/getenv "database-uri"))
