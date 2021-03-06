(ns homepage.v2020-04
  (:require [clojure.string :refer [join]]
            [homepage.ui.atoms :as A]
            [homepage.ui.contact-us :as contact-us]
            [homepage.ui.diishan :as diishan]
            [homepage.ui.faq :as faq]
            [homepage.ui.guarantees :as guarantees]
            [homepage.ui.hashtag-mayvenn-hair :as hashtag-mayvenn-hair]
            [homepage.ui.hero :as hero]
            [homepage.ui.mayvenn-install :as mayvenn-install]
            [homepage.ui.quality-hair :as quality-hair]
            [homepage.ui.quality-stylists :as quality-stylists]
            [homepage.ui.shopping-categories :as shopping-categories]
            [homepage.ui.wig-customization :as wig-customization]
            [storefront.accessors.categories :refer [query-param-separator]]
            [storefront.accessors.contentful :as contentful]
            [storefront.component :as c]
            [storefront.components.homepage-hero :as homepage-hero]
            [storefront.components.svg :as svg]
            [storefront.components.ui :as ui]
            [storefront.events :as e]
            [storefront.keypaths :as k]))

(c/defcomponent template
  [{:keys [contact-us
           diishan
           faq
           guarantees
           hashtag-mayvenn-hair
           hero
           mayvenn-install
           quality-hair
           quality-stylists
           shopping-categories
           wig-customization]} _ _]
  [:div
   (c/build hero/organism hero)
   (c/build shopping-categories/organism shopping-categories)

   A/horizontal-rule-atom

   (c/build mayvenn-install/organism mayvenn-install)
   (c/build wig-customization/organism wig-customization)

   ;; HACK:
   ;; This is to get desktop (1 3 2) and mobile (1 2 3) ordering
   ;; The when is needed for the conditional divider, as it's unclear
   ;; whether this is an organism together or not
   (when quality-stylists
     [:div
      (A/divider-atom "2d3a98e3-b49a-4f0f-9340-828d12865315")
      [:div.flex-on-dt.flex-wrap.justify-center
       (c/build quality-stylists/organism quality-stylists)
       (c/build quality-hair/organism quality-hair)]])

   (A/divider-atom "7e91271e-874c-4303-bc8a-00c8babb0d77")

   (c/build hashtag-mayvenn-hair/organism hashtag-mayvenn-hair)
   (c/build faq/organism faq)
   (c/build guarantees/organism guarantees)
   (c/build diishan/organism diishan)
   (c/build contact-us/organism contact-us)])

(defn hero-query
  "TODO homepage hero query is reused and complected

  decomplect:
  - handles extraction from cms
  - schematizes according to reused component"
  [cms]
  (let [hero-content
        (or
         (some-> cms :homepage :unified :hero)
         ;; TODO handle cms failure fallback
         {})]
    (assoc-in (homepage-hero/query hero-content)
              [:opts :data-test]
              "hero-link")))

(defn shopping-categories-query
  [categories]
  {:shopping-categories.title/primary "Shop Hair"
   :list/boxes
   (conj
    (->> categories
         (filter ::order)
         (sort-by ::order)
         (mapv
          (fn category->box
            [{:keys [page/slug copy/title catalog/category-id]
              ::keys [image-id]}]
            {:shopping-categories.box/id       slug
             :shopping-categories.box/target   [e/navigate-category
                                                {:page/slug           slug
                                                 :catalog/category-id category-id}]
             :shopping-categories.box/ucare-id image-id
             :shopping-categories.box/label    title})))
    {:shopping-categories.box/id        "need-inspiration"
     :shopping-categories.box/target    [e/navigate-shop-by-look {:album-keyword :look}]
     :shopping-categories.box/alt-label ["Need Inspiration?" "Try shop by look."]})})

(def mayvenn-install-query
  {:mayvenn-install.title/primary   "Free Mayvenn Install"
   :mayvenn-install.title/secondary (str "Purchase 3+ bundles or closure and get a mayvenn install "
                                         "valued up to $200 for absolutely free!")
   :mayvenn-install.image/ucare-id  "625b63a0-5724-4a57-ad79-c9e7a72a7f5b"
   :mayvenn-install.list/primary    "What's included?"
   :list/bullets                    ["Shampoo" "Braid down" "Sew-in and style"]
   :mayvenn-install.cta/id          "shop-now"
   :mayvenn-install.cta/value       "Shop Now"
   :mayvenn-install.cta/target      [e/navigate-category {:page/slug           "human-hair-bundles"
                                                          :catalog/category-id "27"}]})

