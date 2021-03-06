(ns adventure.organisms.call-out-center
  (:require [clojure.spec.alpha :as s]
            [storefront.component :as component :refer [defcomponent]]
            [storefront.components.ui :as ui]
            [storefront.platform.component-utils :as utils]))

(defn ^:private cta-molecule
  [{:cta/keys [id label target]}]
  (let [[event] target]
    (when (and id label target)
      (-> (merge {:data-test id}
                 (if (= :navigate (first event))
                   (apply utils/route-to target)
                   (apply utils/fake-href target)))
          (ui/button-large-primary [:div.flex.items-center.justify-center.inherit-color label])))))

(defcomponent organism
  [{:call-out-center/keys [bg-class bg-ucare-id title subtitle] react-key :react/key :as query} _ _]
  [:div.p8
   (merge
    (when react-key {:key react-key})
    (when bg-class  {:class bg-class}))
   [:div.center.col-12
    (when title
      [:div.h1.canela.title-1 title])
    (when subtitle
      [:div.p3.proxima.content-2 subtitle])]
   [:div.col-10-on-tb-dt.mx-auto
    (cta-molecule query)]])

(comment

  (s/def :react/key string?)
  (s/def :atom/react (s/keys :req [:react/key]))

  (s/def :cta/data-test string?)
  (s/def :cta/target vector?)
  (s/def :cta/label string?)
  (s/def :molecule/cta
    (s/keys :req [:cta/id :cta/target :cta/label]))

  (s/def :call-out-center/bg-class string?)
  (s/def :call-out-center/bg-ucare-id string?)
  (s/def :call-out-center/title string?)
  (s/def :call-out-center/subtitle string?)
  (s/def :organism/call-out-center
    (s/keys :req [:call-out-center/bg-class
                  :call-out-center/bg-ucare-id
                  :call-out-center/title
                  :call-out-center/subtitle]))

  (s/fdef cta-molecule
    :args (s/cat :query :molecule/cta)
    :ret any?)

  (s/fdef organism
    :args (s/cat :query (s/and :organism/call-out-center
                               :molecule/cta
                               :atom/react)
                 :owner any?
                 :opts any?)
    :ret vector?))


