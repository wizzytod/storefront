(ns storefront.components.slideout-nav
  (:require [storefront.components.utils :as utils]
            [om.core :as om]
            [sablono.core :refer-macros [html]]
            [storefront.events :as events]
            [storefront.keypaths :as keypaths]
            [storefront.routes :as routes]
            [storefront.accessors.taxons :refer [taxon-path-for default-nav-taxon-path default-stylist-taxon-path]]
            [storefront.messages :refer [send]]
            [storefront.hooks.experiments :as experiments]))

(defn close-all-menus [app-state]
  (send app-state
        events/control-menu-collapse
        {:keypath keypaths/menu-expanded})
  (send app-state
        events/control-menu-collapse
        {:keypath keypaths/account-menu-expanded})
  (send app-state
        events/control-menu-collapse
        {:keypath keypaths/shop-menu-expanded}))

(defn close-and-route [app-state event & [args]]
  {:href
   (routes/path-for @app-state event args)
   :on-click
   (fn [e]
     (.preventDefault e)
     (close-all-menus app-state)
     (routes/enqueue-navigate @app-state event args))})

(defn close-and-enqueue [app-state event & [args]]
  {:href "#"
   :on-click
   (fn [e]
     (.preventDefault e)
     (close-all-menus app-state)
     (send app-state event args))})

(defn slideout-nav-link [data {:keys [href on-click icon-class label full-width?]}]
  [:a.slideout-nav-link
   {:href href :on-click on-click :class (if full-width? "full-width" "half-width")}
   [:div.slideout-nav-link-inner
    [:div.slideout-nav-link-icon {:class (str "icon-" icon-class)}]
    label]])

(defn logged-in? [data]
  (boolean (get-in data keypaths/user-email)))

(defn own-store? [data]
  (= (get-in data keypaths/user-store-slug)
     (get-in data keypaths/store-slug)))

