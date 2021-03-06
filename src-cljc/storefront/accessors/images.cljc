(ns storefront.accessors.images)

(defn skuer->image [use-case skuer]
  (->> skuer
       :selector/images
       (filter #(= (:use-case %) use-case))
       first))

(defn image-by-use-case [use-case skuer]
  ;;TODO fix this!!! PLEASE!!! (should be using selector and doing something more clever than this.)
  (when-let [image (->> skuer
                   :selector/images
                   (filter #(= (:use-case %) use-case))
                   first)]
    {:src (str (:url image) "-/format/auto/")
     :alt (:copy/title skuer)}))

(defn cart-image [skuer]
  (image-by-use-case "cart" skuer))
