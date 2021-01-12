(ns anki.db.with-db
  (:require  [anki.db.core :as sut]
             [anki.db.schema :refer [schema]]
             [datomic.api :as d]))

(def ^:dynamic *conn* nil)

(defn fresh-db []
  (let [db-uri (str "datomic:mem://" (gensym))
        conn (sut/create-conn db-uri)]
    (d/transact conn schema)
    conn))

;;;

(defn with-db [f]
  (binding [*conn* (fresh-db)]
    (f)))