(defn slideout-nav-component [data owner]
  (om/component
   (html
    [:div.slideout-nav-wrapper
     {:class (when (get-in data keypaths/menu-expanded)
               "slideout-nav-open")}
     (let [store (get-in data keypaths/store)
           store-slug (get-in data keypaths/store-slug)]
       [:nav.slideout-nav (when-not (store :profile_picture_url)
                            {:class "no-picture"})
        [:div.slideout-nav-header
         [:div.slideout-nav-img-container
          (if-let [profile-picture-url (store :profile_picture_url)]
            [:img.slideout-nav-portrait {:src profile-picture-url}]
            [:div.slideout-nav-portrait.missing-picture])]
         [:h2.slideout-nav-title (store :store_name)]]
        [:div.horizontal-nav-list
         [:div.account-detail
          (if (logged-in? data)
            [:a.account-menu-link
             {:href "#"
              :on-click
              (if (get-in data keypaths/account-menu-expanded)
                (utils/send-event-callback data
                                           events/control-menu-collapse
                                           {:keypath keypaths/account-menu-expanded})
                (utils/send-event-callback data
                                           events/control-menu-expand
                                           {:keypath keypaths/account-menu-expanded}))}
             [:span.account-detail-name
              (when (own-store? data)
                [:span.stylist-user-label "Stylist:"])
              (get-in data keypaths/user-email)]
             [:figure.down-arrow]]
            [:span
             [:a (close-and-route data events/navigate-sign-in) "Sign In"]
             " | "
             [:a (close-and-route data events/navigate-sign-up) "Sign Up"]])]
         (when (logged-in? data)
           [:ul.account-detail-expanded
            {:class
             (if (get-in data keypaths/account-menu-expanded)
               "open"
               "closed")}
            (when (own-store? data)
              [:div
               [:li
                [:a (close-and-route data events/navigate-stylist-commissions) "Orders & Commissions"]]
               [:li
                [:a (close-and-route data events/navigate-stylist-bonus-credit) "Bonus Credit"]]
               [:li
                [:a (close-and-route data events/navigate-stylist-referrals) "Referrals"]]])
            [:li
             [:a (close-and-route data events/navigate-my-orders) "My Orders"]]
            [:li
             [:a
              (if (own-store? data)
                (close-and-route data events/navigate-stylist-manage-account)
                (close-and-route data events/navigate-manage-account))
              "Manage Account"]]
            [:li
             [:a (close-and-enqueue data events/control-sign-out)
              "Logout"]]])
         [:h2.horizontal-nav-title
          (store :store_name)
          [:ul.header-social-icons
           (when-let [instagram-account (store :instagram_account)]
             [:li.instagram-icon
              [:a.full-link {:href (str "http://instagram.com/" instagram-account) :target "_blank"}]])]]
         [:ul.horizontal-nav-menu
          [:li
           [:a
            (if (own-store? data)
              (close-and-enqueue data events/control-menu-expand
                                 {:keypath keypaths/shop-menu-expanded})
              (if (experiments/bundle-builder? data)
                (close-and-route data events/navigate-categories)
                (when-let [path (default-nav-taxon-path data)]
                  (close-and-route data events/navigate-category
                                   {:taxon-path path}))))
            (if (own-store? data)
              "Shop "
              "Shop")
            (when (own-store? data)
              [:figure.down-arrow])]]
          [:li [:a (close-and-route data events/navigate-guarantee) "30 Day Guarantee"]]
          [:li [:a (close-and-route data events/navigate-help) "Customer Service"]]]]
        (when (get-in data keypaths/shop-menu-expanded)
          [:ul.shop-menu-expanded.open
           [:li
            [:a
             (if (experiments/bundle-builder? data)
               (close-and-route data events/navigate-categories)
               (when-let [path (default-nav-taxon-path data)]
                 (close-and-route data events/navigate-category
                                  {:taxon-path path})))
             "Hair Extensions"]]
           [:li
            [:a
             (when-let [path (default-stylist-taxon-path data)]
               (close-and-route data events/navigate-category
                                {:taxon-path path}))
             "Stylist Only Products"]]])
        [:ul.slideout-nav-list
         (when (own-store? data)
           [:li.slideout-nav-section.stylist
            [:h3.slideout-nav-section-header.highlight "Manage Store"]
            (slideout-nav-link
             data
             (merge (close-and-route data events/navigate-stylist-commissions)
                    {:icon-class "commissions-and-payouts"
                     :label "Commissions & Payouts"
                     :full-width? false}))
            (slideout-nav-link
             data
             (merge (close-and-route data events/navigate-stylist-bonus-credit)
                    {:icon-class "sales-bonuses"
                     :label "Stylist Bonuses"
                     :full-width? false}))
            (slideout-nav-link
             data
             (merge (close-and-route data events/navigate-stylist-referrals)
                    {:icon-class "stylist-referrals"
                     :label "Stylist Referrals"
                     :full-width? false}))
            (slideout-nav-link
             data
             (merge (close-and-route data events/navigate-stylist-manage-account)
                    {:icon-class "edit-profile"
                     :label "Edit Profile"
                     :full-width? false}))])
         [:li.slideout-nav-section
          [:h3.slideout-nav-section-header "Shop"]
          (slideout-nav-link
           data
           (merge
            (if (experiments/bundle-builder? data)
              (close-and-route data events/navigate-categories)
              (when-let [path (default-nav-taxon-path data)]
                (close-and-route data events/navigate-category
                                 {:taxon-path path})))
            {:icon-class "hair-extensions"
             :label "Hair Extensions"
             :full-width? true}))
          (when (own-store? data)
            (slideout-nav-link
             data
             (merge
              (when-let [path (default-stylist-taxon-path data)]
                (close-and-route data events/navigate-category
                                 {:taxon-path path}))
              {:icon-class "stylist-products"
               :label "Stylist Products"
               :full-width? true})))]
         [:li.slideout-nav-section
          [:h3.slideout-nav-section-header "My Account"]
          (if (logged-in? data)
            [:div
             (slideout-nav-link
              data
              (merge
               (close-and-route data events/navigate-my-orders)
               {:icon-class "my-orders"
                :label "My Orders"
                :full-width? true}))
             (slideout-nav-link
              data
              (merge (if (own-store? data)
                       (close-and-route data events/navigate-stylist-manage-account)
                       (close-and-route data events/navigate-manage-account))
                     {:icon-class "manage-account"
                      :label "Manage Account"
                      :full-width? false}))
             (slideout-nav-link
              data
              (merge (close-and-enqueue data events/control-sign-out)
                     {:icon-class "logout"
                      :label "Logout"
                      :full-width? false}))]
            [:div
             (slideout-nav-link
              data
              (merge (close-and-route data events/navigate-sign-in)
                     {:icon-class "sign-in"
                      :label "Sign In"
                      :full-width? false}))
             (slideout-nav-link
              data
              (merge (close-and-route data events/navigate-sign-up)
                     {:icon-class "join"
                      :label "Join"
                      :full-width? false}))])]
         [:li.slideout-nav-section
          [:h3.slideout-nav-section-header "Help"]
          (slideout-nav-link
           data
           (merge (close-and-route data events/navigate-help)
                  {:icon-class "customer-service"
                   :label "Customer Service"
                   :full-width? false}))
          (slideout-nav-link
           data
           (merge (close-and-route data events/navigate-guarantee)
                  {:icon-class "30-day-guarantee"
                   :label "30 Day Guarantee"
                   :full-width? false}))]]])])))
