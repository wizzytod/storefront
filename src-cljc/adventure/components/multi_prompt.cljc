(ns adventure.components.multi-prompt
  (:require #?@(:cljs [[om.core :as om]])
            [storefront.assets :as assets]
            [storefront.component :as component]
            [storefront.components.svg :as svg]
            [storefront.components.ui :as ui]
            [storefront.effects :as effects]
            [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.platform.component-utils :as utils]))

(defn component
  [{:keys [header header-image data-test buttons]} _ _]
  (component/create
   [:div.bg-aqua.white.center.bold.flex-auto.self-stretch
    [:div.flex.items-center
     {:style {:height           "246px"
              :background-size  "cover"
              :background-image (str "url('"header-image "')")}}
     [:div.col-12.p5
      header]]
    [:div.p5 {:data-test data-test}
     (for [button buttons]
       [:div (ui/aqua-button {:data-test (str data-test "-" (:value button))} (:text button))])]]))
