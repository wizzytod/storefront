(ns adventure.components.offset-prompt
  (:require #?@(:cljs [[om.core :as om]])
            [storefront.assets :as assets]
            [storefront.component :as component]
            [storefront.components.svg :as svg]
            [adventure.components.header :as header]
            [storefront.components.ui :as ui]
            [storefront.effects :as effects]
            [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.platform.component-utils :as utils]))

(defn component
  [{:keys [prompt mini-prompt header-data background-image background-position button]} _ _]
  (component/create
   [:div.bg-aqua.white.center.flex.flex-auto.flex-column
    (when header-data
      (header/built-component header-data nil))
    [:div.px2.flex.flex-column.items-center.justify-center
     {:style {:height "246px"}}
     [:div.bold prompt]
     [:div mini-prompt]]
    [:div.flex.flex-auto.items-end.p5
     {:style {:background-image    (str "url(" background-image ")")
              :background-position "right"
              :background-size     "315px"
              :background-repeat   "no-repeat"}}
     (ui/aqua-button
      (merge {:data-test (:data-test button)}
             (utils/route-to (:target button)))
                     (:text button))]]))