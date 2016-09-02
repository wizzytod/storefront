(ns storefront.trackings
  (:require [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.routes :as routes]
            [storefront.hooks.facebook-analytics :as facebook-analytics]
            [storefront.hooks.google-analytics :as google-analytics]
            [storefront.hooks.convert :as convert]
            [storefront.hooks.optimizely :as optimizely]
            [storefront.hooks.riskified :as riskified]
            [storefront.hooks.pixlee :as pixlee-analytics]
            [storefront.hooks.woopra :as woopra]
            [storefront.accessors.orders :as orders]
            [storefront.accessors.bundle-builder :as bundle-builder]
            [storefront.accessors.stylists :as stylists]
            [storefront.accessors.taxons :as taxons]
            [storefront.accessors.pixlee :as pixlee]
            [storefront.components.money-formatters :as mf]))

(defn ^:private pixlee-line-item [taxons {:keys [quantity unit-price product-id]}]
  {:quantity    quantity
   :price       (mf/as-money unit-price)
   :product_sku (or
                 (pixlee/sku (taxons/first-with-product-id taxons product-id))
                 "OTH")})

(defn ^:private pixlee-order [taxons order]
  {:cart_contents       (map (partial pixlee-line-item taxons)
                             (orders/product-items order))
   :cart_total          (mf/as-money (:total order))
   :cart_total_quantity (orders/product-quantity order)})

(defn ^:private pixlee-cart-item [taxon {:keys [variant quantity]}]
  {:product_sku (pixlee/sku taxon)
   :price       (mf/as-money (:price variant))
   :quantity    quantity})

(defn ^:private convert-revenue [{:keys [number total] :as order}]
  {:order-number   number
   :revenue        total
   :products-count (orders/product-quantity order)})

(defmulti perform-track identity)

(defmethod perform-track :default [dispatch event args app-state])

(defn- track-page-view [app-state]
  (let [path (routes/current-path app-state)]
    (riskified/track-page path)
    (woopra/track-page (get-in app-state keypaths/session-id)
                       (get-in app-state keypaths/order-user)
                       path)
    (google-analytics/track-page path)
    (facebook-analytics/track-page path)
    (optimizely/track-event path)))

(defmethod perform-track events/app-start [_ event args app-state]
  (track-page-view app-state))

(defmethod perform-track events/optimizely [_ event {:keys [variation]} app-state]
  (optimizely/activate-universal-analytics)
  (google-analytics/track-event "optimizely-experiment" variation))

(defmethod perform-track events/navigate [_ event args app-state]
  (let [[nav-event nav-args] (get-in app-state keypaths/navigation-message)]
    (when-not (= [nav-event nav-args] (get-in app-state keypaths/previous-navigation-message))
      (track-page-view app-state))))

(defmethod perform-track events/navigate-categories [_ event args app-state]
  (convert/track-conversion "view-categories"))

(defmethod perform-track events/navigate-category [_ event args app-state]
  (facebook-analytics/track-event "ViewContent")
  (convert/track-conversion "view-category"))

(defmethod perform-track events/control-bundle-option-select [_ event _ app-state]
  (when-let [last-step (bundle-builder/last-step (get-in app-state keypaths/bundle-builder))]
    (google-analytics/track-page (str (routes/current-path app-state)
                                      "/choose_"
                                      (clj->js last-step)))))

(defmethod perform-track events/control-add-to-bag [_ event args app-state]
  (facebook-analytics/track-event "AddToCart")
  (google-analytics/track-page (str (routes/current-path app-state) "/add_to_bag"))
  (let [taxon (taxons/current-taxon app-state)]
    (when (pixlee/content-available? taxon)
      (pixlee-analytics/track-event "add:to:cart" (pixlee-cart-item taxon args)))))

(defmethod perform-track events/api-success-add-to-bag [_ _ {:keys [variant quantity] :as args} app-state]
  (when variant
    (woopra/track-add-to-bag {:variant variant
                              :session-id (get-in app-state keypaths/session-id)
                              :quantity quantity
                              :order (get-in app-state keypaths/order)}))
  (when (stylists/own-store? app-state)
    (optimizely/set-dimension "stylist-own-store" "stylists"))
  (optimizely/track-event "add-to-bag"))

(defmethod perform-track events/control-cart-share-show [_ event args app-state]
  (google-analytics/track-page (str (routes/current-path app-state) "/Share_cart")))

(defmethod perform-track events/control-checkout-cart-submit [_ event args app-state]
  (facebook-analytics/track-event "InitiateCheckout"))

(defmethod perform-track events/control-checkout-cart-paypal-setup [_ event _ app-state]
  (facebook-analytics/track-event "InitiateCheckout"))

(defmethod perform-track events/api-success-get-saved-cards [_ event args app-state]
  (google-analytics/set-dimension "dimension2" (count (get-in app-state keypaths/checkout-credit-card-existing-cards))))

(defmethod perform-track events/order-completed [_ event {:keys [total] :as order} app-state]
  (when (stylists/own-store? app-state)
    (optimizely/set-dimension "stylist-own-store" "stylists"))
  (facebook-analytics/track-event "Purchase" {:value (str total) :currency "USD"})
  (convert/track-conversion "place-order")
  (convert/track-revenue (convert-revenue order))
  (optimizely/track-event "place-order" {:revenue (* 100 total)})
  (google-analytics/track-event "orders" "placed_total" nil (int total))
  (google-analytics/track-event "orders" "placed_total_minus_store_credit" nil (int (orders/non-store-credit-payment-amount order)))
  (pixlee-analytics/track-event "converted:photo" (pixlee-order (taxons/current-taxons app-state) order)))

(defmethod perform-track events/api-success-auth [_ event args app-state]
  (woopra/track-identify {:session-id (get-in app-state keypaths/session-id)
                          :user       (get-in app-state keypaths/user)}))

(defmethod perform-track events/api-success-update-order-update-guest-address [_ event args app-state]
  (woopra/track-identify {:session-id (get-in app-state keypaths/session-id)
                          :user       (:user (get-in app-state keypaths/order))}))

(defmethod perform-track events/convert [_ event {:keys [variation]} app-state]
  (woopra/track-experiment (get-in app-state keypaths/session-id)
                           (get-in app-state keypaths/order-user)
                           variation))
