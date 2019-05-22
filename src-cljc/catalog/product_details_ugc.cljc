(ns catalog.product-details-ugc
  (:require #?@(:cljs [[goog.string]])
            [storefront.component :as component]
            [storefront.components.ugc :as ugc]
            [storefront.components.ui :as ui]
            [storefront.platform.component-utils :as util]
            [storefront.events :as events]
            [storefront.platform.carousel :as carousel]
            [clojure.string :as str]))

(defn ^:private carousel-slide
  [destination-event product-id page-slug sku-id idx
   {:keys [image-url]}]
  [:div.p1
   [:a (if destination-event
         (util/fake-href destination-event {:offset idx})
         (util/route-to events/navigate-product-details
                        {:catalog/product-id product-id
                         :page/slug          page-slug
                         :query-params       {:SKU    sku-id
                                              :offset idx}}))
    (ui/aspect-ratio
     1 1
     {:class "flex items-center"}
     [:img.col-12 {:src image-url}])]])

(defn ->title-case [s]
  #?(:clj (str/capitalize s)
     :cljs (goog.string/toTitleCase s)))

(defn parse-int [v]
  #?(:clj (Integer/parseInt v)
     :cljs (js/parseInt v 10)))

(defn ^:private popup-slide [show-cta? long-name social-card]
  (component/build ugc/social-image-card-component
                   social-card
                   {:opts {:copy {:back-copy (str "back to " (->title-case long-name))
                                  :button-copy "View this look"}}}))

(defn component [{{:keys [social-cards product-id page-slug sku-id destination-event]} :carousel-data} owner opts]
  (component/create
   (when (seq social-cards)
     [:div.center.mt4
      [:div.h2.medium.dark-gray.crush.m2 "#MayvennMade"]
      (component/build carousel/component
                       {:slides   (map-indexed (partial carousel-slide destination-event product-id page-slug sku-id)
                                               social-cards)
                        :settings {:centerMode    true
                            ;; must be in px, because it gets parseInt'd for
                            ;; the slide width calculation
                                   :centerPadding "36px"
                            ;; The breakpoints are mobile-last. That is, the
                            ;; default values apply to the largest screens, and
                            ;; 1000 means 1000 and below.
                                   :slidesToShow  3
                                   :responsive    [{:breakpoint 1000
                                                    :settings   {:slidesToShow 2}}]}}
                       opts)
      [:p.center.dark-gray.m2
       "Want to show up on our homepage? "
       "Tag your best pictures wearing Mayvenn with " [:span.bold "#MayvennMade"]]])))

(defn popup-component [{:keys [now carousel-data offset show-cta? close-message]} owner opts]
  (let [close-attrs (apply util/route-to close-message)]
    (component/create
     ;; NOTE(jeff,corey): events/navigate-product-details should be the current
     ;; navigation event of the PDP page (freeinstall and classic have different events)
     (ui/modal
      {:close-attrs close-attrs}
      [:div.relative
       (component/build carousel/component
                        {:slides   (map (partial popup-slide show-cta? (:product-name carousel-data))
                                        (:social-cards carousel-data))
                         :settings {:slidesToShow 1
                                    :initialSlide (parse-int offset)}
                         :now      now}
                        {})
       [:div.absolute
        {:style {:top "1.5rem" :right "1.5rem"}}
        (ui/modal-close {:class       "stroke-dark-gray fill-gray"
                         :close-attrs close-attrs})]]))))
