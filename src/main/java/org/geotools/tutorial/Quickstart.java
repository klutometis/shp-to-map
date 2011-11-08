package org.geotools.tutorial;

import java.io.File;
import java.util.Random;
import java.util.List;
import java.util.LinkedList;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/**
 * Prompts the user for a shapefile and displays the contents on the screen in a map frame.
 * <p>
 * This is the GeoTools Quickstart application used in documentationa and tutorials. *
 */
public class Quickstart {
    static Random RANDOM = new Random();
    static int POINTS = 42;

    public static Point randomPoint(double X,
                                    double Y,
                                    double width,
                                    double height) {
        return new Point(new CoordinateArraySequence(new Coordinate[] {
                    new Coordinate(RANDOM.nextDouble() * width + X,
                                   RANDOM.nextDouble() * height + Y,
                                   // Coordinate.NULL_ORDINATE doesn't
                                   // work; though it seems like it
                                   // should.
                                   Double.NaN)
                }),
                new GeometryFactory());
    }

    public static List<Point> randomPoints(final int n,
                                           final double X,
                                           final double Y,
                                           final double width,
                                           final double height) {
        return new LinkedList<Point>() {
            {
                for (int i = 0; i < n; i++) {
                    add(randomPoint(X, Y, width, height));
                };
            }
        };
    }

    public static List<Point> randomPoints(double X,
                                           double Y,
                                           double width,
                                           double height) {
        return randomPoints(POINTS, X, Y, width, height);
    }

    /**
     * GeoTools Quickstart demo application. Prompts the user for a shapefile and displays its
     * contents on the screen in a map frame
     */
    public static void main(String[] args) throws Exception {
        // display a data store file chooser dialog for shapefiles
        // File file = JFileDataStoreChooser.showOpenFile("shp", null);
        // if (file == null) {
        //     return;
        // }
        File file = new File("USA_adm0.shp");

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();
        FeatureCollection features = featureSource.getFeatures();
        FeatureIterator iterator = features.features();

        // This ugly thing mandated by
        // e.g. <http://docs.geotools.org/latest/javadocs/org/geotools/data/simple/SimpleFeatureCollection.html>;
        // i.e. generics-style iteration is out.
        try {
            while (iterator.hasNext()) {
                SimpleFeature feature = (SimpleFeature) iterator.next();
                System.out.println(feature.getAttributeCount());
                System.out.println(feature.getDescriptor());
                System.out.println(feature.getDescriptor().getLocalName());
                System.out.println(feature.getDescriptor().getType());
                MultiPolygon mp = (MultiPolygon) feature.getDefaultGeometry();
                System.out.println(mp.getGeometryType());
                System.out.println(mp.getEnvelopeInternal());
                Envelope boundingBox = mp.getEnvelopeInternal();
                double X = boundingBox.getMinX();
                double Y = boundingBox.getMinY();
                double width = boundingBox.getWidth();
                double height = boundingBox.getHeight();
                // System.out.println(mp.getEnvelopeInternal().getLowerCorner());
                // System.out.println(mp.getEnvelopeInternal().getUpperCorner());
                // Coordinate.NULL_ORDINATE doesn't work; though it
                // seems like it should.
                // Point point = new Point(new CoordinateArraySequence(new Coordinate[] {
                //             new Coordinate(0, 0, Double.NaN)
                //         }),
                //     new GeometryFactory());
                // System.out.println(mp.covers(randomPoint(X, Y, width, height)));
                List<Point> randomPoints = randomPoints(X, Y, width, height);
                for (Point point: randomPoints) {
                    System.out.println(point.toString());
                    System.out.println("covers: " + mp.covers(point));
                    System.out.println("within: " + point.within(mp));
                    System.out.println("disjoint: " + mp.disjoint(point));
                    System.out.println("contains: " + mp.contains(point));
                    System.out.println("overlaps: " + mp.overlaps(point));
                }
            }
        } finally {
            features.close(iterator);
        }

        // Create a map content and add our shapefile to it
        // MapContent map = new MapContent();
        // map.setTitle("Quickstart");
        
        // Style style = SLD.createSimpleStyle(featureSource.getSchema());
        // Layer layer = new FeatureLayer(featureSource, style);
        // map.addLayer(layer);

        // Now display the map
        // JMapFrame.showMap(map);
    }
}
