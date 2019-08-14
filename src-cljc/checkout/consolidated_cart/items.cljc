(ns checkout.consolidated-cart.items
  (:require [adventure.keypaths :as adventure-keypaths]
            [checkout.accessors.vouchers :as vouchers]
            [storefront.accessors.orders :as orders]
            [storefront.components.ui :as ui]
            [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.platform.component-utils :as utils]
            [storefront.request-keys :as request-keys]))

(defn freeinstall-line-item-query [data]
  (let [order (get-in data keypaths/order)]
    (when (orders/freeinstall-entered? order)
      (let [store-nickname        (get-in data keypaths/store-nickname)
            highest-value-service (some-> order
                                          orders/product-items
                                          vouchers/product-items->highest-value-service)

            {:as   campaign
             :keys [:voucherify/campaign-name
                    :service/diva-advertised-type]} (->> (get-in data keypaths/environment)
                                                         vouchers/campaign-configuration
                                                         (filter #(= (:service/type %) highest-value-service))
                                                         first)
            service-price                           (some-> data
                                                            (get-in adventure-keypaths/adventure-servicing-stylist-service-menu)
                                                            (get diva-advertised-type))]
        (when service-price
          {:removing?          (utils/requesting? data request-keys/remove-promotion-code)
           :id                 "freeinstall"
           :title              campaign-name
           :detail             (str "w/ " store-nickname)
           :price              service-price
           :total-savings      (orders/total-savings order service-price)
           :remove-event       [events/control-checkout-remove-promotion {:code "freeinstall"}]
           :thumbnail-image    "688ebf23-5e54-45ef-a8bb-7d7480317022"
           :thumbnail-image-fn (fn [height-width-int]
                                 (ui/ucare-img {:width height-width-int}
                                               "688ebf23-5e54-45ef-a8bb-7d7480317022"))})))))
