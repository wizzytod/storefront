(ns storefront.accessors.categories
  (:require [storefront.accessors.products :as products]
            [catalog.keypaths]
            [catalog.skuers :as skuers]
            [cemerick.url :as cemerick-url]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.string :as string]
            [spice.maps :as maps]
            [spice.selector :as selector]
            [storefront.keypaths :as keypaths]))

(def query-param-separator "~")

(def query-params->facet-slugs
  {:grade         :hair/grade
   :family        :hair/family
   :origin        :hair/origin
   :weight        :hair/weight
   :texture       :hair/texture
   :base-material :hair/base-material
   :color         :hair/color
   :length        :hair/length
   :color.process :hair/color.process})

(defn query-params->selector-electives [query-params]
  (->> (maps/select-rename-keys query-params query-params->facet-slugs)
       (maps/map-values #(set (.split (str %) query-param-separator)))))

(defn id->category [id categories]
  (->> categories
       (filter (comp #{(str id)} :catalog/category-id))
       first))

(defn named-search->category [named-search-slug categories]
  (->> categories
       (filter #(= named-search-slug
                   (:legacy/named-search-slug %)))
       first))

;; TODO: this should receive categories as first arg instead of app state
(defn current-traverse-nav [data]
  (id->category (get-in data keypaths/current-traverse-nav-id)
                (get-in data keypaths/categories)))

;; TODO: this should receive categories as first arg instead of app state
(defn current-category [data]
  (id->category (get-in data catalog.keypaths/category-id)
                (get-in data keypaths/categories)))

(defn canonical-category-id
  "With ICPs, the 'canonical category id' may be different from the ICP category
  id. E.g. 13-wigs with a selected family of 'lace-front-wigs' will have a
  canonical cateogry id of 24, or in other words, lace-front-wigs' category id."
  [data]
  (let [current-category  (current-category data)
        query-selections  (:query (get-in data keypaths/navigation-uri))
        query-map         #?(:clj (cemerick-url/query->map query-selections)
                             :cljs query-selections)
        categories        (get-in data keypaths/categories)
        single-categories (filter #(= 1 (count (:hair/family %))) categories)
        family-selection  (some-> (get query-map "family")
                                  (string/split #"~"))]

    ;; NOTE: this cond will be built out to consider texture selections for the bundle category page in the future (and perhaps other ICPs)
    (cond
      (and family-selection (= (count family-selection) 1))
      (:catalog/category-id (first (filter #(some (:hair/family %) family-selection) single-categories)))

      :else (:catalog/category-id current-category))))

(defn wig-category? [category]
  (-> category
      :hair/family
      first
      products/wig-families))
