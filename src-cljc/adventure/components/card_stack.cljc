(ns adventure.components.card-stack
  (:require [adventure.components.header :as header]
            [adventure.components.profile-card-with-gallery :as profile-card-with-gallery]
            [storefront.component :as component]
            [storefront.components.ui :as ui]
            [storefront.platform.carousel :as carousel]
            [storefront.platform.component-utils :as utils]
            [storefront.platform.messages :as messages]
            #?@(:cljs [[storefront.history :as history]
                       [storefront.hooks.stringer :as stringer]
                       [storefront.api :as api]])))

(defn ^:private gallery-slide [index ucare-img-url]
  [:div {:key (str "gallery-slide" index)}
   (ui/aspect-ratio 1 1
                    (ui/ucare-img {:class "col-12"} ucare-img-url))])

(defn gallery-modal-component [{:keys [ucare-img-urls initially-selected-image-index close-button] :as gallery-modal} _ _]
  (component/create
   [:div
    (when (seq ucare-img-urls)
      (let [close-attrs (utils/fake-href (:target-message close-button))]
        (ui/modal
         {:close-attrs close-attrs
          :col-class   "col-12"}
         [:div.relative.mx-auto
          {:style {:max-width "750px"}}
          (component/build carousel/component
                           {:slides   (map-indexed gallery-slide ucare-img-urls)
                            :settings {:initialSlide (or initially-selected-image-index 0)
                                       :slidesToShow 1}}
                           {})
          [:div.absolute
           {:style {:top "1.5rem" :right "1.5rem"}}
           (ui/modal-close {:class       "stroke-dark-gray fill-gray"
                            :close-attrs close-attrs})]])))]))

(defn component
  [{:keys [header-data gallery-modal-data cards-data title] :as data} _ _]
  (component/create
   (when (seq cards-data)
     [:div.center.flex-auto.bg-light-lavender
      (component/build gallery-modal-component gallery-modal-data nil)
      [:div.white
       (when header-data
         (header/built-component header-data nil))]
      [:div
       [:div.flex.items-center.bold.bg-light-lavender
        {:style {:height "75px"}}]
       [:div.bg-white
        [:div.flex.flex-auto.justify-center.pt6
         [:div.h3.bold.purple title]]
        [:div.px3.p1.bg-white.flex-wrap.flex.justify-center
         [:div.col-12.py2.flex-wrap.flex.justify-center
          (for [{:keys [key] :as cd} cards-data]
            (component/build profile-card-with-gallery/component cd {:key key}))]]
        (let [{:escape-hatch/keys [navigation-event copy data-test]} data]
          [:div.h6.dark-gray.mt3.pb4
           [:div.col-7-on-tb-dt.col-9.mx-auto.mb1
            "Not ready to pick a stylist? Let a Mayvenn expert find one for you after you buy hair."]
           [:a.teal.medium (merge {:data-test data-test} (utils/route-to navigation-event)) copy]])]]])))