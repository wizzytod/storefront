(ns catalog.ui.vertical-direct-to-cart-card
  (:require [storefront.component :as c]
            [storefront.components.money-formatters :as mf]
            [storefront.components.ui :as ui]
            [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.platform.component-utils :as utils]))

(defn query
  [data product]
  (let [service-sku  (-> (get-in data keypaths/v2-skus)
                         (select-keys (:selector/skus product))
                         vals
                         first)
        image        (->> service-sku
                          :selector/images
                          (filter (comp #{"catalog"} :use-case))
                          first)
        product-slug (:page/slug product)]
    {:card-image/src                           (str (:url image) "-/format/auto/" (:filename image))
     :card/type                                :vertical-direct-to-cart-card
     :card-image/alt                           (:alt image)
     :react/key                                (str "product-" product-slug)
     :vertical-direct-to-cart-card-title/id    (some->> product-slug (str "product-card-title-"))
     :vertical-direct-to-cart-card/primary     (:copy/title product)
     :vertical-direct-to-cart-card/secondary   (mf/as-money (:sku/price service-sku))
     :vertical-direct-to-cart-card/card-target [events/navigate-product-details
                                                {:catalog/product-id (:catalog/product-id product)
                                                 :page/slug          product-slug
                                                 :query-params       {:SKU (:catalog/sku-id service-sku)}}]
     :vertical-direct-to-cart-card/cta-target  [events/control-add-sku-to-bag {:sku      service-sku
                                                                               :quantity 1}]}))

(c/defcomponent card-image-molecule
  [{:keys [card-image/src card-image/alt screen/seen?]}
   _ _]
  ;; TODO: when adding aspect ratio, also use srcset/sizes to scale these images.
  (cond
    (nil? seen?) [:noscript
                  (ui/aspect-ratio
                   640 580
                   (ui/ucare-img
                    {:class "block col-12 container-height"
                     :alt   alt}
                    src))]
    seen?        (ui/aspect-ratio
                  640 580
                  (ui/ucare-img
                   {:class "block col-12 container-height"
                    :alt   alt}
                   src))
    :else        (ui/aspect-ratio
                  640 580
                  [:div.col-12.container-height
                   {:style {:height "100%"}}])))

(defn organism
  [{:as       data
    react-key :react/key
    :vertical-direct-to-cart-card/keys
    [id primary secondary cta-target card-target]}]
  (c/html
   (let [non-cta-action (merge (apply utils/route-to card-target)
                               {:key       id
                                :data-test id})]
     [:div.inherit-color.col.col-6.col-4-on-tb-dt.p1
      [:div.border.border-cool-gray.container-height.center.flex.flex-column.justify-between
       [:a.inherit-color.mb2 non-cta-action (ui/screen-aware card-image-molecule data)]
       [:div.pointer non-cta-action primary]
       [:div.pointer non-cta-action secondary]
       [:div.my2.mx5
        (ui/button-small-secondary (merge (apply utils/fake-href cta-target)
                                          {:key       react-key
                                           :data-test react-key})
                                   "Add To Cart")]]])))