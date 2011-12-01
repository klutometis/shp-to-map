(ns shp-to-map.core
  (:use [debug.core :only (debug)]
        [lambda.core :only (λ)]
        [clojure.tools.cli :only (cli)])
  (:import (java.net URLClassLoader URL)
           (java.io File)
           (org.geotools.data.shapefile ShapefileDataStore)
           (com.vividsolutions.jts.io WKBWriter)
           (clojure.lang IFn))
  (:gen-class))

(def eval-string
  (λ [string]
     ((comp eval read-string) string)))

(defmacro def-monadic [name function]
  "Implements a monadic function whose toString is the source of the
function itself."
  `(def ~name
     (reify IFn
       (toString [this] (str (quote ~function)))
       (invoke [this arg1#] (~function arg1#)))))

(def-monadic default-feature-name
  (λ [feature]
     (.getAttribute feature "NAME10")))

(def-monadic default-feature-geometry
  (λ [feature]
     (.getDefaultGeometry feature)))

(def-monadic default-feature-filter
  (constantly true))

;;; Really should just make an ISeq out of features, so that we can
;;; reduce on it; etc.
(def features
  (λ [shapefile]
     (.features
      (.getFeatures
       (.getFeatureSource
        (new ShapefileDataStore
             (.toURL (.toURI (new File shapefile)))))))))

(def reduce-features
  (λ [f val features]
     (with-open [features features]
       (loop [val val]
         (if (.hasNext features)
           (let [feature (.next features)]
             (recur (f val feature)))
           val)))))

(def do-features
  (λ [f features]
     (with-open [features features]
       (loop []
         (if (.hasNext features)
           (let [feature (.next features)]
             (f feature)
             (recur)))))))

(let [writer (new WKBWriter)]
  (def geometry->hex
    (λ [geometry]
       (WKBWriter/toHex (.write writer geometry)))))

(def print-geometries
  (λ [name hexen]
     (printf "%s\t%s\n" name (apply str (interpose "|" hexen)))))

(def print-geometry
  (λ [name hex]
     (printf "%s\t%s\n" name hex)))

(def print-geometry-map
  (λ [feature-name feature-geometry feature-filter files]
     (doseq [file files]
       (do-features
        (λ [feature]
           (if (feature-filter feature)
             (let [name (feature-name feature)
                   hex (geometry->hex (feature-geometry feature))]
               (print-geometry name hex))))
        (features file)))))

(def print-bucketed-geometry-map
  (λ [feature-name feature-geometry feature-filter files]
     (doseq [[name geometries]
             (reduce
              (λ [name->geometries file]
                 (reduce-features
                  (λ [name->geometries feature]
                     (if (feature-filter feature)
                       (let [name (feature-name feature)
                             hex (geometry->hex (feature-geometry feature))]
                         (assoc name->geometries
                           name
                           (cons hex
                                 (get name->geometries
                                      name
                                      '()))))
                       name->geometries))
                  name->geometries
                  (features file)))
              {}
              files)]
       (printf "%s\t%s\n" name (apply str (interpose "|" geometries))))))

(def -main
  (λ [& args]
     (let [[{feature-name :feature-name
             feature-geometry :feature-geometry
             feature-filter :feature-filter
             bucket-duplicates :bucket-duplicates}
            files
            usage]
           (cli args
                ["-n" "--name" "Extract a name from a feature"
                 :name :feature-name
                 :parse-fn eval-string
                 :default (with-meta default-feature-name {:doc "harro"})]
                ["-g" "--geometry" "Extract a geometry from a feature"
                 :name :feature-geometry
                 :parse-fn eval-string
                 :default default-feature-geometry]
                ["-f" "--filter" "Filter features"
                 :name :feature-filter
                 :parse-fn eval-string
                 :default default-feature-filter]
                ["-b" "--bucket" "Bucket duplicates"
                 :name :bucket-duplicates
                 :flag true
                 :default true])]
       (if (empty? files)
         (println usage)
         (if bucket-duplicates
           (print-bucketed-geometry-map feature-name
                                        feature-geometry
                                        feature-filter
                                        files)
           (print-geometry-map feature-name
                               feature-geometry
                               feature-filter
                               files))))))
