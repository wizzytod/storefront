(ns ^:figwheel-load voucher.redeemed
  (:require #?@(:cljs [[storefront.history :as history]])
            [storefront.component :as component :refer [defcomponent]]
            [storefront.components.money-formatters :as mf]
            [storefront.components.svg :as svg]
            [storefront.components.ui :as ui]
            [storefront.events :as events]
            [storefront.effects :as effects]
            [storefront.platform.component-utils :as utils]
            [voucher.keypaths :as voucher-keypaths]
            [storefront.keypaths :as keypaths]
            [storefront.request-keys :as request-keys]))

(def unit-type->menu-kw-payout
  {"Free Install (with Closure)" "install-sew-in-closure"
   "Free Install (with 360)"     "install-sew-in-360-frontal"
   "Free Install (with Frontal)" "install-sew-in-frontal"
   "Free Install (Leave Out)"    "install-sew-in-leave-out"
   "Wig Customization"           "wig-customization"})

(def unit-type->menu-kw-advertised
  {"Free Install (with Closure)" "advertised-sew-in-closure"
   "Free Install (with 360)"     "advertised-sew-in-360-frontal"
   "Free Install (with Frontal)" "advertised-sew-in-frontal"
   "Free Install (Leave Out)"    "advertised-sew-in-leave-out"
   "Wig Customization"           "advertised-wig-customization"})

(def unit-type->display-name
  {"Free Install (with Closure)" "Closure Install"
   "Free Install (with 360)"     "360 Frontal Install"
   "Free Install (with Frontal)" "Frontal Install"
   "Free Install (Leave Out)"    "Leave Out Install"
   "Wig Customization"           "Wig Customization"})

(defn fine-print-molecule
  [{:fine-print/keys [id copy]}]
  (when id
    [:div.mt4.proxima.content-4.dark-gray.flex
     {:key       id
      :data-test id}
     [:div.mr1 "*"]
     [:div.flex-grow-1 copy]]))

(defn notification-molecule
  [{:notification/keys [id content]}]
  (when id
    [:div.bg-s-color.border.border-s-color.col-12.mb2
     [:div.flex-wrap.flex.items-center.content-3.proxima.bg-lighten-5.flex.justify-between
      {:key       id
       :data-test id}
      [:div.px4.py2 content]]]))

(def icon-molecule
  [:div.p-color.mt6.mb2
   (svg/diamond-check {:height "47px"
                       :width  "47px"
                       :class  "stroke-s-color"})])

(defn ^:private breakdown-molecule
  [{:breakdown/keys [primary-id primary items]}]
  (when primary-id
    [:div.col-12 {:key primary-id}
     [:div.center.title-2.canela.mb4
      {:data-test primary-id}
      primary]
     [:div.mx-auto.col-10
      (for [{:breakdown-item/keys [id label value]} items]
        [:div.flex.justify-between.bg-cool-gray.p2
         {:key       id
          :data-test id}
         [:div label]
         [:div value]])]]))

(defn cta-with-secondary-molecule
  [{:cta/keys [id copy target
               secondary-target
               secondary-id
               secondary-copy]}]
  [:div.center
   (ui/button-medium-primary (assoc (apply utils/route-to target)
                                   :data-test id
                                   :class "mb3")
                            copy)
   (ui/button-small-underline-primary
    (merge
     (apply utils/route-to secondary-target)
     {:data-test secondary-id})
    secondary-copy)])

(defcomponent spinner-molecule [_ _ _]
  [:div.mt8
   (ui/large-spinner {:style {:height "6em"}})])

(defcomponent ^:private component
  [queried-data owner opts]
  [:div.flex.flex-column.items-center.p4
   [:div.mb8.col-12 (notification-molecule queried-data)]

   (breakdown-molecule queried-data)

   [:div.mt5.col-9
    (cta-with-secondary-molecule queried-data)]

   (fine-print-molecule queried-data)])

(defn ^:private query [app-state]
  (let [voucher                   (get-in app-state voucher-keypaths/voucher-response)
        service-menu              (get-in app-state keypaths/user-stylist-service-menu)
        install-type              (-> voucher :discount :unit_type)
        payout-amount             (-> service-menu
                                      (get (keyword (get unit-type->menu-kw-payout install-type)))
                                      mf/as-money)
        install-type-display-name (get unit-type->display-name install-type)]
    {:spinning?            (utils/requesting? app-state request-keys/fetch-user-stylist-service-menu)
     :notification/id      (str "voucher-redeemed-" install-type-display-name)
     :notification/content "Voucher redeemed successfully"
     :cta/id               "view-earnings"
     :cta/target           [events/navigate-v2-stylist-dashboard-payments]
     :cta/copy             [:span.bold "View Earnings"]
     :cta/secondary-id     "redeem-voucher"
     :cta/secondary-target [events/navigate-voucher-redeem]
     :cta/secondary-copy   "Redeem Another Voucher"
     :breakdown/primary-id "redemption-amount"
     :breakdown/primary    "Payout Breakdown"
     :breakdown/items      [{:breakdown-item/id     "base-service"
                             :breakdown-item/label  install-type-display-name
                             :breakdown-item/value (str payout-amount "*")}]

     :fine-print/id   "fine-print"
     :fine-print/copy (str
                       "The Payout Breakdown above is the agreed upon amount set between you "
                       "and Mayvenn at the start of your program. This amount is less than the "
                       "price advertised on the website to customers. Charging customers for the "
                       "difference between the advertised price and payout amount will result in "
                       "your removal from the program.")}))

(defn ^:export built-component
  [data opts]
  (let [queried-data (query data)]
    (if (:spinning? queried-data)
      (component/build spinner-molecule nil nil)
      (component/build component queried-data opts))))

(defmethod effects/perform-effects events/navigate-voucher-redeemed [_ _ _ _ app-state]
  #?(:cljs
     (when-not (-> (get-in app-state voucher-keypaths/voucher-response) :discount :type)
       (history/enqueue-redirect events/navigate-home))))
