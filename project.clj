(defproject ruin "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1859"]
                 [lonocloud/synthread "1.0.4"]]
  :plugins [[lein-cljsbuild "0.3.3"]]
  :cljsbuild {
              :builds [
                       {:source-paths ["src"]
                        :compiler {:output-to "app/main.js"}}]})
