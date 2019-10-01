(ns storefront.hooks.stringer
  (:require [storefront.browser.tags :refer [insert-tag-with-text remove-tags-by-class remove-tag-by-src]]
            [storefront.config :as config]
            [storefront.platform.messages :refer [handle-message]]))

(def stringer-src "//d6w7wdcyyr51t.cloudfront.net/cdn/stringer/stringer-dd6db0a.js")

(defn insert-tracking [subdomain]
  nil
  #_(let [onload-cb-text ""]
    (insert-tag-with-text
     (str "(function(d,e){function g(a){return function(){var b=Array.prototype.slice.call(arguments);b.unshift(a);c.push(b);return d.stringer}}var c=d.stringer=d.stringer||[],a=[\"init\",\"track\",\"identify\",\"clear\"];if(!c.snippetRan&&!c.loaded){c.snippetRan=!0;for(var b=0;b<a.length;b++){var f=a[b];c[f]=g(f)}a=e.createElement(\"script\");a.type=\"text/javascript\";a.async=!0;a.src=\"" stringer-src "\";a.onload=function(){storefront.core.external_message(['inserted', 'stringer'], {})};b=e.getElementsByTagName(\"script\")[0];b.parentNode.insertBefore(a,b);c.init({environment:\"" config/environment "\",sourceSite:\"storefront\"})}})(window,document);")
     "stringer")))

(defn remove-tracking []
  #_
  (remove-tags-by-class "stringer")
  #_
  (remove-tag-by-src stringer-src))

(defn track-event
  ([event-name] (track-event event-name {} nil))
  ([event-name payload] (track-event event-name payload nil))
  ([event-name payload callback-event] (track-event event-name payload callback-event nil))
  ([event-name payload callback-event callback-args]
   (when (.hasOwnProperty js/window "stringer")
     (.track js/stringer event-name (clj->js payload)
             (when callback-event
               (fn [] (handle-message callback-event (merge {:tracking-event event-name :payload payload} callback-args))))))))

(defn track-page [store-experience]
  (track-event "pageview" {:store-experience store-experience}))

(defn identify
  ([args]
   (identify args nil ))
  ([{:keys [id email]} callback-event]
   (when (.hasOwnProperty js/window "stringer")
     (.identify js/stringer email id)
     (when callback-event
       (handle-message callback-event
                       {:stringer.identify/id    id
                        :stringer.identify/email email})))))

(defn track-clear []
  (when (.hasOwnProperty js/window "stringer")
    (.clear js/stringer)
    (.track js/stringer "clear_identify")))

(defn browser-id []
  (when (and (.hasOwnProperty js/window "stringer")
             js/stringer.loaded)
    (.getBrowserId js/stringer)))
