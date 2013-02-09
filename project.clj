(defproject ecs-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :local-repo "lib"
  :profiles {:dev {:plugins [[lein-midje "2.0.0-SNAPSHOT"]
                             [lein-cloverage "1.0.2"]]}}
  :resource-paths ["src/ecs_test/game/assets"
                   "src/ecs_test/game/assets/sprites"
                   "src/ecs_test/game/manifests"]
  :aot [ecs-test.main]
  :main ecs-test.main
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [seesaw "1.4.2"]
                 [midje "1.4.0"]
                 [com.stuartsierra/lazytest "1.2.3"]
                 [org.clojure/tools.logging "0.2.3"]
                 [clj-logging-config "1.9.10"]
                 [org.clojure/tools.logging "0.2.3"]]
  :repositories {"stuart" "http://stuartsierra.com/maven2"})


;:warn-on-reflection true