(def wig-customization-query
  {:wig-customization.title/primary   "Free Wig Customization"
   :wig-customization.title/secondary (str "Purchase any of our virgin lace front wigs or virgin 360 "
                                           "lace wigs and we’ll customize it for free.")
   :wig-customization.image/ucare-id  "beaa9641-35dd-4811-8f57-a10481c5132d"
   :wig-customization.list/primary    "What's included?"
   :list/bullets                      ["Bleaching the knots"
                                       "Tinting the lace"
                                       "Cutting the lace"
                                       "Customize your hairline"]
   :wig-customization.cta/id          "show-wigs"
   :wig-customization.cta/value       "Shop Wigs"
   :wig-customization.cta/target      (let [family (join query-param-separator
                                                         ["360-wigs" "lace-front-wigs"])]
                                        [e/navigate-category
                                         {:catalog/category-id "13"
                                          :page/slug           "wigs"
                                          :query-params        {:family family}}])})

(def quality-hair-query
  {:quality-hair.title/primary   "Hold your hair"
   :quality-hair.title/secondary "high"
   :quality-hair.body/primary    "With the highest industry standards in mind, we have curated a wide variety of textures and colors for you to choose from."
   :quality-hair.cta/id          "shop-hair"
   :quality-hair.cta/label       "shop hair"
   :quality-hair.cta/target      [e/navigate-category {:page/slug           "human-hair-bundles"
                                                       :catalog/category-id "27"}]})

;; HACK shop? is required because link on aladdin is missing
(defn quality-stylists-query
  [shop?]
  (cond-> {:quality-stylists.title/primary   "Sit back and"
           :quality-stylists.title/secondary "relax"
           :quality-stylists.body/primary    "We’ve rounded up the best stylists in the country so you can be sure your hair is in really, really good hands."
           :quality-stylists.image/ucare-ids {:desktop "ac46cdbc-fe7f-469e-bcb8-1efe5e65ea97"
                                              :mobile  "8f14c17b-ffef-4178-8915-640573a8bf3a"}}
    shop?
    (merge
     {:quality-stylists.cta/id          "info-certified-stylists"
      :quality-stylists.cta/label       "Learn more"
      :quality-stylists.cta/target      [e/navigate-info-certified-stylists]})))

(defn hashtag-mayvenn-hair-query
  [ugc]
  (let [images (->> ugc :free-install-mayvenn :looks
                    (mapv (partial contentful/look->homepage-social-card
                                   e/navigate-home
                                   :free-install-mayvenn)))]
    {:hashtag-mayvenn-hair.looks/images images
     :hashtag-mayvenn-hair.cta/id       "see-more-looks"
     :hashtag-mayvenn-hair.cta/label    "see more looks"
     :hashtag-mayvenn-hair.cta/target   [e/navigate-shop-by-look {:album-keyword :look}]}))

(defn faq-query
  [expanded-index]
  {:faq/expanded-index expanded-index
   :list/sections
   [{:faq/title      "Who is going to do my hair?",
     :faq/paragraphs ["Mayvenn Certified Stylists have been chosen because of their professionalism, skillset, and client ratings. We’ve got a network of licensed stylists across the country who are all committed to providing you with amazing service and quality hair extensions."]}
    {:faq/title      "What kind of hair do you offer?"
     :faq/paragraphs ["We’ve got top of the line virgin hair in 8 different textures. In the event that you’d like to switch it up, we have pre-colored options available as well. The best part? All of our hair is quality-guaranteed."]}
    {:faq/title      "What happens after I choose my hair?"
     :faq/paragraphs ["After you choose your hair, you’ll be matched with a Certified Stylist of your choice. You can see the stylist’s work and their salon’s location. We’ll help you book an appointment and answer any questions you may have."]}
    {:faq/title      "Is Mayvenn Install really a better deal?"
     :faq/paragraphs ["Yes! It’s basically hair and service for the price of one. You can buy any 3 bundles, closures and frontals from Mayvenn, and we’ll pay for you to get your hair installed by a local stylist. That means that you’re paying $0 for your next sew-in, with no catch!"]}
    {:faq/title      "How does this process actually work?"
     :faq/paragraphs ["It’s super simple — after you purchase your hair, we’ll send you a pre-paid voucher that you’ll use during your appointment. When your stylist scans it, they get paid instantly by Mayvenn."]}
    {:faq/title      "What if I want to get my hair done by another stylist? Can I still get the Mayvenn Install?"
     :faq/paragraphs ["You must get your hair done from a Certified Stylist in order to get your hair installed for free."]}]})

