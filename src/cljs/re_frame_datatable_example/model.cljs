(ns re-frame-datatable-example.model
  (:require [cljs.spec.alpha :as s]
            [cljs.spec.gen.alpha :as gen]
            [clojure.test.check.generators]))


(def first-names #{"James" "Mary" "John" "Patricia" "Robert" "Jennifer" "Michael" "Elizabeth" "William" "Linda"
                   "David" "Barbara" "Richard" "Susan" "Joseph" "Jessica" "Thomas" "Margaret" "Charles" "Sarah"
                   "Christopher" "Karen" "Daniel" "Nancy" "Matthew" "Betty" "Anthony" "Dorothy" "Donald" "Lisa"
                   "Mark" "Sandra" "Paul" "Ashley" "Steven" "Kimberly" "George" "Donna" "Kenneth" "Carol"})


(def last-names #{"Smith" "Johnson" "Williams" "Jones" "Brown" "Davis" "Miller" "Wilson" "Moore" "Taylor"
                  "Anderson" "Thomas" "Jackson" "White" "Harris" "Martin" "Thompson" "Garcia" "Martinez" "Robinson"
                  "Clark" "Rodriguez" "Lewis" "Lee" "Walker" "Hall" "Allen" "Young" "Hernandez" "King" "Wright"
                  "Lopez" "Hill" "Scott" "Green" "Adams" "Baker" "Gonzalez" "Nelson" "Carter"})

(def words (->> "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                (re-seq #"[A-Za-z]+")
                (map clojure.string/lower-case)
                (set)))

(def labels [{:title "Inbox" :key :inbox}
             {:title "Archived" :key :archived}
             {:title "Spam" :key :spam}
             {:title "Trash" :key :trash}])

(def default-label-keys (set (map :key labels)))

(s/def ::label default-label-keys)


(def domains #{"gmail.com" "yahoo.com" "hotmail.com" "outlook.com" "inbox.com" "mail.com"})


(s/def ::first-name first-names)
(s/def ::last-name last-names)
(s/def ::domain domains)


(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(s/def ::email-address
  (s/with-gen
    (s/and string? #(re-matches email-regex %))
    #(gen/fmap (fn [[fname lname domain]]
                 (str (clojure.string/lower-case fname)
                      "_"
                      (clojure.string/lower-case lname)
                      "@"
                      domain))
               (gen/tuple (s/gen ::first-name) (s/gen ::last-name) (s/gen ::domain)))))


(s/def ::word words)


(s/def ::subject
  (s/with-gen
    string?
    #(gen/fmap (fn [words]
                 (->> words
                      (clojure.string/join \space)
                      (clojure.string/capitalize)))
               (s/gen (s/coll-of ::word :min-count 1 :max-count 4)))))


(s/def ::body
  (s/with-gen
    string?
    #(gen/fmap (fn [words]
                 (->> words
                      (clojure.string/join \space)
                      (clojure.string/capitalize)))
               (s/gen (s/coll-of ::word :min-count 1 :max-count 10)))))


(s/def ::from ::email-address)
(s/def ::to (s/coll-of ::email-address :min-count 1 :max-count 5 :distinct true))
(s/def ::date
  (s/with-gen
    #(instance? js/Date %)
    #(gen/fmap (fn [epoch]
                 (js/Date. (* (+ epoch (rand-int 1000000)) 1000)))
               (s/gen (s/int-in 1454104654 1482958036)))))


(s/def ::email (s/keys :req-un [::to ::from ::subject ::body ::date]))
(s/def ::emails (s/coll-of ::email :min-count 1 :max-count 9))

(s/def ::starred?
  (s/with-gen
    boolean?
    #(s/gen false?)))

(s/def ::id
  (s/with-gen
    #(instance? UUID %)
    #(gen/fmap (fn [[v]] v)
               (gen/tuple (gen/uuid)))))


(s/def ::thread (s/keys :req-un [::emails ::label ::starred? ::id]))


(def sample-inbox (->> (gen/sample (s/gen ::thread) 100)
                       (map (fn [o] {(:id o) o}))
                       (apply merge)))
