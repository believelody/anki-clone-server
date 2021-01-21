(ns dev.user
  (:require [mount.core :as mount]
            [anki.core]))

(defn start
  "Mount starts lifecycle of runtime state"
  []
  (mount/start))

(defn stop
  "Mount stops lifecycle of runtime state"
  []
  (mount/stop))

(defn restart-dev []
  (stop)
  (start))

(comment
  (restart-dev))
