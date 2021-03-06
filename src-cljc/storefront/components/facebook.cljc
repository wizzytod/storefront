(ns storefront.components.facebook
  (:require [storefront.platform.component-utils :as utils]
            [storefront.events :as events]
            [storefront.components.ui :as ui]))

(defn narrow-sign-in-button [loaded?]
  (when loaded?
    (ui/button-large-facebook-blue
     {:on-click  (utils/send-event-callback events/control-facebook-sign-in)
      :data-test "facebook-button"}
     [:div.mx-auto
      [:div.flex.items-center.justify-center
       (ui/ucare-img {:width  14
                      :height 14} "975698f3-3eda-411c-83ad-6a2750e0e59d")
       [:span.ml1 "Sign in"]]])))

(defn- wide-button [loaded? click-event]
  (if loaded?
    (ui/button-large-facebook-blue
     {:on-click  (utils/send-event-callback click-event)
      :data-test "facebook-button"}
     [:div.flex.items-center.justify-center
      [:span "Sign in with Facebook"]
      (ui/ucare-img {:width  16
                     :height 16
                     :class  "ml2"} "975698f3-3eda-411c-83ad-6a2750e0e59d")])
    [:div {:style {:height "2.6666em"}}]))

(defn sign-in-button [loaded?]
  (wide-button loaded? events/control-facebook-sign-in))

(defn reset-button [loaded?]
  (wide-button loaded? events/control-facebook-reset))
