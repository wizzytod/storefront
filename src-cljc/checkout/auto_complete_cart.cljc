(ns checkout.auto-complete-cart
  (:require [storefront.accessors.images :as images]
            [storefront.components.money-formatters :refer [as-money-without-cents]]
            [storefront.components.ui :as ui]
            [storefront.events :as events]
            [storefront.platform.component-utils :as utils]))

(defn ^:private display-line-item
  "Storeback now returns shared-cart line-items as a v2 Sku + item/quantity, aka
  'line-item-skuer' This component is also used to display line items that are
  coming off of a waiter order which is a 'variant' with a :quantity
  Until waiter is updated to return 'line-item-skuers', this function must handle
  the two different types of input"
  [line-item {:keys [catalog/sku-id] :as sku} thumbnail quantity-line]
  (let [legacy-variant-id (or (:legacy/variant-id line-item) (:id line-item))
        price             (or (:sku/price line-item)         (:unit-price line-item))]
    [:div.clearfix.border-bottom.border-gray.py3 {:key legacy-variant-id}
     [:a.left.mr1.z0
      [:div.right.z1.bg-teal.border.border-white.circle.stacking-context
       {:style {:margin-left "-14px"
                :margin-top  "-14px"
                :width      "40px"
                :height     "40px"}}
       [:div.bg-lighten-4.circle.align-middle.flex.items-center.justify-center.medium
        {:style {:width  "44px"
                 :height "44px"}} "12\""]]
      [:img.block.border.border-light-gray
       (assoc thumbnail :style {:width  "7.33em"
                                :height "7.33em"})]]
     [:div.overflow-hidden
      [:div.ml1
       [:a.medium.titleize.h5
        {:data-test (str "line-item-title-" sku-id)}
        (:product-title line-item)]
       [:div.h6.mt1.line-height-1
        [:div.pyp2
         {:data-test (str "line-item-color-" sku-id)}
         (:color-name line-item)]
        [:div.pyp2
         {:data-test (str "line-item-price-ea-" sku-id)}
         "Price Each: " (as-money-without-cents price)]
        quantity-line]]]]))

(defn adjustable-quantity-line
  [line-item {:keys [catalog/sku-id]} removing? updating?]
  [:.mt1.flex.items-center.justify-between
   (if removing?
     [:.h3 {:style {:width "1.2em"}} ui/spinner]
     [:a.gray.medium
      (merge {:data-test (str "line-item-remove-" sku-id)}
             (utils/fake-href events/control-cart-remove (:id line-item))) "Remove"])
   [:.h3
    {:data-test (str "line-item-quantity-" sku-id)}
    (ui/counter {:spinning? updating?
                 :data-test sku-id}
                (:quantity line-item)
                (utils/send-event-callback events/control-cart-line-item-dec {:variant line-item})
                (utils/send-event-callback events/control-cart-line-item-inc {:variant line-item}))]])

(defn display-adjustable-line-items
  [line-items skus update-line-item-requests delete-line-item-requests]
  (for [{sku-id :sku variant-id :id :as line-item} line-items
        :let                                       [sku (get skus sku-id)]]
    (display-line-item
     line-item
     sku
     (merge
      (images/cart-image sku)
      {:data-test (str "line-item-img-" (:catalog/sku-id sku))})
     (adjustable-quantity-line line-item
                               sku
                               (get delete-line-item-requests variant-id)
                               (get update-line-item-requests sku-id)))))