;; TODO svg ns returns components full of undiffable data
(def guarantees-query
  {:list/icons
   [{:guarantees.icon/image (svg/heart {:class  "fill-p-color"
                                        :width  "32px"
                                        :height "29px"})
     :guarantees.icon/title "Top-Notch Customer Service"
     :guarantees.icon/body  "Our team is made up of hair experts ready to help you by phone, text, and email."}
    {:guarantees.icon/image (svg/calendar {:class  "fill-p-color"
                                           :width  "30px"
                                           :height "33px"})
     :guarantees.icon/title "30 Day Guarantee"
     :guarantees.icon/body  "Wear it, dye it, even cut it! If you're not satisfied we'll exchange it within 30 days."}
    {:guarantees.icon/image (svg/worry-free {:class  "fill-p-color"
                                             :width  "35px"
                                             :height "36px"})
     :guarantees.icon/title "100% Virgin Hair"
     :guarantees.icon/body  "Our hair is gently steam-processed and can last up to a year. Available in 8 textures and 5 shades."}
    {:guarantees.icon/image (svg/mirror {:class  "fill-p-color"
                                         :width  "30px"
                                         :height "34px"})
     :guarantees.icon/title "Certified Stylists"
     :guarantees.icon/body  "Our stylists are chosen because of their industry-leading standards. Both our hair and service are quality guaranteed."}]})

(def diishan-query
  {:diishan.quote/text            "You deserve quality extensions & exceptional service without the unreasonable price tag."
   :diishan.attribution/ucare-ids {:desktop "3208fac6-c974-4c80-8e88-3244ee50226b"
                                   :mobile  "befce648-98b6-45a2-90f0-6199119bfffb" }
   :diishan.attribution/primary   "— Diishan Imira"
   :diishan.attribution/secondary "CEO of Mayvenn"})

;; TODO svg ns returns components full of undiffable data
(def contact-us-query
  {:contact-us.title/primary   "Contact Us"
   :contact-us.title/secondary "We're here to help"
   :contact-us.body/primary    "Have Questions?"
   :list/contact-methods
   [{:contact-us.contact-method/uri   (ui/sms-url "346-49")
     :contact-us.contact-method/svg   (svg/icon-sms {:height 51
                                                     :width  56})
     :contact-us.contact-method/title "Live Chat"
     :contact-us.contact-method/copy  "Text: 346-49"}
    {:contact-us.contact-method/uri   (ui/phone-url "1 (855) 287-6868")
     :contact-us.contact-method/svg   (svg/icon-call {:class  "bg-white fill-black stroke-black circle"
                                                      :height 57
                                                      :width  57})
     :contact-us.contact-method/title "Call Us"
     :contact-us.contact-method/copy  "1 (855) 287-6868"}
    {:contact-us.contact-method/uri   (ui/email-url "help@mayvenn.com")
     :contact-us.contact-method/svg   (svg/icon-email {:height 39
                                                       :width  56})
     :contact-us.contact-method/title "Email Us"
     :contact-us.contact-method/copy  "help@mayvenn.com"}]})

;;;; TODO -> model.stylists

(def ^:private wig-customizations
  [:specialty-wig-customization])
(def ^:private mayvenn-installs
  [:specialty-sew-in-360-frontal
   :specialty-sew-in-closure
   :specialty-sew-in-frontal
   :specialty-sew-in-leave-out])
(def ^:private services
  (into mayvenn-installs wig-customizations))

(defn ^:private offers?
  [menu services]
  (->> ((apply juxt services) menu) ; i.e. select-vals
       (some identity)
       boolean))

;;;;

(defn page
  "Binds app-state to template"
  [app-state]
  (let [cms            (get-in app-state k/cms)
        categories     (get-in app-state k/categories)
        ugc            (get-in app-state k/cms-ugc-collection)
        expanded-index (get-in app-state k/faq-expanded-section)
        shop?          (= "shop" (get-in app-state k/store-slug))
        menu           (get-in app-state k/store-service-menu)]
    (c/build
     template
     (cond->
         {:contact-us           contact-us-query
          :diishan              diishan-query
          :guarantees           guarantees-query
          :hero                 (hero-query cms)
          :hashtag-mayvenn-hair (hashtag-mayvenn-hair-query ugc)
          :shopping-categories  (shopping-categories-query categories)}

       (or shop? (offers? menu mayvenn-installs))
       (merge {:mayvenn-install mayvenn-install-query})

       (or shop? (offers? menu wig-customizations))
       (merge {:wig-customization wig-customization-query})

       (or shop? (offers? menu services))
       (merge {:faq              (faq-query expanded-index)
               :quality-hair     quality-hair-query
               ;; HACK shop? is required because link on aladdin is missing
               :quality-stylists (quality-stylists-query shop?)})))))
