(defproject bsless/impedance "0.0.0-alpha"
  :description "Fast Clojure map transformers and context"
  :url "https://github.com/bsless/impedance"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :username :gpg
                                    :password :gpg
                                    :sign-releases false}]
                        ["releases" :clojars]
                        ["snapshots" :clojars]]
  :release-tasks
  [["vcs" "assert-committed"]
   ["change" "version" "leiningen.release/bump-version" "release"]
   ["vcs" "commit"]
   ["deploy" "clojars"]
   ["change" "version" "leiningen.release/bump-version"]
   ["vcs" "commit"]
   ["vcs" "push"]]
  :profiles
  {:dev
   {:source-paths ["bench"]
    :dependencies
    [[criterium "0.4.5"]
     [meander/epsilon "0.0.512"]]}})
