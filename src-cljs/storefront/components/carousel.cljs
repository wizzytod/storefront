(ns storefront.components.carousel
  (:require [storefront.events :as events]
            [storefront.components.utils :as utils]
            [storefront.keypaths :as keypaths]
            [storefront.messages :as messages]
            [storefront.request-keys :as request-keys]
            [sablono.core :refer-macros [html]]
            [swipe :as swipe]
            [om.core :as om]
            [storefront.components.ui :as ui]))

(defn css-url [url] (str "url(" url ")"))

(defn carousel-component [data owner {:keys [index-path images-path]}]
  (om/component
   (html
    (let [idx (get-in data index-path)
          images (get-in data images-path [])]
      [:.carousel-component
       [:.hair-category-image {:style {:background-image (css-url (get images idx))}}]
       [:.left {:on-click
                (utils/send-event-callback events/control-carousel-move
                                           {:index-path index-path
                                            :index (mod (dec idx) (count images))})}]
       [:.right {:on-click
                 (utils/send-event-callback events/control-carousel-move
                                            {:index-path index-path
                                             :index (mod (inc idx) (count images))})}]]))))

(defn swipe-component [{:keys [selected-index items continuous]} owner {:keys [handler]}]
  (reify
    om/IDidMount
    (did-mount [this]
      (om/set-state!
       owner
       {:swiper (js/Swipe. (om/get-ref owner "items")
                           #js {:continuous (or continuous false)
                                :startSlide (or selected-index 0)
                                :callback (fn [idx _] (handler (get items idx)))})}))
    om/IWillUnmount
    (will-unmount [this]
      (when-let [swiper (:swiper (om/get-state owner))]
        (.kill swiper)))
    om/IRenderState
    (render-state [_ {:keys [swiper]}]
      (let [selected-idx selected-index]
        (when (and swiper selected-idx)
          (let [delta (- (.getPos swiper) selected-idx)]
            (if (pos? delta)
              (dotimes [_ delta] (.prev swiper))
              (dotimes [_ (- delta)] (.next swiper)))))
        (html
         [:.center
          [:.absolute.z2.to-md-hide.lg-col-8
           [:a.col.col-2.left-align.img-left-arrow.bg-no-repeat.bg-center.cursor.active-darken-3
            {:style {:height "31rem"
                     :background-size "30px"}
             :on-click (fn [_]
                         (handler (nth items (dec selected-idx) (last items))))}]
           [:.col.col-4 ui/nbsp]
           [:a.col.col-2.right-align.img-right-arrow.bg-no-repeat.bg-center.cursor.active-darken-3
            {:style {:height "31rem"
                     :background-size "30px"}
             :on-click (fn [_]
                         (handler (nth items (inc selected-idx) (first items))))}]]
          [:.overflow-hidden.relative.invisible
           {:ref "items"}
           [:.overflow-hidden.relative
            (for [item items]
              [:.left.col-12.relative {:key (:id item)}
               (:body item)])]]])))))
