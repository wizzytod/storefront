(ns storefront.components.checkout-address
  (:require [om.core :as om]
            [sablono.core :refer-macros [html]]
            [storefront.keypaths :as keypaths]
            [storefront.events :as events]
            [storefront.components.utils :as utils]
            [storefront.components.checkout-steps :refer [checkout-step-bar]]
            [clojure.string :as string]))

(defn selected-value->int [evt]
  (let [elem (.-target evt)]
    (-> (.-value
         (aget (.-options elem)
               (.-selectedIndex elem)))
        (js/parseInt 10))))

(defn textfield [name & [{:keys [id value required? type on-change] :or {type "text"}}]]
  [:p.field
   [:label {:for id} name (when required? [:span.required "*"])]
   [:input {:id id
            :name id
            :type type
            :class (if required? "required" "")
            :value value
            :on-change on-change}]])

(defn selectfield [name & [{:keys [id value options required? on-change]}]]
  [:p.field
   [:label {:for id} name (when required? [:span.required "*"])]
   [:span
    [:br]
    [:select {:class (if required? "required" "")
              :name id
              :id id
              :on-change on-change
              :value value}
     [:option ""]
     (map (fn [{name :name val :id}]
            [:option {:value val} (str name)])
          options)]]])

(defn checkbox [name & [{:keys [id class value on-change checked]}]]
  [:p.field {:class class}
   [:input {:type "checkbox" :id id :name id :value value :on-change on-change :checked checked}]
   [:label {:for id} " " name]])

(defn billing-address-form [data]
  [:div.billing-address-wrapper
   [:fieldset#billing.billing-fieldset
    [:legend {:align "center"} "Billing Address"]
    [:p.checkout-address-warning
     "Please note: If your billing address does not match your credit card your order will be delayed."]

    [:div.inner
     (textfield "First Name"
                (merge (utils/update-text data events/control-checkout-change keypaths/checkout-billing-address-firstname-path)
                       {:id :firstname
                        :required? true
                        :value (str (get-in data keypaths/checkout-billing-address-firstname-path))}))
     (textfield "Last Name"
                (merge (utils/update-text data events/control-checkout-change keypaths/checkout-billing-address-lastname-path)
                       {:id :lastname
                        :required? true
                        :value (str (get-in data keypaths/checkout-billing-address-lastname-path))}))
     (textfield "Street Address"
                (merge (utils/update-text data events/control-checkout-change keypaths/checkout-billing-address-address1-path)
                       {:id :address1
                        :required? true
                        :value (str (get-in data keypaths/checkout-billing-address-address1-path))}))
     (textfield "Street Address (cont'd)"
                (merge (utils/update-text data events/control-checkout-change keypaths/checkout-billing-address-address2-path)
                       {:id :address2
                        :value (str (get-in data keypaths/checkout-billing-address-address2-path))}))
     (textfield "City"
                (merge (utils/update-text data events/control-checkout-change keypaths/checkout-billing-address-city-path)
                       {:id :city
                        :required? true
                        :value (str (get-in data keypaths/checkout-billing-address-city-path))}))
     (selectfield "State"
                  {:id :state
                   :required? true
                   :options (get-in data keypaths/states-path)
                   :value (get-in data keypaths/checkout-billing-address-state-path)
                   :on-change #(utils/put-event data
                                                events/control-checkout-change
                                                {keypaths/checkout-billing-address-state-path (selected-value->int %)})})
     (textfield "Zip"
                (merge (utils/update-text data events/control-checkout-change keypaths/checkout-billing-address-zip-path)
                       {:id :zipcode
                        :required? true
                        :value (str (get-in data keypaths/checkout-billing-address-zip-path))}))
     (textfield "Mobile Phone"
                (merge (utils/update-text data events/control-checkout-change keypaths/checkout-billing-address-phone-path)
                       {:id :order_bill_address_attributes_phone
                        :required? true
                        :type "tel"
                        :value (str (get-in data keypaths/checkout-billing-address-phone-path))}))
     (checkbox "Save my address"
               (merge (utils/update-checkbox data
                                             (get-in data keypaths/checkout-billing-address-save-my-address-path)
                                             events/control-checkout-change
                                             keypaths/checkout-billing-address-save-my-address-path)
                      {:id "save_user_address" :class "checkout-save-address"}))]]])

