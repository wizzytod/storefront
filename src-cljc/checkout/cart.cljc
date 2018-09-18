(ns checkout.cart
  (:require
   #?@(:cljs [[storefront.components.popup :as popup]
              [storefront.components.order-summary :as summary]
              [storefront.api :as api]
              [storefront.history :as history]
              [storefront.hooks.apple-pay :as apple-pay]
              [storefront.browser.cookie-jar :as cookie-jar]
              [storefront.accessors.stylist-urls :as stylist-urls]
              [goog.labs.userAgent.device :as device]])
   [cemerick.url :refer [url-encode]]
   [storefront.component :as component]
   [checkout.header :as header]
   [checkout.accessors.vouchers :as vouchers]
   [checkout.cart-call-out :as call-out]
   [checkout.suggestions :as suggestions]
   [spice.selector :as selector]
   [clojure.string :as string]
   [spice.core :as spice]
   [storefront.accessors.experiments :as experiments]
   [storefront.accessors.facets :as facets]
   [storefront.accessors.images :as images]
   [storefront.accessors.orders :as orders]
   [storefront.accessors.products :as products]
   [storefront.accessors.promos :as promos]
   [storefront.accessors.stylists :as stylists]
   [storefront.components.affirm :as affirm]
   [storefront.components.flash :as flash]
   [storefront.components.footer :as storefront.footer]
   [storefront.components.money-formatters :as mf]
   [storefront.components.promotion-banner :as promotion-banner]
   [storefront.components.stylist-banner :as stylist-banner]
   [storefront.components.svg :as svg]
   [storefront.components.ui :as ui]
   [storefront.config :as config]
   [storefront.platform.messages :as messages]
   [storefront.css-transitions :as css-transitions]
   [storefront.events :as events]
   [storefront.transitions :as transitions]
   [storefront.effects :as effects]
   [storefront.keypaths :as keypaths]
   [storefront.platform.component-utils :as utils]
   [storefront.request-keys :as request-keys]))

(defn transition-background-color [run-transition? & content]
  (if run-transition?
    (css-transitions/transition-element
     {:transitionName          "line-item-fade"
      :transitionAppearTimeout 1300
      :transitionAppear        true
      :transitionEnter         true
      :transitionEnterTimeout  1300}
     content)
    content))

(defn display-adjustable-line-items
  [recently-added-skus line-items skus update-line-item-requests delete-line-item-requests]
  (for [{sku-id :sku variant-id :id :as line-item} line-items
        :let                                       [sku                  (get skus sku-id)
                                                    legacy-variant-id    (or (:legacy/variant-id line-item) (:id line-item))
                                                    price                (or (:sku/price line-item)         (:unit-price line-item))
                                                    thumbnail            (merge
                                                                          (images/cart-image sku)
                                                                          {:data-test (str "line-item-img-" (:catalog/sku-id sku))})
                                                    removing?            (get delete-line-item-requests variant-id)
                                                    updating?            (get update-line-item-requests sku-id)
                                                    just-added-to-order? (contains? recently-added-skus sku-id)]]
    [:div.pt1.pb2 {:key (str (:catalog/sku-id sku) (:quantity line-item))}

     [:div.left.pr1
      (when-let [length (-> sku :hair/length first)]
        (transition-background-color just-added-to-order?
                                     [:div.right.z1.circle.stacking-context.border.border-light-gray.flex.items-center.justify-center.medium.h5.bg-too-light-teal
                                      {:key       (str "length-circle-" sku-id)
                                       :data-test (str "line-item-length-" sku-id)
                                       :style     {:margin-left "-21px"
                                                   :margin-top  "-10px"
                                                   :width       "32px"
                                                   :height      "32px"}} (str length "”")]))

      (transition-background-color just-added-to-order?
                                   [:div.flex.items-center.justify-center.ml1 {:key   (str "thumbnail-" sku-id)
                                                                               :style {:width "79px" :height "79px"}}
                                    [:img.block.border.border-light-gray
                                     (assoc thumbnail :style {:width "75px" :height "75px"})]])]

     [:div {:style {:margin-top "-14px"}}
      [:a.medium.titleize.h5
       {:data-test (str "line-item-title-" sku-id)}
       (:product-title line-item)]
      [:div.h6
       [:div.flex.justify-between.mt1
        [:div
         {:data-test (str "line-item-color-" sku-id)}
         (:color-name line-item)]
        [:div.flex.items-center.justify-between
         (if removing?
           [:div.h3 {:style {:width "1.2em"}} ui/spinner]
           [:a.gray.medium
            (merge {:data-test (str "line-item-remove-" sku-id)}
                   (utils/fake-href events/control-cart-remove (:id line-item)))
            (svg/trash-can {:height "1.1em"
                            :width  "1.1em"
                            :class  "stroke-dark-gray"})])]]
       [:div.flex.justify-between.mt1
        [:div.h3
         {:data-test (str "line-item-quantity-" sku-id)}
         (ui/auto-complete-counter {:spinning? updating?
                                    :data-test sku-id}
                                   (:quantity line-item)
                                   (utils/send-event-callback events/control-cart-line-item-dec
                                                              {:variant line-item})
                                   (utils/send-event-callback events/control-cart-line-item-inc
                                                              {:variant line-item}))]
        [:div.h5 {:data-test (str "line-item-price-ea-" sku-id)} (mf/as-money-without-cents price) " ea"]]]]]))

