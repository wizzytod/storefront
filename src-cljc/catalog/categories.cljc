(ns catalog.categories
  (:require [clojure.string :as string]
            [storefront.keypaths :as keypaths]
            [catalog.keypaths]
            [storefront.events :as events]))

(def new-facet?
  ;; [<facet-slug> <option-slug>]
  #{[:hair/family "ready-wigs"]})

(def new-category? #{})

(defn copy [& sentences]
  (string/join " " sentences))

(defn category->seo [category-name description image-url]
  {:page/title            (str category-name " | Mayvenn")
   :opengraph/title       (str category-name " - Free shipping. Free 30 day returns. Made with 100% virgin human hair.")
   :page.meta/description description
   :opengraph/description (copy "100% virgin human hair, machine-wefted and backed by our 30 Day Quality Guarantee, our"
                                category-name
                                "are the best quality products on the market and ships free!")
   ;; TODO make this a proper image and place under :selector/images
   :category/image-url    image-url
   :seo/sitemap           true})

(def clip-in-tape-in-templates
  {:page/title-template "%sVirgin %s| Mayvenn"
   :page.meta/description-template
   (copy "Get the hair of your dreams with our %s%s. Featuring a thin, polyurethane"
         "weft that flawlessly blends with your own hair.")})

(def closures-templates
  {:page/title-template "%s%s| Mayvenn"
   :page.meta/description-template
   (copy "Mayvenn's %s%sare beautifully crafted and provide a realistic part to"
         "close off any unit or install.")})

(def frontals-templates
  {:page/title-template "%s%s| Mayvenn"
   :page.meta/description-template
   (copy "Mayvenn's %s%smimic a natural hairline and offer versatile parting options"
         "to achieve your desired look.")})

(def texture-templates
  {:page/title-template "%s%s| Mayvenn"
   :page.meta/description-template
   (copy "Mayvenn's %shuman %sare machine-wefted and made with virgin"
         "hair for unbeatable quality. Shop to achieve your desired look!")})

(def closures
  [(merge {:catalog/category-id      "0"
           :copy/title               "Virgin Closures"
           :page/slug                "virgin-closures"
           :seo/title                "Virgin Hair Closures"
           :legacy/named-search-slug "closures"
           :catalog/department       #{"hair"}
           :hair/family              #{"closures"}
           :hair/color.process       #{"natural" "dyed"}
           :hair/source              #{"virgin"}
           :selector/essentials      [:catalog/department :hair/family :hair/color.process :hair/source]
           :selector/electives       [:hair/origin :hair/texture :hair/color :hair/base-material]
           :header/order             0
           :header/group             1
           :footer/order             10
           :copy/description         (copy "Save your precious strands and top your look off with the ultimate tool in protective weave styling."
                                           "Our collection of closures and frontals blend seamlessly with our bundles"
                                           "and can be customized to fit your unique look.")
           :images                   {:hero {:filename    "VirginClosures.jpg",
                                             :desktop-url "//ucarecdn.com/205aa178-866d-4cfd-a122-e16a28cd7314/",
                                             :mobile-url  "//ucarecdn.com/dbae573e-9757-46cb-b42b-8a8159f9e12c/",
                                             :alt         "Virgin Closures"}}}
          (category->seo "Closures"
                         (copy "Mayvenn’s virgin hair closures allow you to close off"
                               "any unit or install and come in a variety of different"
                               "combinations. Shop now to create your look.")
                         "//ucarecdn.com/12e8ebfe-06cd-411a-a6fb-909041723333/")
          {:page/title "Virgin Hair Closures: Human Hair Closures | Mayvenn"}
          closures-templates)
   (merge {:catalog/category-id      "10"
           :copy/title               "Virgin 360 Frontals"
           :page/slug                "virgin-360-frontals"
           :seo/title                "Virgin 360 Hair Frontals"
           :legacy/named-search-slug "360-frontals"

           :catalog/department  #{"hair"}
           :hair/family         #{"360-frontals"}
           :hair/color.process  #{"natural" "dyed"}
           :hair/source         #{"virgin"}
           :selector/essentials [:catalog/department :hair/family :hair/color.process :hair/source]
           :selector/electives  [:hair/origin :hair/texture :hair/color :hair/base-material]

           :copy/description (copy "From your hairline to nape, we’ve got you covered with our revolutionary 360 Lace Frontal."
                                   "This one-of-a-kind frontal piece features freestyle parting, baby hairs,"
                                   "and low-density edges for a naturally flawless look.")
           :images           {:hero {:filename    "360Frontals.jpg"
                                     :desktop-url "//ucarecdn.com/70ea2632-2710-49a7-a53f-4f34213c2fb6/"
                                     :mobile-url  "//ucarecdn.com/2a4e32c9-91fe-4a1e-8a84-d10c62f52819/"
                                     :alt         "360 Frontals"}}
           :footer/order     11
           :dtc-footer/order 5
           :header/order     1
           :header/group     1}
          (category->seo "360 Frontals"
                         (copy "Mayvenn’s virgin hair 360 frontals got you covered and come in"
                               "different variations such as Brazilian, Malaysian, straight, "
                               "and deep wave. Order today.")
                         "//ucarecdn.com/7837332a-2ca5-40dd-aa0e-86a2417cd723/")
          {:page/title "360 Lace Frontals: Virgin Hair 360 Frontals | Mayvenn"}
          frontals-templates)
   (merge {:catalog/category-id      "1"
           :copy/title               "Virgin Frontals"
           :page/slug                "virgin-frontals"
           :seo/title                "Virgin Hair Frontals"
           :legacy/named-search-slug "frontals"

           :catalog/department  #{"hair"}
           :hair/family         #{"frontals"}
           :hair/color.process  #{"natural" "dyed"}
           :header/order        2
           :header/group        1
           :footer/order        12
           :dtc-footer/order    6
           :hair/source         #{"virgin"}
           :selector/essentials [:catalog/department :hair/family :hair/color.process :hair/source]
           :selector/electives  [:hair/origin :hair/texture :hair/color :hair/base-material]
           :copy/description    (copy "Save your precious strands and top your look off with the ultimate tool in protective weave styling."
                                      "Our collection of frontals blend seamlessly with our bundles and can be customized to fit your unique look.")
           :images              {:hero {:filename    "VirginFrontals.jpg",
                                        :desktop-url "//ucarecdn.com/753b14a4-24ac-468c-b6f6-6aad72de443d/",
                                        :mobile-url  "//ucarecdn.com/158461f7-e264-4d58-974a-59abf5b18c3a/",
                                        :alt         "Virgin Frontals"}}}
          (category->seo "Frontals"
                         (copy "Mayvenn’s virgin hair frontals blend in seamlessly"
                               "with our bundles and come in a variety of different"
                               "combinations. Shop now to create your look.")
                         "//ucarecdn.com/0c7d94c3-c00e-4812-9526-7bd669ac679c/")
          {:page/title "Virgin Hair Frontals: Virgin Hair Lace Frontals | Mayvenn"}
          frontals-templates)])

(def virgin-hair
  [(merge {:catalog/category-id      "2"
           :copy/title               "Virgin Straight"
           :seo/title                "Virgin Straight Hair Extensions"
           :page/slug                "virgin-straight"
           :legacy/named-search-slug "straight"

           :catalog/department  #{"hair"}
           :hair/texture        #{"straight"}
           :hair/color.process  #{"natural" "dyed"}
           :hair/source         #{"virgin"}
           :hair/family         #{"bundles" "closures" "frontals" "360-frontals"}
           :selector/essentials [:catalog/department :hair/texture :hair/color.process :hair/source]
           :selector/electives  [:hair/family :hair/origin :hair/color :hair/base-material]
           :copy/description    (copy "For those who want it given to them"
                                      "straight, our collection of 100% virgin straight hair"
                                      "is your go-to for a sleek look with"
                                      "minimal effort.")
           :images              {:hero {:filename    "VirginStraight.jpg",
                                        :desktop-url "//ucarecdn.com/371af310-9100-433e-9e49-4abd8308d262/",
                                        :mobile-url  "//ucarecdn.com/409a74b6-1f12-4a9d-b024-672a4d0e1694/",
                                        :alt         "Virgin Straight"}
                                 :home {:filename "StraightExtensionsMayvenn.jpg",
                                        :url      "//ucarecdn.com/f4addde0-3c0e-40f8-85b0-fe2e2e96a7b5/",
                                        :alt      "Straight Hair Extensions Mayvenn"}
                                 :icon {:url "/images/categories/straight-icon.svg"}}
           :home/order          0
           :footer/order        0
           :dtc-footer/order    0
           :header/order        0
           :header/group        0}
          (category->seo "Natural Straight Extensions"
                         (copy "Straight Brazilian weave, straight Indian hair and straight Peruvian hair."
                               "Our straight bundles are sleek from root to tip.")
                         "//ucarecdn.com/61662cc7-59f5-454b-8031-538516557eb0/")
          texture-templates)
   (merge {:catalog/category-id      "3"
           :copy/title               "Virgin Yaki Straight"
           :seo/title                "Virgin Yaki Straight Hair Extensions"
           :page/slug                "virgin-yaki-straight"
           :legacy/named-search-slug "yaki-straight"

           :catalog/department  #{"hair"}
           :hair/texture        #{"yaki-straight"}
           :hair/color.process  #{"natural" "dyed"}
           :hair/source         #{"virgin"}
           :hair/family         #{"bundles" "closures" "frontals" "360-frontals"}
           :selector/essentials [:catalog/department :hair/texture :hair/color.process :hair/source]
           :selector/electives  [:hair/family :hair/origin :hair/color :hair/base-material]
           :copy/description    (copy "Tired of having to break out the hot"
                                      "tools for a textured straight look? Our Yaki"
                                      "Straight hair collection is here to save your"
                                      "strands! Yaki Straight hair matches the rhythm of"
                                      "your natural hair that's been pressed straight or"
                                      "freshly relaxed. Your flat iron has been officially"
                                      "cancelled.")
           :images              {:hero {:filename    "VirginYakiStraight.jpg",
                                        :desktop-url "//ucarecdn.com/303850ef-a62a-49d9-bc38-e7e823133efa/",
                                        :mobile-url  "//ucarecdn.com/2ed05d29-6f8f-4378-8d24-cab16f1e8a0e/",
                                        :alt         "Virgin Yaki Straight"}
                                 :home {:filename "YakiStraightExtensionsMayvenn.jpg",
                                        :url      "//ucarecdn.com/d4b4aa87-fd32-4ff3-b60a-fd1118beab05/",
                                        :alt      "Yaki Straight Hair Extensions Mayvenn"}
                                 :icon {:url "/images/categories/yaki-straight-icon.svg"}}
           :home/order          4
           :footer/order        1
           :header/order        1
           :header/group        0}
          (category->seo "Yaki Straight Extensions"
                         (copy "Our Yaki Straight hair collection features both Peruvian and Brazilian straight hair bundles."
                               "With Lace Closure or Lace Frontals in different lengths.")
                         "//ucarecdn.com/98e8b217-73ee-475a-8f5e-2c3aaa56af42/")
          texture-templates)
   (merge {:catalog/category-id      "4"
           :copy/title               "Virgin Kinky Straight"
           :seo/title                "Virgin Kinky Straight Hair Extensions"
           :page/slug                "virgin-kinky-straight"
           :legacy/named-search-slug "kinky-straight"
           :catalog/department       #{"hair"}
           :hair/texture             #{"kinky-straight"}
           :hair/color.process       #{"natural" "dyed"}
           :hair/source              #{"virgin"}
           :hair/family              #{"bundles" "closures" "frontals" "360-frontals"}
           :selector/essentials      [:catalog/department :hair/texture :hair/color.process :hair/source]
           :selector/electives       [:hair/family :hair/origin :hair/color :hair/base-material]
           :copy/description         (copy "Blending is a breeze with our Kinky Straight"
                                           "hair collection! Like a fresh blow out, the"
                                           "Kinky Straight hair texture moves freely and gives"
                                           "a naturally flawless look that mimics your own"
                                           "locks.")
           :images                   {:hero {:filename    "VirginKinkyStraight.jpg",
                                             :desktop-url "//ucarecdn.com/92530f8d-0ff0-4705-bfd9-94b75fbbacca/",
                                             :mobile-url  "//ucarecdn.com/8c996c90-9db1-4a45-b6bf-1c242b708583/",
                                             :alt         "Virgin Kinky Straight"}
                                      :icon {:url "/images/categories/kinky-straight-icon.svg"}}
           :footer/order             2
           :header/order             2
           :header/group             0}
          (category->seo "Kinky Straight Extensions"
                         (copy "100% human hair bundles and extensions from Mayvenn."
                               "Peruvian and Brazilian Kinky Straight Lace Closures and Frontals.")
                         "//ucarecdn.com/7fe5f90f-4dad-454a-aa4b-b453fc4da3c4/")
          texture-templates)
   (merge {:catalog/category-id      "5"
           :copy/title               "Virgin Body Wave"
           :seo/title                "Virgin Body Wave Hair Extensions"
           :page/slug                "virgin-body-wave"
           :legacy/named-search-slug "body-wave"
           :catalog/department       #{"hair"}
           :hair/texture             #{"body-wave"}
           :hair/color.process       #{"natural" "dyed"}
           :hair/source              #{"virgin"}
           :hair/family              #{"bundles" "closures" "frontals" "360-frontals"}
           :selector/essentials      [:catalog/department :hair/texture :hair/color.process :hair/source]
           :selector/electives       [:hair/family :hair/origin :hair/color :hair/base-material]
           :copy/description         (copy "Step into the spotlight with our collection of luscious Body Wave hair."
                                           "Body Wave is unbelievably soft and goes from straight to wavy and back again with ease.")
           :images                   {:hero {:filename    "VirginBodyWave.jpg",
                                             :desktop-url "//ucarecdn.com/6ed67381-3dae-41bb-b684-efbcbc05b8bf/",
                                             :mobile-url  "//ucarecdn.com/550b0231-1282-42a8-afcd-caed588e5a61/",
                                             :alt         "Virgin Body Wave"}
                                      :home {:filename "BodyWaveExtensionsMayvenn.jpg",
                                             :url      "//ucarecdn.com/cbf5424f-3bab-4c6b-9fd1-328e9d94e564/",
                                             :alt      "Body Wave Hair Extensions Mayvenn"}
                                      :icon {:url "/images/categories/body-wave-icon.svg"}}
           :home/order               1
           :footer/order             3
           :dtc-footer/order         1
           :header/order             3
           :header/group             0}
          (category->seo "Body Wave Extensions"
                         (copy "Malaysian and Peruvian body wave silk, lace and 360 frontal bundles."
                               "Unbelievably soft and goes from straight to wavy and back again.")
                         "//ucarecdn.com/445c53df-f369-4ca6-a554-c9668c8968f1/")
          texture-templates)
   (merge {:catalog/category-id      "6"
           :copy/title               "Virgin Loose Wave"
           :seo/title                "Virgin Loose Wave Hair Extensions"
           :page/slug                "virgin-loose-wave"
           :legacy/named-search-slug "loose-wave"
           :catalog/department       #{"hair"}
           :hair/texture             #{"loose-wave"}
           :hair/color.process       #{"natural" "dyed"}
           :hair/source              #{"virgin"}
           :hair/family              #{"bundles" "closures" "frontals" "360-frontals"}
           :selector/essentials      [:catalog/department :hair/texture :hair/color.process :hair/source]
           :selector/electives       [:hair/family :hair/origin :hair/color :hair/base-material]
           :copy/description         (copy "For hair that holds a curl beautifully,"
                                           "our collection of 100% virgin Loose Wave hair"
                                           "is the perfect foundation for all your carefree,"
                                           "flirty, wavy looks.")
           :images                   {:hero {:filename    "VirginLooseWave.jpg",
                                             :desktop-url "//ucarecdn.com/6c8a823d-c0bf-4768-bc69-cb7820d85e8c/",
                                             :mobile-url  "//ucarecdn.com/56a81f02-ec0d-4b5d-8518-8cf6929fa502/",
                                             :alt         "Virgin Loose Wave"}
                                      :home {:filename "LooseWaveExtensionsMayvenn.jpg",
                                             :url      "//ucarecdn.com/c935b035-3b7d-4262-a750-a5fa3b559721/",
                                             :alt      "Loose Wave Hair Extensions Mayvenn"}
                                      :icon {:url "/images/categories/loose-wave-icon.svg"}}
           :home/order               2
           :footer/order             4
           :dtc-footer/order         2
           :header/order             4
           :header/group             0}
          (category->seo "Loose Wave Extensions"
                         (copy "Mayvenn’s Brazilian, Peruvian and Indian loose wave bundles."
                               "Also includes loose wave lace closures. All are 100% virgin Loose Wave hair.")
                         "//ucarecdn.com/31be9341-a688-4f03-b754-a22a0a1f267e/")
          texture-templates)
   (merge {:catalog/category-id      "7"
           :copy/title               "Virgin Water Wave"
           :seo/title                "Virgin Water Wave Hair Extensions"
           :page/slug                "Virgin-water-wave"
           :legacy/named-search-slug "water-wave"
           :catalog/department       #{"hair"}
           :hair/texture             #{"water-wave"}
           :hair/color.process       #{"natural" "dyed"}
           :hair/source              #{"virgin"}
           :hair/family              #{"bundles" "closures" "frontals" "360-frontals"}
           :selector/essentials      [:catalog/department :hair/texture :hair/color.process :hair/source]
           :selector/electives       [:hair/family :hair/origin :hair/color :hair/base-material]
           :copy/description         (copy "Ride the lush, carefree waves of the bundles,"
                                           "closures, and frontals in our Water Wave hair"
                                           "collection. For curls you can rock everywhere from"
                                           "the office to your tropical vacation, make a"
                                           "statement with Water Wave hair.")
           :images                   {:hero {:filename    "VirginWaterWave.jpg",
                                             :desktop-url "//ucarecdn.com/8600cff0-4402-43f7-9cff-8314ce0bb197/",
                                             :mobile-url  "//ucarecdn.com/5a015964-8ca9-4d65-80ec-ab0703cc5d97/",
                                             :alt         "Virgin Water Wave"}
                                      :icon {:url "/images/categories/water-wave-icon.svg"}}
           :footer/order             5
           :header/order             5
           :header/group             0}
          (category->seo "Water Wave Extensions"
                         (copy "Water Wave Bundles, Closures, and Frontals."
                               "Peruvian and Brazilian bundles."
                               "Mayvenn has hair extensions, bundles, closures, and frontals.")
                         "//ucarecdn.com/5f6c669f-8274-4bef-afa9-3c08813842f6/")
          texture-templates)
   (merge {:catalog/category-id      "8"
           :copy/title               "Virgin Deep Wave"
           :seo/title                "Virgin Deep Wave Hair Extensions"
           :page/slug                "virgin-deep-wave"
           :legacy/named-search-slug "deep-wave"
           :catalog/department       #{"hair"}
           :hair/texture             #{"deep-wave"}
           :hair/color.process       #{"natural" "dyed"}
           :hair/source              #{"virgin"}
           :hair/family              #{"bundles" "closures" "frontals" "360-frontals"}
           :selector/essentials      [:catalog/department :hair/texture :hair/color.process :hair/source]
           :selector/electives       [:hair/family :hair/origin :hair/color :hair/base-material]
           :copy/description         (copy "Reigning supreme in versatility, the Deep Wave hair collection features"
                                           "a soft, spiral wave full of body and bounce. Our deep wave hair is perfect"
                                           "for those who want big waves that make an even bigger splash.")
           :images                   {:hero {:filename    "DeepWave.jpg",
                                             :desktop-url "//ucarecdn.com/af14c56c-a304-4a82-8491-1bdc777b9b03/",
                                             :mobile-url  "//ucarecdn.com/69e2acad-0140-4b06-9e8f-ac48f6416076/",
                                             :alt         "Virgin Deep Wave"}
                                      :home {:filename "DeepWaveExtensionsMayvenn.jpg",
                                             :url      "//ucarecdn.com/2574ca60-0336-4c42-9087-159740bafdd2/",
                                             :alt      "Deep Wave Hair Extensions Mayvenn"}
                                      :icon {:url "/images/categories/deep-wave-icon.svg"}}
           :home/order               3
           :footer/order             6
           :dtc-footer/order         3
           :header/order             6
           :header/group             0}
          (category->seo "Deep Wave Extensions"
                         (copy "Deep Wave bundles and closures, including Brazilian, Peruvian and Indian Deep Wave."
                               "Soft, spiral wave full of body and bounce.")
                         "//ucarecdn.com/49cc5837-8321-4331-9cec-d299d0de1887/")
          texture-templates)
   (merge {:catalog/category-id      "9"
           :copy/title               "Virgin Curly"
           :seo/title                "Virgin Curly Hair Extensions"
           :page/slug                "virgin-curly"
           :legacy/named-search-slug "curly"
           :catalog/department       #{"hair"}
           :hair/texture             #{"curly"}
           :hair/color.process       #{"natural" "dyed"}
           :hair/source              #{"virgin"}
           :hair/family              #{"bundles" "closures" "frontals" "360-frontals"}
           :selector/essentials      [:catalog/department :hair/texture :hair/color.process :hair/source]
           :selector/electives       [:hair/family :hair/origin :hair/color :hair/base-material]
           :copy/description         (copy "Let your bold, beautiful curls take center stage! Our curly hair collection is a tight,"
                                           "kinky curl perfect for creating voluminous coily styles that mimic natural 3C hair textures.")
           :images                   {:hero {:filename    "VirginCurly.jpg",
                                             :desktop-url "//ucarecdn.com/e6cdfaeb-b98b-4b98-878e-fc2b2936995d/",
                                             :mobile-url  "//ucarecdn.com/17c827ca-e4df-4427-bdf8-c3426ff8d750/",
                                             :alt         "Virgin Curly"}
                                      :icon {:url "/images/categories/curly-icon.svg"}}
           :footer/order             7
           :header/order             7
           :header/group             0}
          (category->seo "Curly Extensions"
                         (copy "Shop our Brazilian curly bundle, Peruvian Curly Bundle,"
                               "Peruvian Curly Lace closures and Curly Lace frontals."
                               "Perfect for creating voluminous coily styles.")
                         "//ucarecdn.com/128b68e2-bf3a-4d72-8e39-0c71662f9c86/")
          texture-templates)])

(def category-id->subsection-fn
  {"13" (comp first :hair/family)
   "23" (comp first :hair/texture)})

(def wigs
  [(merge {:catalog/category-id "13"
           :footer/order        20
           :dtc-footer/order    7
           :header/order        0
           :header/group        2

           :page/icp?     true
           :category/new? true

           :copy/title "Wigs"
           :page/slug  "wigs"
           :seo/title  "Wigs"

           :copy/description (copy "These units will be your go-to protective style"
                                   "for achieving a brand new look."
                                   "With options ranging from 360 to Ready to Wear,"
                                   "there’s a wig available for each of your alter egos.")

           :images                         {:hero {:filename    "Wigs.jpg"
                                                   :desktop-url "//ucarecdn.com/c17d9942-4cec-4f32-814c-ed5141efef59/"
                                                   :mobile-url  "//ucarecdn.com/27fac423-2571-4dee-90fa-e98071051fea/"
                                                   :alt         "Wigs"}}
           :catalog/department             #{"hair"}
           :hair/family                    #{"360-wigs" "lace-front-wigs" "ready-wigs"}
           :selector/essentials            [:hair/family :catalog/department]
           :selector/electives             [:hair/family :hair/texture :hair/origin :hair/color]
           :page/title                     "Human Hair Wigs: Natural Hair Lace Wigs | Mayvenn"
           :page.meta/description          (copy "Mayvenn’s virgin human hair wigs allow you to achieve a new look in minutes"
                                                 "& come in different variations such as Brazilian, Malaysian, straight,"
                                                 "& deep wave.")
           :page/title-template            "%s%s| Mayvenn"
           :page.meta/description-template (copy "Mayvenn's %shuman %sare allow you to change up and"
                                                 "achieve your desired look. Shop our collection of"
                                                 "virgin hair wigs today.")
           :opengraph/title                (copy "Mayvenn 360 and Lace Frontal Wigs - Free shipping."
                                                 "Free 30 day returns. Made with 100% virgin human hair.")
           :opengraph/description          (copy "100% virgin human hair, machine-wefted and backed by our"
                                                 "30 Day Quality Guarantee, our Wigs can be customized to fit"
                                                 "your unique look using the built-in combs and adjustable strap.")
           :image-url                      "//ucarecdn.com/5269d7ef-b5c7-4475-9e9c-d16d7cbbdda3/"
           :subsections                    {"360-wigs"        {:order         2
                                                               :title/primary "360 Wigs"}
                                            "lace-front-wigs" {:order         1
                                                               :title/primary "Lace Front Wigs"}
                                            "ready-wigs"      {:order         0
                                                               :title/primary "Ready to Wear Wigs"}}})
   (merge {:catalog/category-id "24"
           :category/new?       true

           :copy/title "Virgin Lace Front Wigs"
           :page/slug  "virgin-lace-front-wigs"
           :seo/title  "Virgin Lace Front Wigs"

           :copy/description      (copy "With the lace base in front only,"
                                        "these are ideal for exploring new ways to part your hair."
                                        "Ours are made with virgin lace & real human hair.")
           :subcategory/image-uri "//ucarecdn.com/71dcdd17-f9cc-456f-b763-2c1c047c30b4/-/format/auto/-/resize/124x/"

           :catalog/department  #{"hair"}
           :hair/family         #{"lace-front-wigs"}
           :selector/essentials [:hair/family :catalog/department]
           :selector/electives  [:hair/texture :hair/origin]

           :opengraph/description (copy "100% virgin human hair, machine-wefted and backed by our"
                                        "30 Day Quality Guarantee, our Wigs can be customized to fit"
                                        "your unique look using the built-in combs and adjustable strap.")}
          (category->seo "Lace Front Wigs: Human Hair Lace Front Wigs"
                         (copy "Mayvenn’s human hair lace front wigs mimic a natural hairline"
                               "and come in different variations such as Brazilian, Malaysian,"
                               "straight, and deep wave.")
                         "//ucarecdn.com/5269d7ef-b5c7-4475-9e9c-d16d7cbbdda3/"))
   (merge {:catalog/category-id "25"
           :category/new?       true

           :copy/title "Ready to Wear Wigs"
           :page/slug  "ready-wear-wigs"
           :seo/title  "Ready to Wear Wigs"

           :copy/description      (copy "Made of authentic and high-quality human hair,"
                                        "ready to wear wigs are a quick,"
                                        "convenient way to change up your look instantly.")
           :subcategory/image-uri "//ucarecdn.com/a4f7ad94-3c2c-41aa-be4d-94d9d83b1602/-/format/auto/-/resize/124x/"

           :catalog/department    #{"hair"}
           :hair/family           #{"ready-wigs"}
           :selector/essentials   [:hair/family :catalog/department]
           :selector/electives    [:hair/texture :hair/origin]
           :opengraph/description (copy "100% virgin human hair, machine-wefted and backed by our"
                                        "30 Day Quality Guarantee, our Wigs can be customized to fit"
                                        "your unique look using the built-in combs and adjustable strap.")}
          (category->seo "Ready-to-Wear Human Hair Lace Wigs"
                         (copy "Mayvenn’s ready-to-wear human hair lace wigs provide a quick style switch-up and "
                               "come in different variations such as Brazilian, straight, and loose wave.")
                         "//ucarecdn.com/5269d7ef-b5c7-4475-9e9c-d16d7cbbdda3/"))
   (merge {:catalog/category-id "26"
           :category/new?       true

           :copy/title "Virgin 360 Lace Wigs"
           :page/slug  "virgin-360-wigs"
           :seo/title  "Virgin 360 Lace Wigs"

           :copy/description      (copy "Ideal for ponytails, these wigs are denser & fuller."
                                        "360 wigs have lace around the entire crown of your head with a cap in the middle.")
           :subcategory/image-uri "//ucarecdn.com/fe34e6e9-8927-4b62-94ac-91b37f0a137f/-/format/auto/-/resize/124x/"

           :catalog/department    #{"hair"}
           :hair/family           #{"360-wigs"}
           :selector/essentials   [:hair/family :catalog/department]
           :selector/electives    [:hair/texture :hair/origin]
           :opengraph/description (copy "100% virgin human hair, machine-wefted and backed by our"
                                        "30 Day Quality Guarantee, our Wigs can be customized to fit"
                                        "your unique look using the built-in combs and adjustable strap.")}
          (category->seo "360 Lace Wigs: Human Hair 360 Lace Wigs"
                         (copy "Mayvenn’s human hair 360 lace wigs give you all around protection and"
                               "come in different variations such as Brazilian, Malaysian, straight,"
                               "and deep wave.")
                         "//ucarecdn.com/5269d7ef-b5c7-4475-9e9c-d16d7cbbdda3/"))])

(def mayvenn-install-eligible
  [{:catalog/category-id            "23"
    :experience/exclude             #{"mayvenn-classic"}
    :catalog/department             #{"hair"}
    :category/show-title?           true
    :category/new?                  true
    :copy/description               "Get a free Mayvenn Install by a licensed stylist when you purchase 3 or more items. "
    :copy/learn-more                [events/popup-show-consolidated-cart-free-install]
    :copy/title                     "Mayvenn Install"
    :hair/family                    #{"bundles" "closures" "frontals" "360-frontals"}
    :images                         {:hero {:filename    "mayvenn-install-hero-image",
                                            :desktop-url "//ucarecdn.com/b1d0e399-8e62-4f34-aa17-862a9357000b/",
                                            :mobile-url  "//ucarecdn.com/4f9bc98f-2834-4e1f-9e9e-4ca680edd81f/",
                                            :alt         "New Mayvenn Install"}}
    :image-url                      "//ucarecdn.com/5269d7ef-b5c7-4475-9e9c-d16d7cbbdda3/"
    :page/slug                      "mayvenn-install"
    :page/title                     "Mayvenn Install Eligible | Mayvenn"
    :page.meta/description          (copy "Mayvenn’s Natural Lace Front Wigs and 360 Wigs."
                                          "Comes in different variations such as Brazilian and Malaysian, straight, deep wave and loose wave.")
    :promo.mayvenn-install/eligible #{true}
    :opengraph/description          (copy "100% virgin human hair, machine-wefted and backed by our"
                                          "30 Day Quality Guarantee, our Wigs can be customized to fit"
                                          "your unique look using the built-in combs and adjustable strap.")
    :opengraph/title                (copy "Mayvenn 360 and Lace Frontal Wigs - Free shipping."
                                          "Free 30 day returns. Made with 100% virgin human hair.")
    :selector/electives             [:hair/texture :hair/family :hair/origin :hair/color]
    :selector/essentials            [:catalog/department :promo.mayvenn-install/eligible]
    :subsections                    {"straight"       {:title/primary "Straight"
                                                       :order         1}
                                     "yaki-straight"  {:title/primary "Yaki Straight"
                                                       :order         2}
                                     "kinky-straight" {:title/primary "Kinky Straight"
                                                       :order         3}
                                     "body-wave"      {:title/primary "Body Wave"
                                                       :order         4}
                                     "loose-wave"     {:title/primary "Loose Wave"
                                                       :order         5}
                                     "water-wave"     {:title/primary "Water Wave"
                                                       :order         6}
                                     "deep-wave"      {:title/primary "Deep Wave"
                                                       :order         7}
                                     "curly"          {:title/primary "Curly"
                                                       :order         8}}}])

(def the-only-stylist-exclusive
  (merge {:catalog/category-id       "14"
           :auth/requires             #{:stylist}
           :footer/order              30
           :header/order              30
           :header/group              2
           :copy/title                "Stylist Exclusives"
           :page/slug                 "stylist-exclusives"
           :direct-to-details/id      "49"
           :direct-to-details/slug    "rings-kits"
           :direct-to-details/sku-id  "SK3"
           :catalog/department        #{"stylist-exclusives"},
           :kits/contents             #{"rings"},
           :stylist-exclusives/family #{"kits"}
           :selector/electives        []
           :selector/essentials       [:catalog/department :kits/contents :stylist-exclusives/family]
           :page/title                "Stylist"
           :opengraph/title           (copy "Stylist Exclusives - Free shipping."
                                            "Free 30 day returns. Made with 100% virgin human hair.")
           :page.meta/description     (copy "")
           :opengraph/description     (copy "")
           :seo/sitemap               false}))

(def stylist-exclusives
  [the-only-stylist-exclusive])

(def virgin-hair-nav-roots
  [(merge {:catalog/category-id   "15"
           :copy/title            "Virgin Hair"
           :page/slug             "virgin-hair"
           :catalog/department    #{"hair"}
           :hair/color.process    #{"natural"}
           :hair/source           #{"virgin"},
           :hair/family           :query/missing
           :hair/texture          #{"straight"
                                    "kinky-straight"
                                    "yaki-straight"
                                    "body-wave"
                                    "deep-wave"
                                    "water-wave"
                                    "loose-wave"
                                    "curly"}
           :selector/essentials   [:catalog/department :hair/color.process :hair/texture :hair/source]
           :selector/electives    []
           :page/title            ""
           :opengraph/title       (copy "" "")
           :page.meta/description (copy "")
           :opengraph/description (copy "")
           :page/redirect?        true
           :seo/sitemap           false})
   (merge {:catalog/category-id "12"
           :copy/title          "Closures & Frontals"
           :page/slug           "closures-and-frontals"
           :catalog/department  #{"hair"}
           :hair/family         #{"closures" "frontals" "360-frontals"}
           :hair/color.process  #{"dyed" "natural"}
           :hair/source         #{"virgin"}
           :selector/essentials [:catalog/department :hair/family :hair/color.process :hair/source]
           :hamburger/order     1
           :selector/electives  []
           :copy/description    (copy "Save your precious strands and top your look"
                                      "off with the ultimate tool in protective weave"
                                      "styling. Our collection of closures and frontals blend"
                                      "seamlessly with our bundles and can be customized"
                                      "to fit your unique look.")
           :page/redirect?      true
           :seo/sitemap         false})])

(def seamless-clip-ins-category
  [(merge
    {:catalog/category-id "21"
     :catalog/department  #{"hair"}
     :category/new?       false

     :hair/family         #{"seamless-clip-ins"}
     :selector/electives  [:hair/weight :hair/texture :hair/color]
     :selector/essentials [:catalog/department :hair/family]

     :copy/title       "Clip-Ins"
     :copy/description (copy "Get the hair of your dreams in an instant with our seamless clip-in extensions."
                             "Featuring a thin, polyurethane (PU) weft that flawlessly blends with your own hair."
                             "Ditch the tracks for a clip-in experience that is truly seamless.")

     :page/title "Clip-In Hair Extensions: Human Hair Clip-In Extensions | Mayvenn"
     :page/slug  "seamless-clip-ins"
     :page.meta/description (copy "Get the hair of your dreams with our seamless clip-in hair extensions."
                                  "Featuring a thin, polyurethane (PU) weft that flawlessly blends with your own hair.")
     :seo/title "Clip-In Hair Extensions"

     :opengraph/title       "Mayvenn Clip-In Hair Extensions - Free shipping. Free 30 day returns. Made with 100% human hair extensions."
     :opengraph/description "Blending flawlessly with your own hair and backed by our 30 Day Quality Guarantee, our seamless clip-in extensions are the best quality products on the market and ships free!"

     :images {:hero {:filename    "categories-header-clip"
                     :desktop-url "//ucarecdn.com/96b65a4b-34d5-4b45-b5a7-cf4156d355c0/"
                     :mobile-url  "//ucarecdn.com/be8b83db-370f-45f9-bc37-4f674abbd718/"
                     :alt         "Clip-Ins"}}

     :footer/order     40
     :dtc-footer/order 11
     :header/group     2
     :header/order     4
     :seo/sitemap      true}
    clip-in-tape-in-templates)])

(def the-only-tape-in-category
  (merge {:catalog/category-id "22"
          :catalog/department  #{"hair"}
          :category/new?       false

          :hair/family         #{"tape-ins"}
          :selector/electives  [:hair/color :hair/weight :hair/length]
          :selector/essentials [:catalog/department :hair/family]

          :copy/title "Tape-Ins"

          :page/slug "tape-ins"

          :direct-to-details/id     "111"
          :direct-to-details/slug   "50g-straight-tape-ins"
          :direct-to-details/sku-id "TAPE-S-1-20"

          :footer/order     50
          :dtc-footer/order 12
          :header/group     2
          :header/order     5
          :page/redirect?   true
          :seo/sitemap      false}
         clip-in-tape-in-templates))

(def tape-ins-category
  [the-only-tape-in-category])

(def menu-categories
  (concat virgin-hair
          closures))

(def initial-categories
  (concat wigs
          mayvenn-install-eligible
          stylist-exclusives
          virgin-hair-nav-roots
          menu-categories
          seamless-clip-ins-category
          tape-ins-category))

(defn id->category [id categories]
  (->> categories
       (filter (comp #{(str id)} :catalog/category-id))
       first))

(defn named-search->category [named-search-slug categories]
  (->> categories
       (filter #(= named-search-slug
                   (:legacy/named-search-slug %)))
       first))

(defn current-traverse-nav [data]
  (id->category (get-in data keypaths/current-traverse-nav-id)
                (get-in data keypaths/categories)))

(defn current-category [data]
  (id->category (get-in data catalog.keypaths/category-id)
                (get-in data keypaths/categories)))

