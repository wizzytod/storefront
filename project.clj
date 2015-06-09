(defproject storefront "0.1.0-SNAPSHOT"
  :description "The front of the store"
  :url "https://github.com/Mayvenn/storefront"
  
            
  :dependencies [[org.clojure/clojure "1.6.0"]

                 [com.taoensso/timbre "3.4.0" :exclusions [org.clojure/tools.reader]]
                 [com.stuartsierra/component "0.2.2"]
                 [environ "1.0.0"]
                 [clj-honeybadger "0.3.0"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [clj-http "1.0.1" :exclusions [org.clojure/tools.reader]]
                 [compojure "1.3.2"]
                 [noir-exception "0.2.3"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-defaults "0.1.3"]
                 [ring-jetty-component "0.2.2"]
                 [ring-logging "0.1.0"]
                 [hiccup "1.0.5"]

                 [org.clojure/clojurescript "0.0-3211"]
                 [org.clojure/core.cache "0.6.3"]
                 [org.clojure/core.memoize "0.5.6" :exclusions [org.clojure/core.cache]]
                 [org.omcljs/om "0.8.8"]
                 [sablono "0.3.4"]
                 [cljs-ajax "0.3.11"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [bidi "1.18.11"]
                 [com.cemerick/url "0.1.1"]]
  
                             
                             
  :plugins [
            [lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.3.1"]]
  :figwheel {:nrepl-port 4000
             :css-dirs ["resources/public/css"]}
  :cljsbuild
  {:builds
   {:dev
    {:source-paths ["src-cljs"]
     :figwheel {:on-jsload "storefront.core/on-jsload"}
     :compiler {:main "storefront.core"
                :asset-path "/js/out"
                :output-to "resources/public/js/out/main.js"
                :pretty-print true
                :output-dir "resources/public/js/out"
                :externs ["externs/riskified.js"]}}}}
  :profiles
  {:dev {:source-paths ["dev/clj"]
         :dependencies [[pjstadig/humane-test-output "0.6.0"]
                        [standalone-test-server "0.2.1"]
                        [org.clojure/tools.namespace "0.2.9"]
                        [figwheel-sidecar "0.3.1"]]
         :injections [(require 'pjstadig.humane-test-output)
                      (pjstadig.humane-test-output/activate!)]
         :cljsbuild
         {:builds {:dev {:source-paths ["dev/cljs"]}}}}})
