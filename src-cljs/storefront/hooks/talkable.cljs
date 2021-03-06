(ns storefront.hooks.talkable
  (:require [storefront.browser.tags :as tags]
            [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.accessors.orders :refer [add-rounded-floats]]
            [storefront.platform.messages :as m]
            [storefront.config :as config]))

(defn- discounted-subtotal [order]
  (add-rounded-floats (:line-items-total order) (:promotion-discount order)))

(defn completed-order [order]
  {:purchase {:order_number (:number order)
              :subtotal (.toString (discounted-subtotal order))
              :coupon_code (-> order :promotion-codes first)}
   :customer {:email (-> order :user :email)}})

(defn show-pending-offer [data]
  (when-let [order (get-in data keypaths/pending-talkable-order)]
    (js/showPP (clj->js order))
    (m/handle-message events/talkable-offer-shown)))

(defn show-referrals [data & campaign-tags]
  (js/showSA (clj->js (merge
                       {:affiliate_member
                        {:email (get-in data keypaths/user-email)}}
                       (when (seq campaign-tags)
                         {:campaign_tags campaign-tags})))))
