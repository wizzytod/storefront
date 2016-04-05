(ns storefront.components.home
  (:require [storefront.components.utils :as utils]
            [storefront.components.categories :refer [categories-component]]
            [storefront.hooks.analytics :as analytics]
            [storefront.hooks.experiments :as experiments]
            [storefront.keypaths :as keypaths]
            [storefront.accessors.taxons :refer [filter-nav-taxons]]
            [storefront.accessors.navigation :as navigation]
            [om.core :as om]
            [clojure.string :as string]
            [sablono.core :refer-macros [html]]
            [storefront.events :as events]))

(defn category [{:keys [slug]} index]
  [:a.hair-category-link
   (merge {:key slug
           :data-test (str "taxon-" slug)}
          (utils/route-to events/navigate-category
                          {:taxon-slug slug}))
   [:div.hair-container.not-decorated.no-margin
    (when (> index 5)
      {:class "extra-wide"})
    [:div.hair-taxon {:class slug}]
    [:div.hair-image.image-cover {:class slug}]]])

(defn non-frontal-home-component [data owner]
  (om/component
   (html
    [:div#home-content
     [:a.home-large-image
      (apply utils/route-to (navigation/shop-now-navigation-message data))]
     [:div.text-free-shipping-banner
      [:p "Free Shipping + 30 Day Money Back Guarantee"]]
     [:div.squashed-hair-categories
      [:h3.pick-style "Pick your style"]
      (map category
           (drop-last (filter-nav-taxons (get-in data keypaths/taxons)))
           (range))
      [:div {:style {:clear "both"}}]]
     [:div.featured-product-content.mobile-hidden
      [:figure.featured-new]
      [:a.featured-product-image
       (apply utils/route-to (navigation/shop-now-navigation-message data))]
      [:p.featured-product-banner "Introducing Peruvian In All Textures"]]
     [:div {:style {:clear "both"}}]])))

(defn frontal-home-component [data owner]
  (om/component
   (html
    [:div
     [:a.img-home.block.bg-no-repeat.bg-contain.bg-center.col-12
      (apply utils/route-to (navigation/shop-now-navigation-message data))]
     [:div.m2
      [:div.text-free-shipping-banner.col-12
       [:p "Free Shipping + 30 Day Money Back Guarantee"]]
      (om/build categories-component data)]])))

(defn home-component [data owner]
  (if (get-in data keypaths/loaded-optimizely)
    (if (experiments/frontals? data)
      (frontal-home-component data owner)
      (non-frontal-home-component data owner))
    (om/component (html [:div]))))
