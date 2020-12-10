(defproject bsless/impedance "0.0.0-alpha1"
  :description "Fast Clojure map transformers and context"
  :url "https://github.com/bsless/impedance"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :username :env/clojars_user
                                    :password :env/clojars_token
                                    :sign-releases false}]
                        ["releases" :clojars]
                        ["snapshots" :clojars]]
  :profiles
  {:dev
   {:source-paths ["bench"]
    :dependencies
    [[criterium "0.4.5"]
     [meander/epsilon "0.0.512"]]}})