(defn ^:private non-adjustable-line-item
  [{:keys [removing? id title detail price remove-event thumbnail-image]}]
  [:div.pt1.pb2.clearfix
   [:div.left.pr3 thumbnail-image]
   [:div
    [:a.medium.titleize.h5
     {:data-test (str "line-item-title" id)}
     title]
    [:div.h6
     [:div.flex.justify-between.mt1
      [:div {:data-test (str "line-item-detail-" id)}
       detail]
      [:div.flex.items-center.justify-between
       (if removing?
         [:div.h3 {:style {:width "1.2em"}} ui/spinner]
         [:a.gray.medium
          (merge {:data-test (str "line-item-remove-" id)}
                 (apply utils/fake-href remove-event))
          (svg/trash-can {:height "1.1em"
                          :width  "1.1em"
                          :class  "stroke-dark-gray"})])]]
     [:div.h5.right {:data-test (str "line-item-price-ea-" id)} (some-> price mf/as-money)]]]])

(defn ^:private summary-row
  ([content amount] (summary-row {} content amount))
  ([row-attrs content amount]
   [:tr.h5
    (merge (when (neg? amount)
             {:class "teal"})
           row-attrs)
    [:td.pyp1 content]
    [:td.pyp1.right-align.medium
     (mf/as-money-or-free amount)]]))

