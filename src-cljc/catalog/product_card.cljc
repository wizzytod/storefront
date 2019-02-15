(ns catalog.product-card
  (:require catalog.keypaths
            [catalog.selector :as selector]
            [catalog.facets :as facets]
            [spice.selector :as spice-selector]
            [storefront.accessors.skus :as skus]
            [storefront.accessors.experiments :as experiments]
            [storefront.component :as component]
            [storefront.components.money-formatters :as mf]
            [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.platform.component-utils :as utils]))

(defn slug->facet [facet facets]
  (->> facets
       (filter (fn [{:keys [:facet/slug]}] (= slug facet)))
       first))

(defn slug->option [option options]
  (->> options
       (filter (fn [{:keys [:option/slug]}] (= slug option)))
       first))

(defmulti unconstrained-facet (fn [color-order-map product skus facets facet] facet))

(defmethod unconstrained-facet :hair/length
  [_color-order-map product skus facets facet]
  (let [lengths  (->> skus
                      (mapcat #(get % :hair/length))
                      sort)
        length-facet-options (->> facets (slug->facet :hair/length) :facet/options)
        shortest (->> length-facet-options
                      (slug->option (first lengths))
                      :option/name)
        longest  (->> length-facet-options
                      (slug->option (last lengths))
                      :option/name)]
    [:p.h6.dark-gray
     "in "
     (if (= shortest longest)
       shortest
       [:span shortest " - " longest])]))

(defn facet-image
  [facets facet option]
  (->> facets
       (filter #(= facet (:facet/slug %)))
       first
       :facet/options
       (filter #(= option (:option/slug %)))
       (map :option/image)))

(defmethod unconstrained-facet :hair/color
  [color-order-map product skus facets facet-slug]
  (let [sorted-product-colors (distinct (->> skus
                                             (mapcat #(get % :hair/color))
                                             (sort-by color-order-map)))]
    [:div
     (when (> (count sorted-product-colors) 1)
       [:p.h6.dark-gray
        (for [color-url (map #(facet-image facets facet-slug %)
                             sorted-product-colors)]
          [:img.mx1.border-light-gray
           {:width  10
            :height 10
            :src    (first color-url)}])])]))

(defn sku-best-matching-selections [selections skus]
  (some (fn [criteria]
          (first (spice-selector/match-all {:selector/complete? true} criteria skus)))
        [selections
         {:hair/color (:hair/color selections)}
         {:hair/length (:hair/length selections)}]))

(defn query [data product]
  (let [selections                (get-in data catalog.keypaths/category-selections)
        skus                      (vals (select-keys (get-in data keypaths/v2-skus)
                                                     (:selector/skus product)))
        facets                    (get-in data keypaths/v2-facets)
        color-order-map           (facets/color-order-map facets)
        in-stock-skus             (selector/query skus selections {:inventory/in-stock? #{true}})
        ;; in order to fill the product card, we should always have a sku to use for
        ;; the cheapest-sku and epitome
        skus-to-search            (or (not-empty in-stock-skus) skus)
        ;; It is technically possible for the cheapest sku to not be the epitome
        cheapest-sku              (->> skus-to-search
                                       (sort-by :sku/price)
                                       first)
        ;; Product definition of epitome is the "first" SKU on the product details page where
        ;; first is when the first of every facet is selected.
        ;;
        ;; We're being lazy and sort by color facet + sku price (which implies sort by hair/length)
        epitome                   (skus/determine-epitome color-order-map skus-to-search)
        product-detail-selections (get-in data catalog.keypaths/detailed-product-selections)]
    {:product                          product
     :skus                             skus
     :epitome                          epitome
     :sku-matching-previous-selections (sku-best-matching-selections product-detail-selections skus)
     :cheapest-sku                     cheapest-sku
     :color-order-map                  color-order-map
     :sold-out?                        (empty? in-stock-skus)
     :title                            (:copy/title product)
     :slug                             (:page/slug product)
     :image                            (->> epitome
                                            :selector/images
                                            (filter (comp #{"catalog"} :use-case))
                                            (sort-by (comp color-order-map first :hair/color))
                                            first)
     :facets                           facets
     :selections                       (get-in data catalog.keypaths/category-selections)}))

(defn component
  [{:keys [product skus cheapest-sku epitome sku-matching-previous-selections sold-out? title slug image facets color-order-map]}]
  [:div.col.col-6.col-4-on-tb-dt.px1
   {:key slug}
   [:a.inherit-color
    (assoc (utils/route-to events/navigate-product-details
                           {:catalog/product-id (:catalog/product-id product)
                            :page/slug          slug
                            :query-params       {:SKU (:catalog/sku-id (or sku-matching-previous-selections epitome))}})
           :data-test (str "product-" slug))
    [:div.center.relative
     ;; TODO: when adding aspect ratio, also use srcset/sizes to scale these images.
     [:img.block.col-12 {:src (str (:url image) "-/format/auto/" (:filename image))
                         :alt (:alt image)}]
     [:h2.h4.mt3.mb1 title]
     (if sold-out?
       [:p.h6.dark-gray "Out of stock"]
       [:div
        (for [selector (reverse (:selector/electives product))]
          [:div {:key selector}
           (unconstrained-facet color-order-map product skus facets selector)])
        [:p.h6.mb4 "Starting at " (mf/as-money-without-cents (:sku/price cheapest-sku 0))]])]]])
