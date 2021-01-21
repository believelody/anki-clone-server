(ns anki.db.user
  (:require [datomic.api :as d]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]
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
(s/def :user/id string?)

(gen/generate (s/gen :user/email))
(gen/generate (s/gen :user/password))
(gen/generate (s/gen :user/username))
(gen/generate (s/gen :user/token))
(gen/generate (s/gen ::user))

;; (s/valid? :user/email "madamada")
;; (s/explain :user/email "madamada")
;; (s/conform :user/email "madamada")

(s/def ::user
  (s/keys :req [:user/email :user/password]
          :opt [:user/id :user/id :user/username]))

(def sample-user {:user/email "john@doe.com"
                  :user/password "1234"})

(s/valid? ::user sample-user)

(defn create! [conn user-params]
  (if (s/valid? ::user user-params)
    (let [user-id (d/squuid)
          tx-data (merge {:user/id user-id} user-params)]
      (d/transact conn [tx-data])
      user-id)
    (throw (ex-info "User is invalid"
                    {:anki/error-id :validation
                     :error "Invalid email or password provided"}))))
