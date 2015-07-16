(ns twitter-grepper.core
  (:gen-class)
  (:use
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
   [twitter.api.restful])
  (:import
   (twitter.callbacks.protocols SyncSingleCallback)))

(require 'clojure.edn)

(defn load-config [filename]
  (clojure.edn/read-string (slurp filename)))

(defn get-tweets [username creds]
  "Get a list of all tweets from my start page"
  []
  (get (statuses-home-timeline :oauth-creds creds :params {:source-screen-name username}) :body))

(defn print-tweet
  "Print a single tweet"
  [tweet]
  (print "[" (get tweet :created_at) "] "
       	   	   	  (get (get tweet :user) :name) ": "
                          (get tweet :text) "\n"))

(defn filter-tweets
  "Filter a list of tweet texts by a given word"
  [tweets search]
  (filter (fn [tweet] (re-find (re-pattern (str "(?i)" search)) (get tweet :text))) tweets))

(defn dump-tweets
  "Dump a list of tweets with the given dump-fn"
  [tweets dump-fn]
  (map (fn [tweet] (dump-fn tweet)) tweets))

(defn -main
  "Print a filtered list of all tweets from my startpage"
  [& args]
  (def config (load-config (or (first args) "conf/twitter_grepper.conf")))

  (def creds (make-oauth-creds (:app-consumer-key config)
                               (:app-consumer-secret config)
                               (:user-access-token config)
                               (:user-access-token-secret config)))

  (def unfiltered-tweets (get-tweets (:username config) creds))

  (for [keyword (:keywords config)] 
    (dump-tweets 
     (filter-tweets unfiltered-tweets keyword) 
     print-tweet))
)
