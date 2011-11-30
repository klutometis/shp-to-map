(ns shp-to-map.core
  (:use [debug.core :only (debug)]
        [lambda.core :only (λ)]
        [clojure.tools.cli :only (cli)])
  (:gen-class))

(import 'java.net.URLClassLoader)
(import 'java.net.URL)
(import 'java.io.File)

(import 'org.geotools.data.shapefile.ShapefileDataStore)
(import 'com.vividsolutions.jts.io.WKBWriter)

(def eval-string
  (λ [string]
     ((comp eval read-string) string)))

(def default-feature-name
  (λ [feature]
     (.getAttribute feature "NAME10")))

(def default-feature-geometry
  (λ [feature]
     (.getDefaultGeometry feature)))

(def default-feature-filter (constantly true))

(let [writer (new WKBWriter)]
  (def print-shape-map
    (λ [feature-name feature-geometry feature-filter files]
       (doseq [[name geometries]
               (reduce
                (λ [name->geometries file]
                   (reduce
                    (λ [name->geometries feature]
                       (let [hex (WKBWriter/toHex
                                  (.write writer
                                          (feature-geometry feature)))
                             name (feature-name feature)]
                         (assoc name->geometries
                           name
                           (cons hex
                                 (get name->geometries
                                      name
                                      '())))))
                    name->geometries
                    (.toArray
                     (.getFeatures
                      (.getFeatureSource
                       (new ShapefileDataStore
                            (.toURL (.toURI (new File file)))))))))
                {}
                files)]
         (printf "%s\t%s\n" name (apply str (interpose "|" geometries)))))))

(def -main
  (λ [& args]
     (let [[{feature-name :feature-name
             feature-geometry :feature-geometry
             feature-filter :feature-filter}
            files
            usage]
           (cli args
                ["-n" "--name" "Extract a name from a feature"
                 :name :feature-name
                 :parse-fn eval-string
                 :default default-feature-name]
                ["-g" "--geometry" "Extract a geometry from a feature"
                 :name :feature-geometry
                 :parse-fn eval-string
                 :default default-feature-geometry]
                ["-f" "--filter" "Filter features"
                 :name :feature-filter
                 :parse-fn eval-string
                 :default default-feature-filter])]
       (if (empty? files)
         (println usage)
         (print-shape-map feature-name
                          feature-geometry
                          feature-filter
                          files)))))
