(ns stylist-matching.ui.shopping-method-choice
  (:require [storefront.component :as component]
            [storefront.components.ui :as ui]
            [storefront.platform.component-utils :as utils]))

(defn shopping-method-choice-button-molecule
  [{:shopping-method-choice.button/keys [id target ucare-id label]}]
  (ui/white-button
   (merge {:style     {:border-radius "3px"}
           :class     "my1"
           :key       id
           :data-test id}
          (apply utils/route-to target))
   [:div.flex.items-center.justify-between
    (ui/ucare-img {:width 60} ucare-id)
    [:div.flex-auto.left-align.p3 label]
    [:div.p2 (ui/forward-caret {:width 16 :height 16 :color "gray"})]]))

(defn buttons-list-molecule
  [{:list/keys [buttons]}]
  [:div.mt1
   (for [button buttons]
     (shopping-method-choice-button-molecule button))])

(defn shopping-method-choice-title-molecule
  [{:shopping-method-choice.title/keys [id primary secondary]}]
  [:div.left-align
   [:div.h1.my2.light primary]
   [:div.h5.my2.light secondary]])

(defn organism
  [data _ _]
  (component/create
   [:div.m5
    (shopping-method-choice-title-molecule data)
    (buttons-list-molecule data)]))