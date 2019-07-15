(ns adventure.components.profile-card
  (:require
   #?@(:cljs [[storefront.hooks.stringer :as stringer]])
   [storefront.accessors.stylists :as stylists]
   [storefront.component :as component]
   [storefront.components.formatters :as formatters]
   [storefront.components.ui :as ui]
   [storefront.effects :as effects]
   [storefront.events :as events]
   [storefront.platform.component-utils :as utils]
   [storefront.trackings :as trackings]
   [clojure.string :as string]
   [spice.date :as date]))

(defn component [{:keys [image-url title subtitle rating detail-line detail-attributes]} _ _]
  (component/create
   [:div.flex.bg-white.px1.mxn2.rounded.py3
    ;; TODO: image-url should be format/auto?
    [:div.mr2 (ui/circle-picture {:width "104px"} image-url)]
    [:div.flex-grow-1.left-align.dark-gray.h7.line-height-4
     [:div.h3.black.line-height-1 title]
     [:div.pyp2 (ui/star-rating rating)]
     [:div.bold subtitle]
     [:div detail-line]
     [:div
      (into [:div.flex.flex-wrap]
            (comp
             (remove nil?)
             (map (fn [x] [:div x]))
             (interpose [:div.mxp3 "·"]))
            detail-attributes)]]]))

;; TODO: find a better place for this query function
(defn stylist-profile-card-data [stylist]
  {:image-url         (-> stylist :portrait :resizable-url)
   :title             [:div {:data-test "stylist-name"}
                       (stylists/->display-name stylist)]
   :subtitle          (let [{:keys [name
                                    address-1
                                    address-2
                                    city
                                    state
                                    zipcode]}        (:salon stylist)
                            google-maps-redirect-url (str "https://www.google.com/maps/place/"
                                                          (string/join "+" (list address-1 address-2 city state zipcode)))]
                        [:div.py1
                         [:div name]
                         [:a.navy
                          (merge
                           {:data-test "stylist-salon-address"}
                           (utils/route-to events/control-adventure-stylist-salon-address-clicked
                                           {:stylist-id               (:stylist-id stylist)
                                            :google-maps-redirect-url google-maps-redirect-url}))
                          (when address-1
                            [:div address-1
                             (when address-2
                               [:span ", " address-2])])
                          [:div city ", " state " " zipcode]]])
   :rating            (:rating stylist)
   :detail-line       (ui/link :link/phone
                               :a.navy
                               {:data-test "stylist-phone"
                                :on-click  (utils/send-event-callback events/control-adventure-stylist-phone-clicked {:stylist-id   (:stylist-id stylist)
                                                                                                                      :phone-number (formatters/phone-number (:phone (:address stylist)))})}
                               (formatters/phone-number (:phone (:address stylist))))
   :detail-attributes [(when (:licensed stylist)
                         "Licensed")
                       (case (-> stylist :salon :salon-type)
                         "salon"   "In-Salon"
                         "in-home" "In-Home"
                         nil)
                       (when (:stylist-since stylist)
                         (str (ui/pluralize-with-amount
                               (- (date/year (date/now)) (:stylist-since stylist))
                               "yr")
                              " Experience"))]})

(defmethod trackings/perform-track events/control-adventure-stylist-phone-clicked
  [_ event {:keys [stylist-id phone-number]} app-state]
  #?(:cljs
     (stringer/track-event "stylist_phone_clicked"
                           {:stylist_id stylist-id}
                           events/external-redirect-phone
                           {:phone-number phone-number})))

(defmethod effects/perform-effects events/external-redirect-phone [_ event {:keys [phone-number]} _ app-state]
  #?(:cljs
     (set! (.-location js/window) (ui/phone-url phone-number))))

(defmethod trackings/perform-track events/control-adventure-stylist-salon-address-clicked
  [_ event {:keys [stylist-id google-maps-redirect-url]} app-state]
  #?(:cljs
     (stringer/track-event "salon_address_clicked"
                           {:stylist_id stylist-id}
                           events/external-redirect-google-maps
                           {:google-maps-redirect-url google-maps-redirect-url})))

(defmethod effects/perform-effects events/external-redirect-google-maps [_ event {:keys [google-maps-redirect-url]} _ app-state]
  #?(:cljs
     (set! (.-location js/window) google-maps-redirect-url)))
