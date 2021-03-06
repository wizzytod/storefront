(ns storefront.request-keys)

(def get-promotions [:get-promotions])
(def get-products [:get-products])
(def get-skus [:get-skus])
(def get-states [:get-states])
(def get-saved-cards [:get-saved-cards])
(def get-static-content [:get-static-content])

(def sign-out [:sign-out])
(def sign-in [:sign-in])
(def one-time-sign-in [:one-time-sign-in])
(def sign-up [:sign-up])
(def facebook-sign-in [:facebook-sign-in])
(def reset-facebook [:reset-facebook])
(def forgot-password [:forgot-password])
(def reset-password [:reset-password])
(def get-account [:get-account])
(def update-account [:update-account])
(def update-account-profile [:update-account-profile])

(def get-stylist-account [:get-stylist-account])
(def refresh-stylist-portrait [:refresh-stylist-portrait])
(def update-stylist-account [:update-stylist-account])
(def update-stylist-account-portrait [:update-stylist-account-portrait])
(def append-gallery [:append-gallery])
(def get-stylist-balance-transfer [:get-stylist-balance-transfer])
(def get-stylist-payout-stats [:get-stylist-payout-stats])
(def get-stylist-dashboard-stats [:get-stylist-dashboard-stats])
(def get-stylist-dashboard-balance-transfers [:get-stylist-dashboard-balance-transfers])
(def get-stylist-dashboard-sales [:get-stylist-dashboard-sales])
(def get-stylist-dashboard-sale [:get-stylist-dashboard-sale])
(def get-stylist-referral-program [:get-stylist-referral-program])
(def cash-out-commit [:cash-out-commit])
(def cash-out-status [:cash-out-status])
(def get-store-gallery [:get-store-gallery])
(def get-stylist-gallery [:get-stylist-gallery])
(def delete-gallery-image [:delete-gallery-image])

(def get-shipping-methods [:shipping-methods])

(def update-cart [:update-cart])
(def update-line-item [:update-line-item])
(def delete-line-item [:delete-line-item])
(def remove-freeinstall-line-item [:remove-freeinstall-line-item])
(def update-order [:update-order])
(def update-addresses [:update-addresses])
(def update-shipping-method [:update-shipping-method])
(def update-cart-payments [:update-cart-payments])
(def add-promotion-code [:add-promo-code])
(def remove-promotion-code [:remove-promo-code])
(def get-order [:get-order])
(def add-to-bag [:add-to-bag])
(def place-order [:place-order])
(def checkout [:checkout])
(def send-referrals [:send-referrals])
(def create-shared-cart [:create-shared-cart])
(def fetch-shared-cart [:fetch-shared-cart])
(def create-order-from-shared-cart [:create-order-from-shared-cart])
(def assign-servicing-stylist [:assign-servicing-stylist])
(def remove-servicing-stylist [:remove-servicing-stylist])

(def stripe-create-token [:stripe-create-token])

(def fetch-cms-keypath [:fetch-cms-keypath])
(def voucher-redemption [:voucher-redemption])
(def fetch-user-stylist-service-menu [:fetch-user-stylist-service-menu])
(def fetch-stylists-within-radius [:fetch-stylists-within-radius])
(def fetch-stylists-matching-filters [:fetch-stylists-matching-filters])

(def fetch-matched-stylist [:fetch-matched-stylist])
(def fetch-matched-stylists [:fetch-matched-stylists])
(def fetch-stylist-reviews [:fetch-matched-stylist])