(defn shipping-address-form [data]
  [:div.shipping-address-wrapper
   [:fieldset#shipping.shipping-fieldset
    [:legend {:align "center"} "Shipping Address"]
    (checkbox "Use Billing Address"
              (merge (utils/update-checkbox data
                                            (get-in data keypaths/checkout-shipping-address-use-billing-address-path)
                                            events/control-checkout-change
                                            keypaths/checkout-shipping-address-use-billing-address-path)
                     {:id "use_billing" :class "checkbox checkout-use-billing-address"}))

    [:div.inner {:class (if (get-in data keypaths/checkout-shipping-address-use-billing-address-path) "hidden" "")}
     (textfield "First Name"
                (merge (utils/update-text data events/control-checkout-change keypaths/checkout-shipping-address-firstname-path)
                       {:id :firstname
                        :required? true
                        :value (str (get-in data keypaths/checkout-shipping-address-firstname-path))}))
     (textfield "Last Name"
                (merge (utils/update-text data events/control-checkout-change keypaths/checkout-shipping-address-lastname-path)
                       {:id :lastname
                        :required? true
                        :value (str (get-in data keypaths/checkout-shipping-address-lastname-path))}))
     (textfield "Street Address"
                (merge (utils/update-text data events/control-checkout-change keypaths/checkout-shipping-address-address1-path)
                       {:id :address1
                        :required? true
                        :value (str (get-in data keypaths/checkout-shipping-address-address1-path))}))
     (textfield "Street Address (cont'd)"
                (merge (utils/update-text data events/control-checkout-change keypaths/checkout-shipping-address-address2-path)
                       {:id :address2
                        :value (str (get-in data keypaths/checkout-shipping-address-address2-path))}))
     (textfield "City"
                (merge (utils/update-text data events/control-checkout-change keypaths/checkout-shipping-address-city-path)
                       {:id :city
                        :required? true
                        :value (str (get-in data keypaths/checkout-shipping-address-city-path))}))
     (selectfield "State"
                  {:id :state
                   :required? true
                   :options (get-in data keypaths/states-path)
                   :value (get-in data keypaths/checkout-shipping-address-state-path)
                   :on-change #(utils/put-event data
                                                events/control-checkout-change
                                                {keypaths/checkout-shipping-address-state-path (selected-value->int %)})})
     (textfield "Zip"
                (merge (utils/update-text data events/control-checkout-change keypaths/checkout-shipping-address-zip-path)
                       {:id :zipcode
                        :required? true
                        :value (str (get-in data keypaths/checkout-shipping-address-zip-path))}))
     (textfield "Mobile Phone"
                (merge (utils/update-text data events/control-checkout-change keypaths/checkout-shipping-address-phone-path)
                       {:id :order_bill_address_attributes_phone
                        :required? true
                                :type "tel"
                                :value (str (get-in data keypaths/checkout-shipping-address-phone-path))}))]]])

(defn checkout-address-component [data owner]
  (om/component
   (html
    [:div#checkout
     (checkout-step-bar data)
     [:div.row
      [:div.checkout-form-wrapper
       [:form.edit_order
        {:action "TODO: /checkout/update/address"
         :method "POST"
         :on-submit (utils/enqueue-event data events/control-checkout-update-addresses-submit)}

        (billing-address-form data)

        (shipping-address-form data)

        [:div.form-buttons.checkout.save-and-continue
         [:input.continue.button.primary {:type "submit" :name "Commit" :value "Save and Continue"}]]]]]])))
