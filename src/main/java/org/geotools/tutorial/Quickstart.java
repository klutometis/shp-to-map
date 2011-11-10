package org.geotools.tutorial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.Callable;

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
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTFileReader;
import com.vividsolutions.jts.io.ParseException;

public class Quickstart {
    static Random RANDOM = new Random();
    static int POINTS = 1000;

    public static Point randomPoint(double X,
                                    double Y,
                                    double width,
                                    double height) {
        return new Point(new CoordinateArraySequence(new Coordinate[] {
                    new Coordinate(RANDOM.nextDouble() * width + X,
                                   RANDOM.nextDouble() * height + Y,
                                   Coordinate.NULL_ORDINATE)
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

    public static List<Point> randomPoints(int n, Envelope envelope) {
        return randomPoints(n,
                            envelope.getMinX(),
                            envelope.getMinY(),
                            envelope.getWidth(),
                            envelope.getHeight());
    }


    public static List<Point> randomPoints(int n, List<Geometry> geometries) {
        return randomPoints(n,
                            new GeometryFactory()
                            .buildGeometry(geometries)
                            .getEnvelopeInternal());
    }

    public static List<Geometry> readGeometries(String file)
        throws IOException, ParseException {
        return new WKTFileReader(file, new WKTReader()).read();
    }

    public static boolean covers(List<Geometry> geometries, Geometry point) {
        for (Geometry geometry: geometries) {
            if (geometry.getEnvelope().covers(point) &&
                geometry.covers(point)) {
                return true;
            }
        }
        return false;
    }

    // Can't return both the time and the object: a map, maybe? Ah,
    // how we pine for the fjords of multiple return values.
    public static <T> T time(Callable<T> callable) throws Exception {
        long start = System.currentTimeMillis();
        T value = callable.call();
        long end = System.currentTimeMillis();
        System.out.println(String.format("Time elapsed: %s", end - start));
        return value;
    }

    public static void main(String[] args) throws Exception {
        final List<Geometry> geometries = readGeometries("us_geometry.txt");
        final List<Point> points = randomPoints(POINTS, geometries);
        time(new Callable<Integer>() {
                public Integer call() {
                    int i = 0;
                    for (Point point: points) {
                        if (covers(geometries, point))
                            i++;
                    }
                    return i;
                }
            });
    }
}
