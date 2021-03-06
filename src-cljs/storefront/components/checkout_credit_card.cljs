(ns storefront.components.checkout-credit-card
  (:require [storefront.accessors.credit-cards :as cc]
            [storefront.components.ui :as ui]
            [storefront.keypaths :as keypaths]
            [storefront.platform.component-utils :as utils]
            [storefront.request-keys :as request-keys]
            [storefront.hooks.stripe :as stripe]
            [storefront.events :as events]
            [storefront.platform.messages :as messages]
            [storefront.component :as component :refer [defcomponent defdynamic-component]]))

(defn saving-card? [data]
  (or (utils/requesting? data request-keys/stripe-create-token)
      (utils/requesting? data request-keys/update-cart-payments)))

(defdynamic-component ^:private new-card-component
  (did-mount [_]
             (messages/handle-message events/stripe-component-mounted
                                      {:card-element (stripe/card-element "#card-element")}))
  (will-unmount [_]
                (messages/handle-message events/stripe-component-will-unmount))
  (render [this]
          (let [{{:keys [focused
                         guest?
                         name
                         save-credit-card?
                         saved-cards]} :credit-card
                 field-errors          :field-errors} (component/get-props this)]
            (component/html
             [:div
              (ui/text-field {:errors    (get field-errors ["cardholder-name"])
                              :data-test "payment-form-name"
                              :keypath   keypaths/checkout-credit-card-name
                              :focused   focused
                              :label     "Cardholder's Name"
                              :name      "name"
                              :required  true
                              :value     name})
              (let [card-errors (mapcat (partial get field-errors) [["card-number"]
                                                                    ["card-expiration"]
                                                                    ["security-code"]
                                                                    ["card-error"]])]
                [:div
                 [:div#card-element.border.rounded.p2
                  {:style  {:height "47px"}
                   :class (if (seq card-errors)
                            "border-error error"
                            "border-gray")}]
                 (when (seq card-errors)
                   [:div.h6.my1.error.center.medium {:data-test "payment-form-card-error"}
                    (:long-message (first card-errors))])])
              (when (and (not guest?) (empty? saved-cards))
                [:div.mb2
                 [:label
                  [:input.mr1 (merge (utils/toggle-checkbox keypaths/checkout-credit-card-save save-credit-card?)
                                     {:type     "checkbox"
                                      :data-test "payment-form-save-credit-card"})]
                  "Save my card for easier checkouts."]])]))))

(defcomponent component
  [{{:keys [focused
            selected-saved-card-id
            saved-cards
            fetching-saved-cards?
            loaded-stripe?] :as credit-card} :credit-card
    :as data}
   owner opts]
  [:div
   (if fetching-saved-cards?
     (ui/large-spinner {:style {:height "4em"}})
     [:div.my2
      (when (seq saved-cards)
        (let [card-options (conj (mapv (juxt cc/display-credit-card :id) saved-cards)
                                 ["Add a new payment card" "add-new-card"])]
          (ui/select-field {:data-test "payment-form-selected-saved-card"
                            :id        "selected-saved-card"
                            :keypath   keypaths/checkout-credit-card-selected-id
                            :focused   focused
                            :label     "Payment Card"
                            :options   card-options
                            :required  true
                            :value     selected-saved-card-id})))

      (when (and loaded-stripe?
                 (or (empty? saved-cards)
                     (= selected-saved-card-id "add-new-card")))
        (component/build new-card-component data opts))])])

(defn query [data]
  (let [saved-cards (get-in data keypaths/checkout-credit-card-existing-cards)]
    {:credit-card {:guest?                 (get-in data keypaths/checkout-as-guest)
                   :name                   (get-in data keypaths/checkout-credit-card-name)
                   :save-credit-card?      (get-in data keypaths/checkout-credit-card-save)
                   :selected-saved-card-id (get-in data keypaths/checkout-credit-card-selected-id)
                   :saved-cards            saved-cards
                   :fetching-saved-cards?  (and (utils/requesting? data request-keys/get-saved-cards)
                                                (empty? saved-cards))
                   :focused                (get-in data keypaths/ui-focus)
                   :loaded-stripe?         (get-in data keypaths/loaded-stripe)}}))

(defn built-component [data opts]
  (component/build component (query data) opts))
