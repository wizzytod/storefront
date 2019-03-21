(ns checkout.templates.item-card
  (:require [storefront.component :as component]
            [storefront.components.ui :as ui]
            [storefront.css-transitions :as css-transitions]))

(defn component
  [{:keys [items]} _ _]
  (component/create
   [:div
    (for [{:as       item
           react-key :react/key} items]
      [:div.pt1.pb2.flex.items-center.col-12 {:key react-key}

       ;; image group
       [:div.mt2.mr2.relative.self-start

        ;; circle over image
        (let [{:circle/keys [id value highlight?]} item]
          (when id
            (css-transitions/transition-background-color
             highlight?
             [:div.medium.h5
              [:div.absolute.z1.circle.stacking-context.border.border-light-gray.bg-too-light-teal.flex.items-center.justify-center
               {:key       id
                :data-test id
                :style     {:right  "-5px"
                            :top    "-12px"
                            :width  "32px"
                            :height "32px"}}
               value]])))

        ;; actual image
        (let [{:image/keys [id value highlight?]} item]
          (when id
            (css-transitions/transition-background-color
             highlight?
             [:div.flex.items-center.justify-center.ml1
              {:key       id
               :data-test id
               :style     {:width "79px" :height "74px"}}
              [:div.pp1
               (ui/ucare-img {:width 75} value)]])))]

       ;; info group
       [:div.h6.flex.flex-wrap.flex-auto.items-center.justify-between.mt1

        ;; title
        (let [{:title/keys [id value]} item]
          [:div.col-12
           (when id
             [:a.medium.titleize.h5 {:data-test id} value])])

        ;; detail top-left
        (let [{:detail-top-left/keys [id value]} item]
          [:div.col-10
           (when id {:data-test id})
           (when id value)])

        ;; action top-right
        (let [{:detail-top-right/keys [id value]} item]
          [:div.col-2.right-align
           (when id
             [:div
              #_(if spinning?
                [:div.h3 {:style {:width "1.2em"}} ui/spinner]
                [:a.medium
                 (merge {:data-test id}
                        (apply utils/fake-href event))
                 value])])])

        ;; detail bottom-left
        (let [{:detail-bottom-left/keys [id value]} item]
          [:div.col-6
           (when id {:data-test id})
           (when id value)])

        ;; detail bottom-right
        (let [{:detail-bottom-right/keys [id value]} item]
          [:div.col-6.right-align
           (when id {:data-test id})
           (when id value)])]])]))