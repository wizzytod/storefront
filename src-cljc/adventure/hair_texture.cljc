(ns adventure.hair-texture
  (:require [clojure.set :as set]
            [storefront.events :as events]
            [storefront.component :as component]
            [storefront.keypaths :as keypaths]
            [adventure.keypaths :as adventure-keypaths]
            [adventure.components.multi-prompt :as multi-prompt]
            [adventure.utils.facets :as facets]
            [storefront.accessors.experiments :as experiments]))

(def option-metadata
  {"straight"   {:subtitle   "For silky, sleek looks"
                 :album-slug :shop-by-look-straight}
   "body-wave"  {:subtitle   "Soft, bouncy S-wave"
                 :album-slug :shop-by-look-body-wave}
   "loose-wave" {:subtitle   "Big, glamorous waves"
                 :album-slug :shop-by-look-loose-wave}
   "deep-wave"  {:subtitle   "C-shaped ringlet curls"
                 :album-slug :shop-by-look-deep-wave}})

(defn enriched-buttons [facet-options]
  (for [option facet-options
        :let [{:keys [subtitle album-slug]} (get option-metadata (:option/slug option))]]
    {:text             [:div.mynp6
                        [:div (:adventure/name option)]
                        [:div.light.h6 subtitle]]
     :data-test-suffix (:option/slug option)
     :value            {:texture (:option/slug option)}
     :target-message   [events/navigate-adventure-select-new-look
                        {:album-keyword album-slug}]}))

(defn ^:private query [data]
  (let [texture-facet-options       (facets/available-adventure-facet-options :hair/texture
                                                                              (get-in data keypaths/v2-facets)
                                                                              (get-in data adventure-keypaths/adventure-matching-products))
        adventure-choices           (get-in data adventure-keypaths/adventure-choices)
        current-step                (if (-> adventure-choices :flow #{"match-stylist"}) 3 2)
        ;; TODO: move all progress value to a namespace
        progress                    12]
    {:prompt       "Which texture are you looking for?"
     :prompt-image "//ucarecdn.com/3346657d-a039-487f-98fb-68b9b050e042/-/format/auto/aladdinMatchingOverlayImagePurpleER203Lm3x.png"
     :data-test    "hair-texture"
     :current-step current-step
     :footer       ""
     :header-data  {:title                   "The New You"
                    :progress                progress
                    :back-navigation-message [events/navigate-adventure-how-shop-hair]
                    :subtitle                (str "Step " current-step " of 3")}
     :buttons      (enriched-buttons texture-facet-options)}))

(defn built-component
  [data opts]
  (component/build multi-prompt/component (query data) opts))