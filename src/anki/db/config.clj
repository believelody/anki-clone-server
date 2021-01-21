(ns anki.db.config
  (:require [datomic.api :as d]
            [mount.core :refer [defstate]]
            [config.core :as config]))

(defstate env
  :start config/env)
