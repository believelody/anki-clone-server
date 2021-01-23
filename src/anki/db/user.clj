(ns anki.db.user
  (:require [datomic.api :as d]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [anki.db.core :refer [conn]]
            [clojure.test.check.generators :as gen]))

(defn validate-email [email]
  (let [email-regex #"^([a-zA-Z0-9_\-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)$"]
    (re-matches email-regex email)))

(validate-email "joe@gmail.com")

(s/def :user/email (s/with-gen
                     (s/and string? validate-email)
                     #(s/gen #{"t1@t.com" "t2@t.com"})))
(s/def :user/password (s/with-gen
                        (s/and string? #(> (count %) 3))
                        #(s/gen #{"azerty" "1234" "abc"})))
(s/def :user/username (s/with-gen
                        string?
                        #(s/gen #{"t1" "t2" "t3"})))
(s/def :user/token (s/with-gen
                     string?
                     #(s/gen #{"token1" "token2" "token3"})))
(s/def :user/id uuid?)

(s/def ::user
  (s/keys :req [:user/email :user/password]
          :opt [:user/id :user/id :user/username]))

(gen/generate (s/gen :user/email))
(gen/generate (s/gen :user/password))
(gen/generate (s/gen :user/username))
(gen/generate (s/gen :user/token))
(gen/generate (s/gen ::user))

;; (s/valid? :user/email "madamada")
;; (s/explain :user/email "madamada")
;; (s/conform :user/email "madamada")

(def sample-user {:user/email "john@doe.com"
                  :user/password "1234"})

(s/valid? ::user sample-user)

(defn create! [conn user-params]
  (if (s/valid? ::user user-params)
    (let [user-id (d/squuid)
          tx-data (merge user-params {:user/id user-id})]
      (d/transact conn [tx-data])
      user-id)
    (throw (ex-info "User is invalid"
                    {:anki/error-id :validation
                     :error "Invalid email or password provided"}))))

;; Passing a . after find clause returns a single item
(defn fetch-by-id [db user-id]
  (d/q '[:find (pull ?uid [*]) .
         :in $ ?user-id
         :where [?uid :user/id ?user-id]]
       (d/db conn)
       user-id))

(defn edit! [conn user-id user-params]
  (if (fetch-by-id (d/db conn) user-id)
    (let [tx-data (merge user-params {:user/id user-id})
          db-after (:db-after @(d/transact conn [tx-data]))]
      (fetch-by-id db-after user-id))
    (throw (ex-info "Unable to update user"
                    {:anki/error-id :server-error
                     :error "Unable to edit user"}))))

(defn delete! [conn user-id]
  (when-let [user (fetch-by-id (d/db conn) user-id)]
    (d/transact conn [[:db/retractEntity [:user/id user-id]]])
    user))

(comment
  (create! conn sample-user)
  (d/db conn)
  (fetch-by-id (d/db conn) #uuid "60093baf-bc5a-4fbd-a441-2ca97c28b24e"))
