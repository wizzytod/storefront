(ns storefront.components.footer
  (:require [storefront.platform.component-utils :as utils]
            #?(:clj [storefront.component-shim :as component]
               :cljs [storefront.component :as component])
            [storefront.events :as events]
            [storefront.accessors.named-searches :as named-searches]
            [storefront.accessors.experiments :as experiments]
            [storefront.accessors.nav :as nav]
            [storefront.components.ui :as ui]
            [storefront.components.svg :as svg]
            [storefront.accessors.stylists :refer [own-store?]]
            [storefront.platform.component-utils :as utils]
            [storefront.platform.date :as date]
            [storefront.platform.numbers :as numbers]
            [storefront.keypaths :as keypaths]))

(defn phone-href [tel-num]
  (apply str "tel://+" (filter numbers/digits tel-num)))

(defn products-section [named-searches]
  (for [{:keys [name slug]} named-searches]
    [:a.block.py1.dark-gray.light.titleize (merge {:key slug}
                                             (utils/route-to events/navigate-category {:named-search-slug slug}))
     name]))

(defn shop-section [named-searches own-store?]
  [:div.col-12
   [:div.medium.border-bottom.border-gray.mb1 "Shop"]
   [:nav.clearfix {:aria-label "Shop Products"}
    [:div.col.col-6
     (products-section (filter named-searches/is-extension? named-searches))]
    [:div.col.col-6
     (products-section (filter #(or (named-searches/is-closure-or-frontal? %)
                                    (and own-store? (named-searches/is-stylist-product? %)))
                               named-searches))]]])

(defn contacts-section [{:keys [call-number sms-number contact-email]}]
  [:div
   [:div.medium.border-bottom.border-gray.mb1 "Contact"]
   [:div.dark-gray.light
    [:div.py1
     [:span.hide-on-tb-dt [:a.dark-gray {:href (phone-href call-number)} call-number]] ;; mobile
     [:span.hide-on-mb call-number] ;; desktop
     " | 9am-5pm PST M-F"]
    [:a.block.py1.dark-gray {:href (str "mailto:" contact-email)} contact-email]]

   [:div.py1.hide-on-tb-dt
    (ui/ghost-button {:href (phone-href call-number)
                      :class "my1"}
                     [:div.flex.items-center.justify-center
                      (svg/phone-ringing {})
                      [:div.ml1.left-align "Call Now"]])
    (ui/ghost-button {:href (str "sms://+1" sms-number)
                      :class "my1"}
                     [:div.flex.items-center.justify-center
                      (svg/message-bubble {})
                      [:div.ml1.left-align "Send Message"]])
    (ui/ghost-button {:href (str "mailto:" contact-email)
                      :class "my1"}
                     [:div.flex.items-center.justify-center
                      (svg/mail-envelope {})
                      [:div.ml1.left-align "Send Email"]])]])

(def social-section
  (component/html
   [:div
    [:div.medium.border-bottom.border-gray
     [:div.hide-on-mb ui/nbsp]]
    [:div.border-bottom.border-gray.p1.flex.items-center.justify-around.py2
     [:a.block {:item-prop "sameAs"
                :href "https://www.facebook.com/MayvennHair"}
      [:div {:style {:width "22px" :height "22px"}} svg/mayvenn-on-facebook]]
     [:a.block {:item-prop "sameAs"
                :href "http://instagram.com/mayvennhair"}
      [:div {:style {:width "22px" :height "22px"}} svg/mayvenn-on-instagram]]
     [:a.block {:item-prop "sameAs"
                :href "https://twitter.com/MayvennHair"}
      [:div {:style {:width "22px" :height "22px"}} svg/mayvenn-on-twitter]]
     [:a.block {:item-prop "sameAs"
                :href "http://www.pinterest.com/mayvennhair/"}
      [:div {:style {:width "22px" :height "22px"}} svg/mayvenn-on-pinterest]]]]))

(defn full-component [{:keys [named-searches
                              contacts
                              own-store?]} owner opts]
  (component/create
   [:div.h5.border-top.border-gray.bg-light-gray
    [:div.container
     [:div.col-12.clearfix
      [:div.col-on-tb-dt.col-4-on-tb-dt.px3.my2 (shop-section named-searches own-store?)]
      [:div.col-on-tb-dt.col-4-on-tb-dt.px3.my2 (contacts-section contacts)]
      [:div.col-on-tb-dt.col-4-on-tb-dt.px3.my2 social-section]]]

    [:div.mt3.bg-dark-gray.white.py1.px3.clearfix.h7
     [:div.left
      {:item-prop "name"
       :content "Mayvenn Hair"}
      [:span "© " (date/full-year (date/current-date)) " "] "Mayvenn"]
     [:div.right
      [:a.white
       (utils/route-to events/navigate-content-about-us) "About"]
      " - "
      [:span
       [:a.white {:href "https://jobs.mayvenn.com"}
        "Careers"]
       " - "]
      [:a.white
       (utils/route-to events/navigate-content-help) "Contact"]
      " - "
      [:a.white
       (assoc (utils/route-to events/navigate-content-privacy)
              :data-test "content-privacy") "Privacy"]
      " - "
      [:a.white (assoc (utils/route-to events/navigate-content-tos)
                       :data-test "content-tos") "Terms"]]]]))

(defn minimal-component [{:keys [call-number]} owner opts]
  (component/create
   [:div.border-top.border-gray.bg-white
    [:div.container
     [:div.center.px3.my2
      [:div.my1.medium.dark-gray "Need Help?"]
      [:div.dark-gray.light.h5
       [:span.hide-on-tb-dt [:a.dark-gray {:href (phone-href call-number)} call-number]]
       [:span.hide-on-mb call-number]
       " | 9am-5pm PST M-F"]]]]))

(defn contacts-query [data]
  {:sms-number    (get-in data keypaths/sms-number)
   :call-number   "+1 (888) 562-7952"
   :contact-email "help@mayvenn.com"})

(defn query [data]
  {:named-searches (named-searches/current-named-searches data)
   :contacts       (contacts-query data)
   :own-store?     (own-store? data)})

(defn built-component [data opts]
  (if (nav/minimal-events (get-in data keypaths/navigation-event))
    (component/build minimal-component (contacts-query data) nil)
    (component/build full-component (query data) nil)))