(defn ^:private text->data-test-name [name]
  (-> name
      (string/replace #"[0-9]" (comp spice/number->word int))
      string/lower-case
      (string/replace #"[^a-z]+" "-")))

(defn display-order-summary [order {:keys [read-only? available-store-credit use-store-credit? promo-data]}]
  (let [adjustments-including-tax (orders/all-order-adjustments order)
        shipping-item             (orders/shipping-item order)
        store-credit              (min (:total order) (or available-store-credit
                                                          (-> order :cart-payments :store-credit :amount)
                                                          0.0))]
    [:div {:data-test "cart-order-summary"}
     [:div.hide-on-dt.border-top.border-light-gray]
     [:div.py1.border-bottom.border-light-gray
      [:table.col-12
       [:tbody
        (summary-row "Subtotal" (orders/products-subtotal order))
        (when shipping-item
          (summary-row "Shipping" (* (:quantity shipping-item) (:unit-price shipping-item))))

        (when (orders/no-applied-promo? order)
          (let [{:keys [focused coupon-code field-errors updating? applying? error-message]} promo-data]
            [:tr.h5
             [:td
              {:col-span "2"}
              [:form.mt2
               {:on-submit (utils/send-event-callback events/control-cart-update-coupon)}
               (ui/input-group
                {:keypath       keypaths/cart-coupon-code
                 :wrapper-class "flex-grow-5 clearfix"
                 :class         "h6"
                 :data-test     "promo-code"
                 :focused       focused
                 :label         "Promo code"
                 :value         coupon-code
                 :errors        (when (get field-errors ["promo-code"])
                                  [{:long-message error-message
                                    :path         ["promo-code"]}])
                 :data-ref      "promo-code"}
                {:ui-element ui/teal-button
                 :content    "Apply"
                 :args       {:on-click   (utils/send-event-callback events/control-cart-update-coupon)
                              :class      "flex justify-center items-center"
                              :size-class "flex-grow-3"
                              :data-test  "cart-apply-promo"
                              :disabled?  updating?
                              :spinning?  applying?}})]]]))

        (for [{:keys [name price coupon-code]} adjustments-including-tax]
          (when (or (not (= price 0)) (#{"amazon" "freeinstall" "install"} coupon-code))
            (summary-row
             {:key name}
             [:div.flex.items-center.align-middle {:data-test (text->data-test-name name)}
              (when (= "Bundle Discount" name)
                (svg/discount-tag {:class  "mxnp6"
                                   :height "2em" :width "2em"}))
              (orders/display-adjustment-name name)
              (when (and (not read-only?) coupon-code)
                [:a.ml1.h6.gray.flex.items-center
                 (merge {:data-test "cart-remove-promo"}
                        (utils/fake-href events/control-checkout-remove-promotion
                                         {:code coupon-code}))
                 (svg/close-x {:class "stroke-white fill-gray"})])]
             price)))

        (when (pos? store-credit)
          (summary-row "Store Credit" (- store-credit)))]]]
     [:div.py2.h2
      [:div.flex
       [:div.flex-auto.light "Total"]
       [:div.right-align.medium
        (cond-> (:total order)
          use-store-credit? (- store-credit)
          true              mf/as-money)]]]]))

(defn full-component [{:keys [products
                              order
                              disable-apple-pay-button?
                              skus
                              promotion-banner
                              updating?
                              redirecting-to-paypal?
                              share-carts?
                              requesting-shared-cart?
                              suggestions
                              focused
                              field-errors
                              line-items
                              error-message
                              coupon-code
                              update-line-item-requests
                              show-apple-pay?
                              the-ville?
                              v2-experience?
                              applying-coupon?
                              recently-added-skus
                              delete-line-item-requests
                              seventy-five-off-install?
                              show-green-banner?
                              freeinstall-line-item]} owner _]
  (component/create
   [:div.container.p2
    (component/build promotion-banner/sticky-component promotion-banner nil)

    (cond
      v2-experience?
      [:div.mb3
       (call-out/v2-cart-promo show-green-banner?)]

      seventy-five-off-install?
      [:div.mb3
       (call-out/seventy-five-off-install-cart-promo show-green-banner?)]

      the-ville?
      [:div.mb3
       (call-out/free-install-cart-promo show-green-banner?)]

      :else nil)

    [:div.clearfix.mxn3
     [:div.col-on-tb-dt.col-6-on-tb-dt.px3
      {:data-test "cart-line-items"}
      (display-adjustable-line-items recently-added-skus
                                     line-items
                                     skus
                                     update-line-item-requests
                                     delete-line-item-requests)
      (when freeinstall-line-item
        (non-adjustable-line-item freeinstall-line-item))

      (component/build suggestions/component suggestions nil)]

     [:div.col-on-tb-dt.col-6-on-tb-dt.px3
      (display-order-summary order
                             {:read-only?        false
                              :use-store-credit? false
                              :promo-data        {:coupon-code   coupon-code
                                                  :applying?     applying-coupon?
                                                  :focused       focused
                                                  :error-message error-message
                                                  :field-errors  field-errors}})

      (affirm/auto-complete-as-low-as-box {:amount (:total order)})

      (ui/teal-button {:spinning? false
                       :disabled? updating?
                       :on-click  (utils/send-event-callback events/control-checkout-cart-submit)
                       :data-test "start-checkout-button"}
                      [:div "Check out"])

      [:div.h5.black.center.py1.flex.justify-around.items-center
       [:div.flex-grow-1.border-bottom.border-light-gray]
       [:div.mx2 "or"]
       [:div.flex-grow-1.border-bottom.border-light-gray]]

      [:div.pb2
       (ui/aqua-button {:on-click  (utils/send-event-callback events/control-checkout-cart-paypal-setup)
                        :spinning? redirecting-to-paypal?
                        :disabled? updating?
                        :data-test "paypal-checkout"}
                       [:div
                        "Check out with "
                        [:span.medium.italic "PayPal™"]])]

      (when show-apple-pay?
        [:div.pb2
         (ui/apple-pay-button
          {:on-click  (utils/send-event-callback events/control-checkout-cart-apple-pay)
           :data-test "apple-pay-checkout"
           :disabled? disable-apple-pay-button?}
          [:div.flex.items-center.justify-center
           "Check out with "
           [:span.img-apple-pay.bg-fill.bg-no-repeat.inline-block.ml1.mynp6 {:style {:width  "4rem"
                                                                                     :height "2rem"}}]])])

      (when share-carts?
        [:div.py2
         [:div.h6.center.pt2.black.bold "Is this bag for a customer?"]
         (ui/navy-ghost-button {:on-click  (utils/send-event-callback events/control-cart-share-show)
                                :class     "border-width-2 border-navy"
                                :spinning? requesting-shared-cart?
                                :data-test "share-cart"}
                          [:div.flex.items-center.justify-center.bold
                           (svg/share-arrow {:class  "stroke-navy mr1 fill-navy"
                                             :width  "24px"
                                             :height "24px"})
                           "Share your bag"])])]]]))

(defn empty-component [{:keys [promotions]} owner _]
  (component/create
   (ui/narrow-container
    [:div.p2
     [:.center {:data-test "empty-bag"}
      [:div.m2 (svg/bag {:style {:height "70px" :width "70px"}
                         :class "fill-black"})]

      [:p.m2.h2.light "Your bag is empty."]

      [:div.m2
       (if-let [promo (promos/default-advertised-promotion promotions)]
         (:description promo)
         promos/bundle-discount-description)]]

     (ui/teal-button (utils/route-to events/navigate-shop-by-look {:album-keyword :look})
                     "Shop Our Looks")])))

(defn ^:private variants-requests [data request-key variant-ids]
  (->> variant-ids
       (map (juxt identity
                  #(utils/requesting? data (conj request-key %))))
       (into {})))

(defn ^:private update-pending? [data]
  (let [request-key-prefix (comp vector first :request-key)]
    (some #(apply utils/requesting? data %)
          [request-keys/add-promotion-code
           [request-key-prefix request-keys/update-line-item]
           [request-key-prefix request-keys/delete-line-item]])))

(defn add-product-title-and-color-to-line-item [products facets line-item]
  (merge line-item {:product-title (->> line-item
                                        :sku
                                        (products/find-product-by-sku-id products)
                                        :copy/title)
                    :color-name    (-> line-item
                                       :variant-attrs
                                       :color
                                       (facets/get-color facets)
                                       :option/name)}))

(defmethod effects/perform-effects events/control-cart-update-coupon
  [_ _ _ _ app-state]
  #?(:cljs
     (let [coupon-code (get-in app-state keypaths/cart-coupon-code)]
       (when-not (empty? coupon-code)
         (api/add-promotion-code (get-in app-state keypaths/session-id)
                                 (get-in app-state keypaths/order-number)
                                 (get-in app-state keypaths/order-token)
                                 coupon-code
                                 false)))))

(defmethod effects/perform-effects events/control-cart-share-show
  [_ _ _ _ app-state]
  #?(:cljs
     (api/create-shared-cart (get-in app-state keypaths/session-id)
                             (get-in app-state keypaths/order-number)
                             (get-in app-state keypaths/order-token))))

(defmethod effects/perform-effects events/control-cart-remove
  [_ event variant-id _ app-state]
  #?(:cljs
     (api/delete-line-item (get-in app-state keypaths/session-id) (get-in app-state keypaths/order) variant-id)))

(defmethod effects/perform-effects events/control-cart-line-item-inc
  [_ event {:keys [variant]} _ app-state]
  #?(:cljs
     (let [sku      (get (get-in app-state keypaths/v2-skus) (:sku variant))
           order    (get-in app-state keypaths/order)
           quantity 1]
       (api/add-sku-to-bag (get-in app-state keypaths/session-id)
                           {:sku      sku
                            :token    (:token order)
                            :number   (:number order)
                            :quantity quantity}
                           #(messages/handle-message events/api-success-add-sku-to-bag
                                            {:order    %
                                             :quantity quantity
                                             :sku      sku})))))

(defmethod effects/perform-effects events/control-cart-line-item-dec
  [_ event {:keys [variant]} _ app-state]
  #?(:cljs
     (let [order (get-in app-state keypaths/order)]
       (api/remove-line-item (get-in app-state keypaths/session-id)
                             {:number     (:number order)
                              :token      (:token order)
                              :variant-id (:id variant)
                              :sku-code   (:sku variant)}
                             #(messages/handle-message events/api-success-add-to-bag {:order %})))))

(defmethod effects/perform-effects events/control-checkout-cart-submit
  [dispatch event args _ app-state]
  #?(:cljs
     ;; If logged in, this will send user to checkout-address. If not, this sets
     ;; things up so that if the user chooses sign-in from the returning-or-guest
     ;; page, then signs-in, they end up on the address page. Convoluted.
     (history/enqueue-navigate events/navigate-checkout-address)))

(defmethod effects/perform-effects events/control-checkout-cart-apple-pay
  [dispatch event args _ app-state]
  #?(:cljs
     (apple-pay/begin (get-in app-state keypaths/order)
                      (get-in app-state keypaths/session-id)
                      (cookie-jar/retrieve-utm-params (get-in app-state keypaths/cookie))
                      (get-in app-state keypaths/shipping-methods)
                      (get-in app-state keypaths/states))))

(defmethod effects/perform-effects events/control-checkout-cart-paypal-setup
  [dispatch event args _ app-state]
  #?(:cljs
     (let [order (get-in app-state keypaths/order)]
       (api/update-cart-payments
        (get-in app-state keypaths/session-id)
        {:order (-> app-state
                    (get-in keypaths/order)
                    (select-keys [:token :number])
                 ;;; Get ready for some nonsense!
                    ;;
                    ;; Paypal requires that urls are *double* url-encoded, such as
                    ;; the token part of the return url, but that *query
                    ;; parameters* are only singley encoded.
                    ;;
                    ;; Thanks for the /totally sane/ API, PayPal.
                    (assoc-in [:cart-payments]
                              {:paypal {:amount (get-in app-state keypaths/order-total)
                                        :mobile-checkout? (not (device/isDesktop))
                                        :return-url (str stylist-urls/store-url "/orders/" (:number order) "/paypal/"
                                                         (url-encode (url-encode (:token order)))
                                                         "?sid="
                                                         (url-encode (get-in app-state keypaths/session-id)))
                                        :callback-url (str config/api-base-url "/v2/paypal-callback?number=" (:number order)
                                                           "&order-token=" (url-encode (:token order)))
                                        :cancel-url (str stylist-urls/store-url "/cart?error=paypal-cancel")}}))
         :event events/external-redirect-paypal-setup}))))

(defn full-cart-query [data]
  (let [order                        (get-in data keypaths/order)
        products                     (get-in data keypaths/v2-products)
        facets                       (get-in data keypaths/v2-facets)
        line-items                   (map (partial add-product-title-and-color-to-line-item products facets) (orders/product-items order))
        variant-ids                  (map :id line-items)
        store-nickname               (get-in data keypaths/store-nickname)
        highest-value-service        (some-> order
                                             orders/product-items
                                             vouchers/product-items->highest-value-service)
        {:as   campaign
         :keys [:voucherify/campaign-name
                :service/diva-type]} (->> (get-in data keypaths/environment)
                                          vouchers/campaign-configuration
                                          (filter #(= (:service/type %) highest-value-service))
                                          first)
        service-price                (some-> data
                                             (get-in keypaths/store-service-menu)
                                             (get diva-type))]
    {:suggestions               (suggestions/query data)
     :order                     order
     :line-items                line-items
     :skus                      (get-in data keypaths/v2-skus)
     :products                  products
     :show-green-banner?        (or (orders/install-applied? order)
                                    (orders/freeinstall-applied? order))
     :coupon-code               (get-in data keypaths/cart-coupon-code)
     :promotion-banner          (promotion-banner/query data)
     :updating?                 (update-pending? data)
     :applying-coupon?          (utils/requesting? data request-keys/add-promotion-code)
     :redirecting-to-paypal?    (get-in data keypaths/cart-paypal-redirect)
     :share-carts?              (stylists/own-store? data)
     :requesting-shared-cart?   (utils/requesting? data request-keys/create-shared-cart)
     :show-apple-pay?           (and (get-in data keypaths/show-apple-pay?)
                                     (seq (get-in data keypaths/shipping-methods))
                                     (seq (get-in data keypaths/states)))
     :disable-apple-pay-button? (get-in data keypaths/disable-apple-pay-button?)
     :update-line-item-requests (merge-with
                                 #(or %1 %2)
                                 (variants-requests data request-keys/add-to-bag (map :sku line-items))
                                 (variants-requests data request-keys/update-line-item (map :sku line-items)))
     :delete-line-item-requests (variants-requests data request-keys/delete-line-item variant-ids)
     :field-errors              (get-in data keypaths/field-errors)
     :error-message             (get-in data keypaths/error-message)
     :focused                   (get-in data keypaths/ui-focus)
     :the-ville?                (experiments/the-ville? data)
     :v2-experience?            (experiments/v2-experience? data)
     :seventy-five-off-install? (experiments/seventy-five-off-install? data)
     :recently-added-skus       (get-in data keypaths/cart-recently-added-skus)
     :stylist-service-menu      (get-in data keypaths/stylist-service-menu)
     :freeinstall-line-item     (when (and (experiments/aladdin-freeinstall-line-item? data)
                                           (experiments/aladdin-experience? data)
                                           (orders/freeinstall-applied? order))
                                  {:removing?       (utils/requesting? data request-keys/remove-promotion-code)
                                   :id              "freeinstall"
                                   :title           campaign-name
                                   :detail          (str "w/ " store-nickname)
                                   :price           service-price
                                   :remove-event    [events/control-checkout-remove-promotion {:code "freeinstall"}]
                                   :thumbnail-image [:div.flex.items-center.justify-center.ml1
                                                     {:style {:width  "79px"
                                                              :height "79px"}}
                                                     (ui/ucare-img {:width 79}
                                                                   "d2a9e626-a6f3-4cbe-804f-214ea1d92f9b")]})}))

(defn empty-cart-query [data]
  {:promotions (get-in data keypaths/promotions)})

(defn component
  [{:keys [fetching-order?
           item-count
           empty-cart
           full-cart]} owner opts]
  (component/create
   (if fetching-order?
     [:div.py3.h2 ui/spinner]
     [:div.col-7-on-dt.mx-auto
      (if (zero? item-count)
        (component/build empty-component empty-cart opts)
        (component/build full-component full-cart opts))])))

(defn query [data]
  {:fetching-order? (utils/requesting? data request-keys/get-order)
   :item-count      (orders/product-quantity (get-in data keypaths/order))
   :empty-cart      (empty-cart-query data)
   :full-cart       (full-cart-query data)})

(defn built-component [data opts]
  (component/build component (query data) opts))

(defn layout [data nav-event]
  [:div.flex.flex-column {:style {:min-height    "100vh"
                                  :margin-bottom "-1px"}}
   (stylist-banner/built-component data nil)
   (promotion-banner/built-component data nil)
   #?(:cljs (popup/built-component (popup/query data) nil))

   (header/built-component data nil)
   [:div.relative.flex.flex-column.flex-auto
    (flash/built-component data nil)

    [:main.bg-white.flex-auto {:data-test (keypaths/->component-str nav-event)}
     (built-component data nil)]

    [:footer
     (storefront.footer/built-component data nil)]]])
