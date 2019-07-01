(defproject ebenbild "0.3.0"
  :description "tiny library to create predicates from different data that match on \"look-a-likes\""
  :url "https://github.com/IamDrowsy/ebenbild"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-cljsbuild "1.1.7"]]
  :cljsbuild {:builds {:tests
                       {:source-paths ["src" "test"]
                        :compiler {:output-to "target/test.js"
                                   :main ebenbild.core-test
                                   :target :nodejs
                                   :optimizations :none}}}
              :test-commands {"tests" ["node" "target/test.js"]}}
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.1"]
                                  [org.clojure/test.check "0.9.0"]
                                  [org.clojure/clojurescript "1.10.520"]
                                  [cider/piggieback "0.4.1"]
                                  [olical/cljs-test-runner "3.7.0"]]
                   :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}})