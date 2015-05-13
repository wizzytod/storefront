(ns storefront.core
  (:require-macros [cljs.core.async.macros :refer [go-loop alt!]])
  (:require [storefront.state :as state]
            [storefront.events :as events]
            [storefront.components.top-level :refer [top-level-component]]
            [storefront.effects :refer [perform-effects]]
            [storefront.transitions :refer [transition-state]]
            [storefront.routes :as routes]
            [cljs.core.async :refer [<! chan close! put!]]
            [om.core :as om]))

(enable-console-print!)

(defn transition [app-state [event args]]
  (reduce #(transition-state %2 event args %1) app-state (reductions conj [] event)))

(defn effects [app-state [event args]]
  (doseq [event-fragment (rest (reductions conj [] event))]
    (perform-effects event-fragment event args app-state)))

(defn start-event-loop [app-state]
  (let [event-ch (get-in @app-state state/event-ch-path)]
    (go-loop []
      (when-let [event-and-args (<! event-ch)]
        (do
          (swap! app-state transition event-and-args)
          (effects @app-state event-and-args)
          (recur))))))

(defn main [app-state]
  (routes/install-routes app-state)
  (om/root
   top-level-component
   app-state
   {:target (.getElementById js/document "content")})

  (start-event-loop app-state)
  (routes/set-current-page @app-state))

(defonce app-state (atom (state/initial-state)))

(defn debug-app-state []
  (clj->js @app-state))

(defn on-jsload []
  (close! (get-in @app-state state/event-ch-path))
  (swap! app-state assoc-in state/event-ch-path (chan))
  (main app-state))

(main app-state)
