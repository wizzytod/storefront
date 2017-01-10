(ns storefront.routes
  (:require [bidi.bidi :as bidi]
            [clojure.walk :refer [keywordize-keys]]
            [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.platform.uri :as uri]
            [storefront.config :as config]
            #?(:cljs [cljs.reader :refer [read-string]])))

(defn edn->bidi [value]
  (keyword (prn-str value)))

(defn bidi->edn [value]
  (read-string (name value)))

(def static-page-routes
  {"/guarantee"      (edn->bidi events/navigate-content-guarantee)
   "/help"           (edn->bidi events/navigate-content-help)
   "/about-us"       (edn->bidi events/navigate-content-about-us)
   "/policy/privacy" (edn->bidi events/navigate-content-privacy)
   "/policy/tos"     (edn->bidi events/navigate-content-tos)})

(def static-api-routes
  ["/static" static-page-routes])

(def style-guide-routes
  {"/_style"                 (edn->bidi events/navigate-style-guide)
   "/_style/color"           (edn->bidi events/navigate-style-guide-color)
   "/_style/spacing"         (edn->bidi events/navigate-style-guide-spacing)
   "/_style/buttons"         (edn->bidi events/navigate-style-guide-buttons)
   "/_style/form-fields"     (edn->bidi events/navigate-style-guide-form-fields)
   "/_style/navigation"      (edn->bidi events/navigate-style-guide-navigation)
   "/_style/navigation/tab1" (edn->bidi events/navigate-style-guide-navigation-tab1)
   "/_style/navigation/tab3" (edn->bidi events/navigate-style-guide-navigation-tab3)
   "/_style/progress"        (edn->bidi events/navigate-style-guide-progress)
   "/_style/carousel"        (edn->bidi events/navigate-style-guide-carousel)})

(def app-routes
  ["" (merge static-page-routes
             style-guide-routes
             {"/"                                      (edn->bidi events/navigate-home)
              "/categories"                            (edn->bidi events/navigate-categories)
              ["/categories/hair/" :named-search-slug] (edn->bidi events/navigate-category)
              ["/products/" :product-slug]             (edn->bidi events/navigate-product)
              "/login"                                 (edn->bidi events/navigate-sign-in)
              "/logout"                                (edn->bidi events/navigate-sign-out)
              "/signup"                                (edn->bidi events/navigate-sign-up)
              "/password/recover"                      (edn->bidi events/navigate-forgot-password)
              ["/m/" :reset-token]                     (edn->bidi events/navigate-reset-password)
              ["/c/" :shared-cart-id]                  (edn->bidi events/navigate-shared-cart)
              "/account/edit"                          (edn->bidi events/navigate-account-manage)
              "/account/referrals"                     (edn->bidi events/navigate-account-referrals)
              "/cart"                                  (edn->bidi events/navigate-cart)
              "/shop/look"                             (edn->bidi events/navigate-shop-by-look)
              ["/shop/look/" :look-id]                 (edn->bidi events/navigate-shop-by-look-details)
              "/stylist/commissions"                   (edn->bidi events/navigate-stylist-dashboard-commissions)
              "/stylist/store_credits"                 (edn->bidi events/navigate-stylist-dashboard-bonus-credit)
              "/stylist/referrals"                     (edn->bidi events/navigate-stylist-dashboard-referrals)
              "/stylist/account/profile"               (edn->bidi events/navigate-stylist-account-profile)
              "/stylist/account/password"              (edn->bidi events/navigate-stylist-account-password)
              "/stylist/account/commission"            (edn->bidi events/navigate-stylist-account-commission)
              "/stylist/account/social"                (edn->bidi events/navigate-stylist-account-social)
              "/share"                                 (edn->bidi events/navigate-friend-referrals)
              "/checkout/returning_or_guest"           (edn->bidi events/navigate-checkout-returning-or-guest)
              "/checkout/login"                        (edn->bidi events/navigate-checkout-sign-in)
              "/checkout/address"                      (edn->bidi events/navigate-checkout-address)
              "/checkout/payment"                      (edn->bidi events/navigate-checkout-payment)
              "/checkout/confirm"                      (edn->bidi events/navigate-checkout-confirmation)
              ["/orders/" :number "/complete"]         (edn->bidi events/navigate-order-complete)})])

(defn path-for [navigation-event & [args]]
  (let [query-params (:query-params args)
        args         (dissoc args :query-params)
        path         (apply bidi/path-for
                            app-routes
                            (edn->bidi navigation-event)
                            (apply concat (seq args)))]
    (when path
      (uri/set-query-string path query-params))))

(defn current-path [app-state]
  (apply path-for (get-in app-state keypaths/navigation-message)))

(defn navigation-message-for
  ([uri] (navigation-message-for uri nil))
  ([uri query-params]
   (let [{nav-event :handler params :route-params} (bidi/match-route app-routes uri)]
     [(if nav-event (bidi->edn nav-event) events/navigate-not-found)
      (-> params
          (merge (when (seq query-params) {:query-params query-params}))
          keywordize-keys)])))

(defn current-page? [[current-event current-args] target-event & [args]]
  (and (= (take (count target-event) current-event)
          target-event)
       (every? #(= (%1 args) (%1 current-args)) (keys args))))
