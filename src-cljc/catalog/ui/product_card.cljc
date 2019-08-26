(ns catalog.ui.product-card
  (:require catalog.keypaths
            [catalog.facets :as facets]
            [spice.selector :as selector]
            [storefront.accessors.skus :as skus]
            [storefront.components.money-formatters :as mf]
            [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.platform.component-utils :as utils]
            [storefront.component :as component]
            [catalog.product-card :as product-card]))

(defn- slug->facet [facet facets]
  (->> facets
       (filter (fn [{:keys [:facet/slug]}] (= slug facet)))
       first))

(defn- slug->option [option options]
  (->> options
       (filter (fn [{:keys [:option/slug]}] (= slug option)))
       first))

(defn sku-best-matching-selections
  "Find the sku best matching selectors, falling back to trying one facet at a time"
  [selections skus color-order-map]
  (some (fn [criteria]
          (skus/determine-epitome color-order-map
                                  (selector/match-all {:selector/complete? true} criteria skus)))
        [selections
         {:hair/color (:hair/color selections)}
         {:hair/length (:hair/length selections)}]))

(defn product-options
  [facets skus facet-slug]
  (let [facet-options (->> facets (slug->facet facet-slug) :facet/options)]
    (->> skus
         (mapcat #(get % facet-slug))
         set
         (map #(slug->option % facet-options))
         (sort-by (juxt :option/order :option/slug)))))

(defn query
  [data product]
  (let [skus       (vals (select-keys (get-in data keypaths/v2-skus)
                                      (:selector/skus product)))
        facets     (get-in data keypaths/v2-facets)
        selections (get-in data catalog.keypaths/category-selections)

        color-order-map (facets/color-order-map facets)
        in-stock-skus   (selector/match-all {}
                                            (assoc selections :inventory/in-stock? #{true})
                                            skus)

        ;; in order to fill the product card, we should always have a sku to use for
        ;; the cheapest-sku and epitome
        skus-to-search            (or (not-empty in-stock-skus) skus)
        ;; It is technically possible for the cheapest sku to not be the epitome:
        ;; If 10'' Black is sold out, 10'' Brown is the cheapest, but 12'' Black is the epitome
        cheapest-sku              (skus/determine-cheapest color-order-map skus-to-search)
        ;; Product definition of epitome is the "first" SKU on the product details page where
        ;; first is when the first of every facet is selected.
        ;;
        ;; We're being lazy and sort by color facet + sku price (which implies sort by hair/length)
        product-detail-selections (get-in data catalog.keypaths/detailed-product-selections)

        product-color-swatch-urls (->> (product-options facets skus :hair/color)
                                       (mapv :option/circle-swatch))
        [shortest longest]        (->> (product-options facets skus :hair/length)
                                       ((juxt first last))
                                       (mapv :option/name))

        slug (:page/slug product)

        epitome (skus/determine-epitome color-order-map skus-to-search)
        image   (->> epitome
                     :selector/images
                     (filter (comp #{"catalog"} :use-case))
                     first)]
    {:sort/value                   (:sku/price cheapest-sku)
     :react/key                    (str "product-" slug)
     :product-card-title/id        (str "product-card-title-" slug)
     :product-card-title/primary   (:copy/title product)
     :product-card/target          [events/navigate-product-details
                                    {:catalog/product-id (:catalog/product-id product)
                                     :page/slug          slug
                                     :query-params       {:SKU (:catalog/sku-id
                                                                (product-card/sku-best-matching-selections product-detail-selections
                                                                                                           skus
                                                                                                           color-order-map))}}]
     :product-card-details/id      (str "product-card-details-" slug)
     :product-card-details/content (if (empty? in-stock-skus)
                                     ["Out of stock"]
                                     [(str "in "
                                           (if (= shortest longest)
                                             shortest
                                             (str shortest " - " longest)))
                                      [:div
                                       (for [color-url product-color-swatch-urls]
                                         [:img.mx1.border-light-gray
                                          {:key    (str "product-card-details-" slug "-" color-url)
                                           :width  10
                                           :height 10
                                           :src    color-url}])]
                                      (str "Starting at $" (:sku/price cheapest-sku))])
     :card-image/src               (str (:url image) "-/format/auto/" (:filename image))
     :card-image/alt               (:alt image)}))

(defn card-image-molecule
  [{:card-image/keys [src alt]}]
  ;; TODO: when adding aspect ratio, also use srcset/sizes to scale these images.
  [:img.block.col-12 {:style {:border-radius "5px 5px 0 0"}
                      :src   src
                      :alt   alt}])

(defn product-card-title-molecule
  [{:product-card-title/keys [id primary]}]
  (when id
    (component/html
     [:h2.h5.mt3.mb1.mx1.medium
      primary])))

(defn product-card-details-molecule
  [{:product-card-details/keys [id content]}]
  (when id
    [:div.h6.dark-gray.mb4
     (for [[idx item] (map-indexed vector content)]
       [:div {:key (str id "-" idx)}
        item])]))

(defn organism
  [{:as data react-key :react/key :product-card/keys [target]}]
  (component/html
   [:a.inherit-color.col.col-6.col-4-on-tb-dt.p1
    (merge (apply utils/route-to target)
           {:key       react-key
            :data-test react-key})
    [:div.border.border-light-silver.rounded.container-height.center
     (card-image-molecule data)
     (product-card-title-molecule data)
     (product-card-details-molecule data)]]))

