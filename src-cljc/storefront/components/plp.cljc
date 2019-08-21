(ns storefront.components.plp
  (:require #?@(:cljs [[storefront.history :as history]
                       [storefront.api :as api]
                       [storefront.platform.messages :as messages]])
            [ui.product-card :as product-card]
            [storefront.component :as component]
            [storefront.components.ui :as ui]
            [storefront.effects :as effects]
            [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.platform.component-utils :as utils]
            [ui.molecules :as ui.M]))

(defn component [{:keys [product-card-data] :as query-data} owner opts]
  (component/create
   [:div.container
    (component/build ui.M/hero query-data nil)
    [:div.center.mx2
     [:div.purple.h7.medium.mbn1.mt3
      "NEW!"]
     [:div.h1 "Mayvenn Install"]
     [:div.h5.dark-gray.light.my2.mx6-on-mb.col-8-on-tb-dt.mx-auto-on-tb-dt

      "Save 10% on your hair & get a free Mayvenn Install by a licensed stylist when you purchase 3 or more items. "
      [:a.teal.h6.medium
       {:on-click (utils/send-event-callback events/popup-show-adventure-free-install)}
       "learn" ui/nbsp "more"]]]
    [:div.px3
     (for [[texture products] product-card-data]
       [:div.flex.flex-wrap
        [:h1.col-12 texture]
        (for [product products]
          (component/build product-card/organism product {}))])]]))

(defn category-key->order-rank [texture-facet-options category-key]
  (->> texture-facet-options
       (filter #(= category-key (:option/slug %)))
       first
       :filter/order))


(defn query [data]
  (let [texture-facet-options (->> (get-in data keypaths/v2-facets)
                                  (filter #(= :hair/texture (:facet/slug %)))
                                  first
                                  :facet/options)]
    {:mob-uuid          "4f9bc98f-2834-4e1f-9e9e-4ca680edd81f"
     :dsk-uuid          "b1d0e399-8e62-4f34-aa17-862a9357000b"
     :file-name         "plp-hero-image"
     :alt               "New Mayvenn Install"
     :opts              (utils/scroll-href "mayvenn-free-install-video")
     :product-card-data (->> (get-in data keypaths/v2-products)
                             vals
                             (group-by (comp first :hair/texture))
                             (reduce-kv (fn [m fam cat] (assoc m fam (map #(product-card/query data %) cat))) {})
                             (sort-by #(category-key->order-rank texture-facet-options (first %))))}))

(defn built-component [data opts]
  (component/build component (query data) nil))

(defmethod effects/perform-effects events/navigate-plp [_ event args _ app-state]
  #?(:cljs (let [store-experience (get-in app-state keypaths/store-experience)
                 store-slug       (get-in app-state keypaths/store-slug)]
             (when-not (or (= "aladdin" store-experience)
                           (= "shop" store-slug))
               (history/enqueue-redirect events/navigate-home))
             (api/search-v2-products (get-in app-state keypaths/api-cache)
                                     {:promo.mayvenn-install/eligible true}
                                     #(messages/handle-message events/api-success-v2-products-for-browse %)))))