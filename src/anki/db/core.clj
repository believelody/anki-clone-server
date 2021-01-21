(ns anki.db.core
  (:require
   [datomic.api :as d]
   [mount.core :as mount :refer [defstate]]
   [anki.db.config :refer [env]]
   [anki.db.schema :refer [schema]]))

(:databse-uri env)

(def database-uri "datomic:free://localhost:4334/anki")

(defn create-conn [db-uri]
  (d/create-database db-uri)
  (let [conn (d/connect db-uri)]
    conn))

(defstate conn
  :start (create-conn database-uri)
  :stop (.release conn))
