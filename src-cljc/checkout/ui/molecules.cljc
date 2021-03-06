(ns checkout.ui.molecules
  (:require [storefront.components.ui :as ui]
            [storefront.components.svg :as svg]
            [storefront.component :as component]
            [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.platform.component-utils :as utils]))

(defn promo-entry
  [{:keys [focused coupon-code field-errors updating? applying? error-message] :as promo-data}]
  [:form.mt2.bg-white
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
    {:ui-element ui/button-large-primary
     :content    "Apply"
     :args       {:on-click    (utils/send-event-callback events/control-cart-update-coupon)
                  :class       "flex justify-center items-center"
                  :data-test   "cart-apply-promo"
                  :disabled?   updating?
                  :spinning?   applying?}})])

(defn cart-summary-line-molecule
  [{:cart-summary-line/keys
    [id label sublabel icon value action-id action-target action-icon class]}]
  (component/html
   (when id
     [:tr.proxima.content-2
      {:data-test (str "cart-summary-line-" id)
       :key       (str "cart-summary-line-" id)}
      [:td.pyp1.flex.items-center.align-middle
       (svg/symbolic->html icon)
       ^String (str label)
       (when sublabel
         [:div.h7.ml1 ^String (str sublabel)])
       (when action-id
         [:a.ml1.h6.gray.flex.items-center
          ^:attrs (merge {:data-test action-id}
                         (apply utils/fake-href action-target))
          (svg/symbolic->html action-icon)])]
      [:td.pyp1.right-align.medium
       {:class class}
       value]])))

(defn cart-summary-total-line
  [{:cart-summary-total-line/keys [id label value]}]
  (component/html
   (when id
     [:div.flex {:data-test id}
      [:div.flex-auto.content-1.proxima label]
      [:div.right-align.title-2.proxima value]])))

(defn cart-summary-total-incentive
  [{:cart-summary-total-incentive/keys [id label savings]}]
  (component/html
   (when id
     [:div.flex.justify-end {:data-test id}
      [:div.right-align.content-3
       [:div.bg-warm-gray.px2.py1.nowrap.mb1
        label]
       (when savings
         [:div.light.pxp1.nowrap.italic
          "You've saved "
          [:span.bold.p-color {:data-test "total-savings"}
           savings]])]])))

(defn freeinstall-informational
  [{:freeinstall-informational/keys
    [id primary secondary cta-label cta-target fine-print
     secondary-link-id secondary-link-label secondary-link-target
     button-id]}]
  (component/html
   (when id
     [:div.flex.py2 {:data-test id}
      "✋"
      [:div.flex.flex-column.pl1
       [:div.proxima.content-2.line-height-1.bold
        primary]
       [:div.content-3.proxima
        secondary]
       [:div.flex.justify-left.py1
        (ui/button-small-primary
         (assoc (apply utils/fake-href cta-target) :data-test button-id)
         cta-label)
        (when secondary-link-id
          [:div.s-color.flex.items-center.px2.button-font-3.shout
           [:a
            ^:attrs (merge
                     (apply utils/fake-href secondary-link-target)
                     {:class     "inherit-color border-bottom border-width-2"
                      :data-test secondary-link-id})
            secondary-link-label]])]
       [:div.content-4.dark-gray
        fine-print]]])))
