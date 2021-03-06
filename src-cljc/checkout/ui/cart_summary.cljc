(ns checkout.ui.cart-summary
  (:require [storefront.component :as component :refer [defcomponent]]
            [storefront.components.ui :as ui]
            [checkout.ui.molecules :as checkout.M]
            [storefront.keypaths :as keypaths]
            [storefront.platform.component-utils :as utils]
            [ui.molecules :as ui.M]))

(defcomponent organism
  [{:cart-summary/keys [id lines]
    :as query
    :keys [promo-field-data]} owner _]
  [:div {:data-test id}
   [:div.py1.bg-cool-gray.px4
    [:table.col-12
     [:tbody
      (for [line lines]
        ^:inline (checkout.M/cart-summary-line-molecule line))]]

    (when-let [{:keys [text-input-attrs button-attrs]} promo-field-data]
      [:div.my2
       ^:inline (ui.M/field-reveal-molecule promo-field-data)
       (when (and text-input-attrs button-attrs)
         ^:inline (ui/input-group text-input-attrs button-attrs))])

    ^:inline (checkout.M/freeinstall-informational query)]

   [:div.pt2.px4
    ^:inline (checkout.M/cart-summary-total-line query)
    ^:inline (checkout.M/cart-summary-total-incentive query)]])
