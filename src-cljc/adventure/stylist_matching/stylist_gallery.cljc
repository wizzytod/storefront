(ns adventure.stylist-matching.stylist-gallery
  (:require #?@(:cljs
                [[goog.dom]
                 [goog.events.EventType :as EventType]
                 [goog.events]
                 [goog.style]
                 [storefront.api :as api]
                 [storefront.browser.scroll :as scroll]
                 [storefront.platform.messages :as messages]])
            [storefront.components.header :as header]
            [adventure.keypaths :as keypaths]
            [spice.core :as spice]
            [storefront.component :as component :refer [defcomponent defdynamic-component]]
            [storefront.components.svg :as svg]
            [storefront.components.ui :as ui]
            [storefront.effects :as effects]
            [storefront.events :as events]
            storefront.keypaths
            [storefront.platform.component-utils :as utils]
            [storefront.platform.messages :as messages]
            [storefront.transitions :as transitions]
            [stylist-directory.stylists :as stylists]))

(defn query
  [data]
  (let [stylist-id (get-in data keypaths/stylist-profile-id)
        stylist    (stylists/by-id data stylist-id)
        back       (first (get-in data storefront.keypaths/navigation-undo-stack))]
    (cond-> {}

      stylist
      (merge {:stylist-gallery-header/title       (str (stylists/->display-name stylist) "'s Recent work")
              :stylist-gallery-header/close-id    "close-stylist-gallery"
              :stylist-gallery-header/close-route (utils/route-back-or-to back
                                                                          events/navigate-adventure-stylist-profile
                                                                          {:stylist-id stylist-id
                                                                           :store-slug (:store-slug stylist)})
              :gallery                            (map (comp ui/ucare-img-id :resizable-url) (:gallery-images stylist))}))))

(defn ^:private component-overhead-magic-number [mobile-overhead desktop-overhead]
  #?(:cljs
     (if (< (.-width (goog.dom/getViewportSize)) 750)
       mobile-overhead
       desktop-overhead)))

(defn ^:private handle-scroll [component event]
  #?(:cljs
     (let [{:component/keys [mobile-overhead desktop-overhead]} (component/get-opts component)]
       (component/set-state! component
                             :show? (< (component-overhead-magic-number mobile-overhead desktop-overhead)
                                       (.-y (goog.dom/getDocumentScroll)))))))

(defn ^:private set-height [component dom-node]
  #?(:cljs
     (component/set-state! component :component-height (some-> dom-node goog.style/getSize .-height))))

(defn stylist-gallery-header-molecule
  [{:stylist-gallery-header/keys [title close-id close-route]}]
  [:div
   [:div.fixed.z4.top-0.left-0.right-0
    (header/mobile-nav-header
     {:class "border-bottom border-gray bg-white black"
      :style {:height "70px"}}
     nil
     (component/html [:div.h4.medium title])
     (component/html
      (when close-id
        [:div.ml-auto.flex.items-center.justify-around
         (merge {:data-test close-id
                 :style     {:width  "70px"
                             :height "70px"}}
                close-route)
         (svg/x-sharp {:class "black"
                       :style {:width  "14px"
                               :height "14px"}})])))]
   [:div {:style {:margin-top "70px"}}]])

(defcomponent component
  [data owner opts]
  [:div.col-12.bg-white
   (stylist-gallery-header-molecule data)
   (map-indexed (fn [ix image-id]
                  (ui/ucare-img {:class    "col-12 block"
                                 :width    580
                                 :data-ref (str "offset-" ix)} image-id))
                (:gallery data))])

(defmethod effects/perform-effects events/navigate-adventure-stylist-gallery
  [dispatch event {:keys [stylist-id query-params] :as args} prev-app-state app-state]
  #?(:cljs
     (api/fetch-stylist-details (get-in app-state storefront.keypaths/api-cache)
                                stylist-id
                                #(do (messages/handle-message events/api-success-fetch-stylist-details %)
                                     (when-let [offset (:offset query-params)]

                                       ;; We wait a moment for the images to at least start to load so that we know where to scroll to.
                                       (js/setTimeout
                                        (fn [] (scroll/scroll-to-selector (str "[data-ref=offset-" offset "]")))
                                        500))))))

(defmethod transitions/transition-state events/navigate-adventure-stylist-gallery
  [_ _ {:keys [stylist-id]} app-state]
  (assoc-in app-state keypaths/stylist-profile-id (spice/parse-int stylist-id)))

(defn built-component
  [data opts]
  (component/build component (query data) {}))
