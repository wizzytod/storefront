(ns storefront.keypaths)

(def event-ch [:event-ch])

(def history [:history])
(def cookie [:cookie])
(def routes [:routes])

(def session-id [:session-id])

(def user [:user])
(def user-email (conj user :email))
(def user-token (conj user :token))
(def user-store-slug (conj user :store-slug))
(def user-id (conj user :id))
(def user-order-token (conj user :order-token))
(def user-order-id (conj user :order-id))
(def user-total-available-store-credit (conj user :total-available-store-credit))

(def order [:order])
(def order-covered-by-store-credit [:order :covered_by_store_credit])
(def order-total-applicable-store-credit [:order :total_applicable_store_credit])

(def promotions [:promotions])

(def store [:store])
(def store-slug (conj store :store_slug))

(def taxons [:taxons])
(def products [:products])
(def states [:states])
(def sms-number [:sms-number])

(def ui [:ui])
(def navigation-event (conj ui :navigation-event))
(def browse-taxon-query (conj ui :browse-taxon-query))
(def browse-product-query (conj ui :browse-product-query))
(def browse-variant-query (conj ui :browse-variant-query))
(def browse-variant-quantity (conj ui :browse-variant-quantity))
(def browse-recently-added-variants (conj ui :browse-recently-added-variants))
(def menu-expanded (conj ui :menu-expanded))
(def account-menu-expanded (conj ui :account-menu-expanded))

(def checkout-selected-shipping-method-id (conj ui :checkout-selected-shipping-method-id))

(def sign-in (conj ui :sign-in))
(def sign-in-email (conj sign-in :email))
(def sign-in-password (conj sign-in :password))
(def sign-in-remember (conj sign-in :remember-me))

(def sign-up (conj ui :sign-up))
(def sign-up-email (conj sign-up :email))
(def sign-up-password (conj sign-up :password))
(def sign-up-password-confirmation (conj sign-up :password-confirmation))

(def forgot-password (conj ui :forgot-password))
(def forgot-password-email (conj forgot-password :email))

(def reset-password (conj ui :reset-password))
(def reset-password-password (conj reset-password :password))
(def reset-password-password-confirmation (conj reset-password :password-confirmation))
(def reset-password-token (conj reset-password :token))

(def manage-account (conj ui :manage-account))
(def manage-account-email (conj manage-account :email))
(def manage-account-password (conj manage-account :password))
(def manage-account-password-confirmation (conj manage-account :password-confirmation))

(def cart (conj ui :cart))
(def cart-quantities (conj cart :quantities))
(def cart-coupon-code (conj cart :coupon-code))

(def checkout (conj ui :checkout))
(def checkout-billing-address (conj checkout :billing-address))
(def checkout-billing-address-firstname (conj checkout-billing-address :firstname))
(def checkout-billing-address-lastname (conj checkout-billing-address :lastname))
(def checkout-billing-address-address1 (conj checkout-billing-address :address1))
(def checkout-billing-address-address2 (conj checkout-billing-address :address2))
(def checkout-billing-address-city (conj checkout-billing-address :city))
(def checkout-billing-address-state (conj checkout-billing-address :state_id))
(def checkout-billing-address-zip (conj checkout-billing-address :zipcode))
(def checkout-billing-address-phone (conj checkout-billing-address :phone))
(def checkout-billing-address-save-my-address (conj checkout-billing-address :save-my-address))
(def checkout-shipping-address (conj checkout :shipping-address))
(def checkout-shipping-address-firstname (conj checkout-shipping-address :firstname))
(def checkout-shipping-address-lastname (conj checkout-shipping-address :lastname))
(def checkout-shipping-address-address1 (conj checkout-shipping-address :address1))
(def checkout-shipping-address-address2 (conj checkout-shipping-address :address2))
(def checkout-shipping-address-city (conj checkout-shipping-address :city))
(def checkout-shipping-address-state (conj checkout-shipping-address :state_id))
(def checkout-shipping-address-zip (conj checkout-shipping-address :zipcode))
(def checkout-shipping-address-phone (conj checkout-shipping-address :phone))
(def checkout-shipping-address-use-billing-address (conj checkout-shipping-address :use-billing-address))
(def checkout-credit-card-name (conj checkout :credit-card-name))
(def checkout-credit-card-number (conj checkout :credit-card-number))
(def checkout-credit-card-expiration (conj checkout :credit-card-expiration))
(def checkout-credit-card-ccv (conj checkout :credit-card-ccv))

(def flash (conj ui :flash))
(def flash-success (conj flash :success))
(def flash-success-message (conj flash-success :message))
(def flash-success-nav (conj flash-success :navigation))

(def billing-address [:billing-address])

(def shipping-address [:shipping-address])

(def stylist [:stylist])

(def stylist-sales-rep-email (conj stylist :sales-rep-email))

(def stylist-commissions (conj stylist :commissions))
(def stylist-commissions-rate (conj stylist-commissions :rate))
(def stylist-commissions-next-amount (conj stylist-commissions :next-amount))
(def stylist-commissions-paid-total (conj stylist-commissions :paid-total))
(def stylist-commissions-new-orders (conj stylist-commissions :new-orders))
(def stylist-commissions-payouts (conj stylist-commissions :payouts))


(def stylist-bonus-credit (conj stylist :bonus-credits))
(def stylist-bonus-credit-bonus-amount (conj stylist-bonus-credit :bonus-amount))
(def stylist-bonus-credit-earning-amount (conj stylist-bonus-credit :earning-amount))
(def stylist-bonus-credit-commissioned-revenue (conj stylist-bonus-credit :commissioned-revenue))
(def stylist-bonus-credit-total-credit (conj stylist-bonus-credit :total-credit))
(def stylist-bonus-credit-available-credit (conj stylist-bonus-credit :available-credit))
(def stylist-bonus-credit-bonuses (conj stylist-bonus-credit :bonuses))

(def stylist-referral-program (conj stylist :referral-program))
(def stylist-referral-program-bonus-amount (conj stylist-referral-program :referral-program-bonus-amount))
(def stylist-referral-program-earning-amount (conj stylist-referral-program :referral-program-earning-amount))
(def stylist-referral-program-total-amount (conj stylist-referral-program :referral-program-total-amount))
(def stylist-referral-program-referrals (conj stylist-referral-program :referral-program-referrals))
