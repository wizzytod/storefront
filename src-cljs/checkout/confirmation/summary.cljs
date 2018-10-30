(ns checkout.confirmation.summary
  (:require [checkout.cart.items :as cart-items]
            [clojure.string :as string]
            [spice.core :as spice]
            [storefront.accessors.orders :as orders]
            [storefront.accessors.experiments :as experiments]
            [storefront.component :as component]
            [storefront.components.money-formatters :as mf]
            [storefront.components.svg :as svg]
            [storefront.components.ui :as ui]
            [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.platform.component-utils :as utils]
            [storefront.request-keys :as request-keys]))

(defn order-total-title [freeinstall-line-item-data]
  (if freeinstall-line-item-data
    "Hair + Install Total"
    "Total"))

(defn non-zero-adjustment? [{:keys [price coupon-code]}]
  (or (not (= price 0))
      (#{"amazon" "freeinstall" "install"} coupon-code)))

(defn ^:private summary-row
  ([name amount] (summary-row {} name amount))
  ([row-attrs name amount]
   [:tr.h5
    (merge (when-not (pos? amount)
             {:class "teal"})
           row-attrs)
    [:td.pyp3 name]
    [:td.pyp3.right-align.medium
     {:class (when (pos? amount)
               "navy")}
     (mf/as-money-or-free amount)]]))

(defn summary-total-section [{:keys [freeinstall-line-item-data order store-credit use-store-credit?]}]
  [:div.py2.h2
   [:div.flex
    [:div.flex-auto.light (order-total-title freeinstall-line-item-data)]
    [:div.right-align.medium
     (cond-> (:total order)
       use-store-credit? (- store-credit)
       true              mf/as-money)]]
   (when freeinstall-line-item-data
     [:div
      [:div.flex.justify-end
       [:div.h6.bg-purple.white.px1.nowrap.medium.mb1
        "Includes Free Install"]]
      [:div.flex.justify-end
       [:div.h6.light.dark-gray.px1.nowrap.italic
        "You've saved "
        [:span.bold {:data-test "total-savings"}
         (mf/as-money (:total-savings freeinstall-line-item-data))]]]])])

(defn ^:private text->data-test-name [name]
  (-> name
      (string/replace #"[0-9]" (comp spice/number->word int))
      string/lower-case
      (string/replace #"[^a-z]+" "-")))

(defn component
  [{:keys [freeinstall-line-item-data
           order
           store-credit
           shipping-cost
           adjustments-including-tax
           use-store-credit?
           subtotal] :as props} owner _]

  (component/create
   [:div {:data-test "confirmation-order-summary"}
    [:div.hide-on-dt.border-top.border-light-gray]
    [:div.py1.border-bottom.border-light-gray
     [:table.col-12
      [:tbody
       (summary-row {:data-test "subtotal"} "Subtotal" subtotal)

       (for [{:keys [name price coupon-code] :as adjustment} adjustments-including-tax]
         (when (non-zero-adjustment? adjustment)
           (summary-row
            {:key       name
             :data-test (text->data-test-name name)}
            [:div.flex.items-center.align-middle
             (when (= "Bundle Discount" name)
               (svg/discount-tag {:class  "mxnp6"
                                  :height "2em" :width "2em"}))
             (orders/display-adjustment-name name)]
            (if (and freeinstall-line-item-data
                     (= "freeinstall" coupon-code))
              (- 0 (:price freeinstall-line-item-data))
              price))))

       (when shipping-cost
         (summary-row "Shipping" shipping-cost))

       (when (and (pos? store-credit) use-store-credit?)
         (summary-row "Store Credit" (- store-credit)))]]]
    (summary-total-section props)]))

(defn query [data]
  (let [order                      (get-in data keypaths/order)
        shipping-item              (orders/shipping-item order)
        freeinstall-line-item-data (cart-items/freeinstall-line-item-query data)]
    (when (and (experiments/aladdin-experience? data)
               (orders/freeinstall-applied? order))
      {:freeinstall-line-item-data freeinstall-line-item-data
       :order                      order
       :shipping-cost              (* (:quantity shipping-item) (:unit-price shipping-item))
       :adjustments-including-tax  (orders/all-order-adjustments order)
       :promo-data                 {:coupon-code   (get-in data keypaths/cart-coupon-code)
                                    :applying?     (utils/requesting? data request-keys/add-promotion-code)
                                    :focused       (get-in data keypaths/ui-focus)
                                    :error-message (get-in data keypaths/error-message)
                                    :field-errors  (get-in data keypaths/field-errors)}
       :subtotal                   (cond-> (orders/products-subtotal order)
                                     freeinstall-line-item-data
                                     (+ (spice/parse-double (:price freeinstall-line-item-data))))})))


;; TODO: keeping it for reference. Remove it soon!
#_(defn display-order-summary [order {:keys [available-store-credit use-store-credit?]}]
  (let [adjustments-including-tax (orders/all-order-adjustments order)
        shipping-item             (orders/shipping-item order)
        store-credit              (min (:total order) (or available-store-credit
                                                          (-> order :cart-payments :store-credit :amount)
                                                          0.0))]
    [:div
     [:.py2.border-top.border-bottom.border-gray
      [:table.col-12
       [:tbody
        (summary-row "Subtotal" (orders/products-subtotal order))
        (for [{:keys [name price coupon-code]} adjustments-including-tax]
          (when (or (not (= price 0))
                    (#{"amazon" "freeinstall" "install"} coupon-code))
            (summary-row
             {:key name}
             [:div {:data-test (text->data-test-name name)}
              (orders/display-adjustment-name name)]
             price)))

        (when shipping-item
          (summary-row "Shipping" (* (:quantity shipping-item) (:unit-price shipping-item))))

        (when (and (pos? store-credit) use-store-credit?)
          (summary-row "Store Credit" (- store-credit)))]]]
     [:.py2.h2
      [:.flex
       [:.flex-auto.light "Total"]
       [:.right-align
        (cond-> (:total order)
          use-store-credit? (- store-credit)
          true              mf/as-money)]]]]))