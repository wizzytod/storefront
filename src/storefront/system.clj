(ns storefront.system
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]
            [tocsin.core :as tocsin]
            [storefront.config :as config]
            [ring.component.jetty :refer [jetty-server]]
            [storefront.handler :refer [create-handler]]))

(defrecord AppHandler [logger exception-handler storeback environment prerender-token]
  component/Lifecycle
  (start [c]
    (let [params (merge {:storeback-config storeback
                         :environment environment
                         :prerender-token prerender-token}
                        (select-keys c [:logger :exception-handler]))]
      (assoc c :handler (create-handler params))))
  (stop [c] c))

(defn logger [logger-config]
  (fn [level str]
    (timbre/log logger-config level str)))

(defn exception-handler [bugsnag-token environment]
  (fn [e]
    (tocsin/notify e {:api-key bugsnag-token
                      :environment environment})))

(defn system-map [config]
  (component/system-map
   :logger (logger (config :logging))
   :app-handler (map->AppHandler (select-keys config [:storeback :environment :prerender-token]))
   :embedded-server (jetty-server (config :server-opts))
   :exception-handler (exception-handler (config :bugsnag-token) (config :environment))))

(defn dependency-map []
  {:app-handler [:logger :exception-handler]
   :embedded-server {:app :app-handler}})

(defn create-system
  ([] (create-system {}))
  ([config-overrides]
   (let [config (config/system-config config-overrides)]
     (component/system-using
      (system-map config)
      (dependency-map)))))
