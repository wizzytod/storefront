(ns storefront.components.popup
  (:require [storefront.component :as component]
            [storefront.components.email-capture :as email-capture]
            [storefront.components.free-install :as free-install]
            [storefront.components.install-phone-capture :as install-phone-capture]
            [storefront.components.share-your-cart :as share-your-cart]
            [storefront.components.stylist.referrals :as stylist.referrals]
            [storefront.components.v2-homepage-popup :as v2-homepage-popup]
            [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.platform.component-utils :as utils]))

(def popup-type->popups
  {:free-install         {:query     free-install/query
                          :component free-install/component}
   :v2-homepage          {:query     v2-homepage-popup/query
                          :component v2-homepage-popup/component}
   :email-capture        {:query     email-capture/query
                          :component email-capture/component}
   :share-cart           {:query     share-your-cart/query
                          :component share-your-cart/component}
   :refer-stylist        {:query     stylist.referrals/query-refer
                          :component stylist.referrals/refer-component}
   :refer-stylist-thanks {:query     stylist.referrals/query-thanks
                          :component stylist.referrals/thanks-component}

   :install-phone-capture {:query     install-phone-capture/query
                           :component install-phone-capture/component}})

(defn query [data]
  (let [popup-type (get-in data keypaths/popup)
        query      (or (some-> popup-type popup-type->popups :query)
                       (constantly nil))]
    {:popup-type popup-type
     :popup-data (query data)}))

(defn built-component [{:keys [popup-type popup-data]} _]
  (let [opts {:opts {:close-attrs (utils/fake-href events/control-popup-hide)}}]
    (some-> popup-type popup-type->popups :component (component/build popup-data opts))))
