(ns storefront.hooks.quadpay
  (:require [storefront.browser.tags :as tags]
            [storefront.browser.events :as browser.events]
            [storefront.component :as component]
            [storefront.platform.messages :refer [handle-message]]
            [storefront.events :as events]
            [sablono.core :refer [html]]
            [om.core :as om]))

(def uri "https://widgets.quadpay.com/mayvenn/quadpay-widget-2.2.1.js")

(defn insert []
  (when-not (pos? (.-length (.querySelectorAll js/document ".quadpay-tag")))
    (let [tag (tags/src-tag uri "quadpay-tag")
          cb #(handle-message events/inserted-quadpay)]
      (tags/insert-tag-with-callback tag cb))))

(defn show-modal
  "Requires component to be on the page"
  []
  (when-let [quadpay-widget (js/document.querySelector "quadpay-widget")]
    (.displayModal quadpay-widget)))

(defn hide-modal
  "Requires component to be on the page"
  []
  (when-let [quadpay-widget (js/document.querySelector "quadpay-widget")]
    (.hideModal quadpay-widget)))

(defn calc-installment-amount [full-amount]
  (.toFixed (/ full-amount 4) 2))

(defn component
  [{:keys [full-amount]} owner opts]
  (reify
    om/IDidMount
    (did-mount [_] (browser.events/invoke-late-ready-state-listeners))
    om/IRender
    (render [_]
      (html
       [:quadpay-widget {:amount full-amount}]))))