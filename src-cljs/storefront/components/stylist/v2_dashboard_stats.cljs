(ns storefront.components.stylist.v2-dashboard-stats
  (:require [storefront.accessors.payouts :as payouts]
            [storefront.api :as api]
            [storefront.components.money-formatters :as mf]
            [storefront.components.stylist.stats :as stats]
            [storefront.components.svg :as svg]
            [storefront.components.ui :as ui]
            [storefront.effects :as effects]
            [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.platform.component-utils :as utils]
            [storefront.platform.numbers :as numbers]
            [storefront.request-keys :as request-keys]
            [storefront.transitions :as transitions]))

(defn earnings-count [title value]
  [:div.dark-gray.letter-spacing-0
   [:div.shout.h6 title]
   [:div.black.medium.h5 value]])

(defn progress-indicator [{:keys [value maximum]}]
  (let [bar-value (-> value (/ maximum) (* 100.0) (min 100))
        bar-width (str (numbers/round bar-value) "%")
        bar-style {:height "5px"}]
    [:div.bg-gray.flex-auto
     (cond
       (zero? value) [:div.px2 {:style bar-style}]
       (= value maximum) [:div.bg-teal.px2 {:style bar-style}]
       :else [:div.bg-teal.px2 {:style (merge bar-style {:width bar-width})}])]))

(defn ^:private cash-balance-card
  [payout-method
   expanded?
   cashing-out?
   {:as earnings :keys [cash-balance lifetime-earnings monthly-earnings]}
   {:as services :keys [lifetime-services monthly-services]}]
  (let [toggle-expand (utils/fake-href events/control-v2-stylist-dashboard-section-toggle
                                       {:keypath keypaths/v2-ui-dashboard-cash-balance-section-expanded?})]
    [:div.h6.bg-too-light-teal.px2.pt2.pb3
     [:div.letter-spacing-1.shout.dark-gray.mbnp5.flex.items-center
      [:a.dark-gray toggle-expand
       "Cash Balance"
       (svg/dropdown-arrow {:class  (str "ml1 stroke-dark-gray "
                                         (when expanded? "rotate-180"))
                            :style  {:stroke-width "2"}
                            :height ".75em"
                            :width  ".75em"})]]

     [:div.flex.mt1.items-center.justify-between
      [:a.col-5 toggle-expand
       [:div.h1.black.medium.flex (mf/as-money cash-balance)]]
      [:div.col-5
       (if (payouts/cash-out-eligible? payout-method)
         (ui/teal-button
           {:height-class   "py2"
            :data-test      "cash-out-begin-button"
            :on-click       (utils/send-event-callback events/control-stylist-dashboard-cash-out-begin
                                                       {:amount cash-balance
                                                        :payout-method-name payout-method})
            :disabled?      (not (pos? cash-balance))
            :disabled-class "bg-gray"
            :spinning?      cashing-out?}
           [:div.flex.items-center.justify-center.regular.h5
            (ui/ucare-img {:width "28" :class "mr2 flex items-center"} "3d651ddf-b37d-441b-a162-b83728f2a2eb")
            "Cash Out"])
         [:div.h7.right
          "Cash out now with " [:a.teal (utils/fake-href events/navigate-stylist-account-commission) "Mayvenn InstaPay"]])]]
     [:div
      (when-not expanded? {:class "hide"})
      [:div.flex.mt2
       [:div.col-7
        (earnings-count "Monthly Earnings" (mf/as-money monthly-earnings))]
       [:div.col-5
        (earnings-count "Lifetime Earnings" (mf/as-money lifetime-earnings))]]
      [:div.flex.pt2
       [:div.col-7
        (earnings-count "Monthly Services" monthly-services)]
       [:div.col-5
        (earnings-count "Lifetime Services" lifetime-services)]]]]))

(defn ^:private store-credit-balance-card [total-available-store-credit lifetime-earned expanded?]
  (let [toggle-expand (utils/fake-href events/control-v2-stylist-dashboard-section-toggle
                                       {:keypath keypaths/v2-ui-dashboard-store-credit-section-expanded?})]
    [:div.h6.bg-too-light-teal.px2.pt2.pb3
     [:div.letter-spacing-1.shout.dark-gray.mbnp5.flex.items-center
      [:a.dark-gray toggle-expand
       "Credit Balance"
       (svg/dropdown-arrow {:class  (str "ml1 stroke-dark-gray "
                                         (when expanded? "rotate-180"))
                            :style  {:stroke-width "2"}
                            :height ".75em"
                            :width  ".75em"})]]

     [:div.flex.items-center
      [:a.col-7 toggle-expand
       [:div.h1.black.medium.flex (mf/as-money total-available-store-credit)]]
      [:div.col-5
       (ui/teal-button (merge (utils/route-to events/navigate-shop-by-look {:album-keyword :look})
                              {:height-class "py2"
                               :disabled? (zero? total-available-store-credit)})
                       [:div.flex.items-center.justify-center.regular.h5
                        (ui/ucare-img {:width "28" :class "mr2 flex items-center"} "81775e67-9a83-46b7-b2ae-1cdb5a737876")
                        "Shop"])]]
     [:div.flex.pt2 {:class (when-not expanded? "hide")}
      [:div.col-7
       (earnings-count "Lifetime Bonuses" (mf/as-money lifetime-earned))]]]
    ))

(defn ^:private sales-bonus-progress [{:keys [previous-level next-level award-for-next-level total-eligible-sales]}]
  [:div.p2
   [:div.h6.letter-spacing-1.shout.dark-gray "Sales Bonus Progress"]
   [:div.h7
    "Sell "
    (mf/as-money (- next-level total-eligible-sales))
    " more in non-FREEINSTALL sales to earn your next "
    [:span.bold (mf/as-money award-for-next-level)]
    " in credit."]
   [:div.mtp2
    (progress-indicator {:value   (- total-eligible-sales previous-level)
                         :maximum (- next-level previous-level)})]])

(defn component
  [{:keys [cash-balance-section-expanded? store-credit-balance-section-expanded? stats total-available-store-credit payout-method cashing-out?]}]
  (let [{:keys [bonuses earnings services]} stats
        {:keys [lifetime-earned]}           bonuses]
    [:div.p2
     (cash-balance-card payout-method cash-balance-section-expanded? cashing-out? earnings services)
     [:div.mt2 (store-credit-balance-card total-available-store-credit lifetime-earned store-credit-balance-section-expanded?)]
     (sales-bonus-progress bonuses)]))

(defmethod effects/perform-effects events/v2-stylist-dashboard-stats-fetch [_ event args _ app-state]
  (let [stylist-id (get-in app-state keypaths/store-stylist-id)
        user-id    (get-in app-state keypaths/user-id)
        user-token (get-in app-state keypaths/user-token)]
    (when (and user-id user-token)
      (api/fetch-stylist-service-menu (get-in app-state keypaths/api-cache)
                                      {:user-id    user-id
                                       :user-token user-token
                                       :stylist-id stylist-id})
      (api/get-stylist-account user-id user-token)
      (api/get-stylist-dashboard-stats events/api-success-v2-stylist-dashboard-stats
                                       stylist-id
                                       user-id
                                       user-token))))

(defmethod transitions/transition-state events/api-success-v2-stylist-dashboard-stats
  [_ event {:as stats :keys [stylist earnings services store-credit-balance bonuses]} app-state]
  (-> app-state
      (assoc-in keypaths/v2-dashboard-stats stats)))

(defmethod transitions/transition-state events/control-v2-stylist-dashboard-section-toggle
  [_ event {:keys [keypath]} app-state]
  (update-in app-state keypath not))
