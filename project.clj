(defproject shp-to-map "1.0.0-SNAPSHOT"
  :description "Facility to convert .shp-files to Scarecrow
  .map-files."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/tools.cli "0.2.2-SNAPSHOT"]
                 [org.geotools/gt-shapefile "8.0-M3"]
                 [com.vividsolutions/jts "1.12"]
                 [debug "1.0.0-SNAPSHOT"]
                 [lambda "1.0.1-SNAPSHOT"]]
  :repositories {"maven2-repository.dev.java.net" "http://download.java.net/maven/2"
                 "osgeo" "http://download.osgeo.org/webdav/geotools/"}
  :main shp-to-map.core)
