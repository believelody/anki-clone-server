(ns anki.db.core-test
  (:require [anki.db.core :as sut]
            [datomic.api :as d]
            [anki.db.with-db :refer [with-db *conn*]]
            [clojure.test :refer [is deftest testing use-fixtures]]))

(use-fixtures :each with-db)

;;; sut => Software Under Test

(deftest conn
  (testing "create-conn"
    (is (not (nil? *conn*)))))
