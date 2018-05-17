(ns storefront.components.footer-modal
  (:require #?(:clj [storefront.component-shim :as component]
               :cljs [storefront.component :as component])
            [storefront.components.ui :as ui]
            [storefront.keypaths :as keypaths]
            [storefront.components.footer-links :as footer-links]))

(defn component
  [{:keys [call-number]} owner opts]
  (component/create
   [:div.border-top.border-gray.bg-white
    [:div.container
     [:div.center.px3.my2
      [:div.my1.medium.dark-gray "Have Questions?"]
      [:div.dark-gray.light.h5
       [:span.hide-on-tb-dt
        (ui/link :link/phone :a.dark-gray {} call-number)]
       [:span.hide-on-mb
        (ui/link :link/phone :a.dark-gray {} call-number)]
       " | 8am-5pm PST M-F"]]]]))

(defn query
  [data]
  {:call-number "+1 (888) 562-7952"})

(defn built-component
  [data opts]
  (component/build component (query data) nil))
