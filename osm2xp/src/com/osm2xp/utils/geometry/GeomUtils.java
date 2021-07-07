package com.osm2xp.utils.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.osm2xp.model.geom.Lod13Location;
import com.osm2xp.model.osm.Node;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.CentroidArea;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import com.vividsolutions.jts.geom.util.LineStringExtracter;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

import math.geom2d.Angle2D;
import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.line.Line2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.polygon.LinearRing2D;
import math.geom2d.polygon.Polyline2D;
import math.geom2d.polygon.Rectangle2D;

/**
 * GeomUtils.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class GeomUtils {
	
	public static final double E = 0.000001;

	public static double bearing(double lat1, double Lng1, double lat2,
			double Lng2) {
		double deltaLong = Math.toRadians(Lng2 - Lng1);

		double latitude1 = Math.toRadians(lat1);
		double latitude2 = Math.toRadians(lat2);

		double y = Math.sin(deltaLong) * Math.cos(lat2);
		double x = Math.cos(latitude1) * Math.sin(latitude2)
				- Math.sin(latitude1) * Math.cos(latitude2)
				* Math.cos(deltaLong);
		double result = Math.toDegrees(Math.atan2(y, x));
		return (result + 360.0) % 360.0;
	}

	/**
	 * compute distance beetween two lat/long points
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 */
//	private static double latLongDistanceOld(double lat1, double lon1,
//			double lat2, double lon2) {
//		double theta = lon1 - lon2;
//		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
//				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
//				* Math.cos(deg2rad(theta));
//		dist = Math.acos(dist);
//		dist = rad2deg(dist);
//		dist = dist * 60 * 1.1515;
//		dist = dist * 1.609344;
//		return dist * 1000;
//	}

	/**
	 * Check if the object fits the polygon.
	 * 
	 * @param xMaxLength
	 *            object x max length.
	 * @param yMaxLength
	 *            object y max length.
	 * @param xMinLength
	 *            bject x min length.
	 * @param yMinLength
	 *            bject y min length.
	 * @param poly
	 *            osm polygon.
	 * @return true if the object can be used for this polygon.
	 */
	public static boolean isRectangleBigEnoughForObject(int xMaxLength,
			int yMaxLength, int xMinLength, int yMinLength, LinearRing2D poly) {
		Boolean result = false;
		if (poly.getVertices().size() == 5) {
			double segment1 = latLongDistance(poly.getVertex(0).y,
					poly.getVertex(0).x, poly.getVertex(1).y,
					poly.getVertex(1).x);
			double segment2 = latLongDistance(poly.getVertex(1).y,
					poly.getVertex(1).x, poly.getVertex(2).y,
					poly.getVertex(2).x);
			result = segment1 < xMaxLength && segment1 > xMinLength
					&& segment2 < yMaxLength && segment2 > yMinLength
					|| segment1 < yMaxLength && segment1 > yMinLength
					&& segment2 < xMaxLength && segment2 > xMinLength;

		}
		return result;
	}
	
	public static Geometry polylineToJtsGeom(Polyline2D polyline2d) {
		if (polyline2d instanceof LinearRing2D) {
			return linearRing2DToJtsPolygon((LinearRing2D) polyline2d);
		}
		List<Coordinate> coords = new ArrayList<Coordinate>();
		for (Point2D point : polyline2d.getVertices()) {
			coords.add(new Coordinate(point.x, point.y));
		}
		Coordinate[] points = (Coordinate[]) coords
				.toArray(new Coordinate[coords.size()]);
		GeometryFactory geometryFactory = new GeometryFactory();
		return geometryFactory.createLineString(points);
	}

	public static Polygon linearRing2DToJtsPolygon(LinearRing2D ring2d) {
		List<Coordinate> coords = new ArrayList<Coordinate>();
		for (Point2D point : ring2d.getVertices()) {
			coords.add(new Coordinate(point.x, point.y));
		}
		if (coords.size() > 1 && !coords.get(coords.size() - 1).equals(coords.get(0))) {
			coords.add(coords.get(0));
		}
		Coordinate[] points = (Coordinate[]) coords
				.toArray(new Coordinate[coords.size()]);
		CoordinateSequence coordSeq = CoordinateArraySequenceFactory.instance()
				.create(points);
		GeometryFactory geometryFactory = new GeometryFactory(getDefaultPrecisionModel());
		LinearRing linearRing = geometryFactory.createLinearRing(coordSeq);
		Polygon jtsPolygon = geometryFactory.createPolygon(linearRing, null);
		return jtsPolygon;
	}

	public static Double latLongDistance(double lat1, double lon1, double lat2,
			double lon2) {
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
				* Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;

		int meterConversion = 1609;

		return new Double(dist * meterConversion);
	}

//	private static double deg2rad(double deg) {
//		return (deg * Math.PI / 180.0);
//	}
//
//	private static double rad2deg(double rad) {
//		return (rad * 180.0 / Math.PI);
//	}

	/**
	 * compute min and max vectors for a polygon
	 * 
	 * @param polygon
	 * @return double[]
	 */
	public static Double[] computeExtremeVectors(Polyline2D polygon) {

		Double minVector = null;
		Double maxVector = null;

		for (LineSegment2D segment : polygon.getEdges()) {
			Double distance = latLongDistance(segment.getFirstPoint().y,
					segment.getFirstPoint().x, segment.getLastPoint().y,
					segment.getLastPoint().x);
			if (minVector == null || minVector > distance)
				minVector = new Double(distance);
			if (maxVector == null || maxVector < distance)
				maxVector = new Double(distance);
		}
		return new Double[] { minVector, maxVector };
	}

	/**
	 * compute min and max vectors for a polygon
	 * 
	 * @param polygon
	 * @return double[]
	 */
//	public static Double[] computeExtremeVectorsBad(LinearRing2D polygon) {
//
//		Double minVector = null;
//		Double maxVector = null;
//
//		for (LineSegment2D segment : polygon.getEdges()) {
//			LineSegment lineSegment = new LineSegment(
//					segment.getFirstPoint().x, segment.getFirstPoint().y,
//					segment.getLastPoint().x, segment.getLastPoint().y);
//			double distance = lineSegment.getLength() * 100000;
//			if (minVector == null || minVector > distance)
//				minVector = new Double(distance);
//			if (maxVector == null || maxVector < distance)
//				maxVector = new Double(distance);
//		}
//		return new Double[] { minVector, maxVector };
//	}

	/**
	 * Compute the smallest vector of the polygon in meters.
	 * 
	 * @param polygon
	 * @return Double
	 */
	public static Double computeMinVector(LinearRing2D polygon) {
		Double[] vectors = computeExtremeVectors(polygon);
		return vectors[0];
	}
	
	/**
	 * Compute length of line formed by given points array in meters
	 * 
	 * @param line - specified by {@link Point2D} coordinates (lat, lon) array
	 * @return line length in meters
	 */
	public static double computeLengthInMeters(Point2D[] line) {
		double sum = 0;
		for (int i = 1; i < line.length; i++) {
			sum += latLongDistance(line[i-1].y,
					line[i-1].x, line[i].y,line[i].x);
		}
		return sum;
	}
	
	/**
	 * Compute polyline edge length sum or perimeter of the polygon in meters.
	 * 
	 * @param polyline - polyline with edges specified by coordinates (lat, lon) 
	 * @return Double - perimeter value, meters
	 */
	public static double computeEdgesLength(Polyline2D polyline) {
		double sum = 0;
		for (LineSegment2D segment : polyline.getEdges()) {
			Double distance = latLongDistance(segment.getFirstPoint().y,
					segment.getFirstPoint().x, segment.getLastPoint().y,
					segment.getLastPoint().x);
			sum += distance;
		}
		return sum;
	}

	/**
	 * Compute the laregest vector of the polygon in meters.
	 * 
	 * @param polygon
	 * @return Double
	 */
//	public static Double computeMaxVector(LinearRing2D polygon) {
//		Double[] vectors = computeExtremeVectors(polygon);
//		return vectors[1];
//	}

	/**
	 * @param pointA
	 * @param pointB
	 * @return
	 */
	public static boolean compareCoordinates(Point2D pointA, Point2D pointB) {
		return ((int) Math.floor(pointA.x) == (int) Math.floor(pointB.x) && (int) Math
				.floor(pointA.y) == (int) Math.floor(pointB.y));
	}

	/**
	 * @param tile
	 * @param nodes
	 * @return
	 */
//	public static boolean isListOfNodesOnTile(Point2D tile, List<Node> nodes) {
//		for (Node node : nodes) {
//			if (!compareCoordinates(tile, node)) {
//				return false;
//			}
//		}
//		return true;
//	}

	/**
	 * @param linearRing2D
	 * @return
	 */
//	public static LineSegment2D getLargestVector(LinearRing2D linearRing2D) {
//		LineSegment2D result = null;
//		for (LineSegment2D loc : linearRing2D.getEdges()) {
//			if (result == null || loc.getLength() > result.getLength()) {
//				result = loc;
//			}
//		}
//		return result;
//	}
	
	public static Point2D cleanCoordinatePoint(double latitude,
			double longitude) {
		int longi = (int) Math.floor(longitude);
		int lati = (int) Math.floor(latitude);
		Point2D cleanedLoc = new Point2D(longi, lati);
		return cleanedLoc;
	}

	public static Point2D cleanCoordinatePoint(Point2D basePoint) {
		int longi = (int) Math.floor(basePoint.x);
		int lati = (int) Math.floor(basePoint.y);
		Point2D cleanedLoc = new Point2D(longi, lati);
		return cleanedLoc;
	}

	
	/**
	 * @param linearRing2D
	 * @return
	 */
	private static Polygon linearRing2DToPolygon(LinearRing2D linearRing2D) {
		List<Coordinate> coordinates = new ArrayList<Coordinate>();
		for (Point2D pt : linearRing2D.getVertices()) {
			Coordinate coordinate = new LatLonCoordinate(pt.x, pt.y, 1);
			coordinates.add(coordinate);
		}

		Coordinate[] coordinatesTab = (Coordinate[]) coordinates
				.toArray(new Coordinate[coordinates.size()]);

		GeometryFactory factory = new GeometryFactory(getDefaultPrecisionModel());
		return factory.createPolygon(coordinatesTab);
	}

	public static PrecisionModel getDefaultPrecisionModel() {
		return new PrecisionModel(1000000);
	}

	/**
	 * @param polygon
	 * @return
	 */
	private static LinearRing2D polygonToLinearRing2D(Geometry polygon) {

		List<Point2D> points = new ArrayList<Point2D>();
		Coordinate[] coordinates = polygon instanceof Polygon ? ((Polygon) polygon).getExteriorRing().getCoordinates() : polygon.getCoordinates();
		for (Coordinate coordinate : coordinates) {
			Point2D point2d = new Point2D(coordinate.x, coordinate.y);
			points.add(point2d);
		}

		return new LinearRing2D(points);
	}

	/**
	 * @param linearRing2D
	 * @return
	 */
	private static Boolean isLinearRingOnASingleTile(LinearRing2D linearRing2D) {
		for (Point2D point : linearRing2D.getVertices()) {
			if (!compareCoordinates(linearRing2D.getFirstPoint(), point)) {
				return false;
			}
		}
		return true;
	}

	public static boolean areParallelsSegmentsIdentics(LinearRing2D linearRing2D) {
		if (linearRing2D.getVertexNumber() == 5) {

			LineSegment2D line1 = new LineSegment2D(linearRing2D.getVertex(0),
					linearRing2D.getVertex(1));
			LineSegment2D line2 = new LineSegment2D(linearRing2D.getVertex(1),
					linearRing2D.getVertex(2));
			LineSegment2D line3 = new LineSegment2D(linearRing2D.getVertex(2),
					linearRing2D.getVertex(3));
			LineSegment2D line4 = new LineSegment2D(linearRing2D.getVertex(3),
					linearRing2D.getVertex(4));

			double length1 = line1.getLength();
			double length2 = line2.getLength();
			double length3 = line3.getLength();
			double length4 = line4.getLength();

			double diff1 = Math.abs(length1 - length3);
			double diff2 = Math.abs(length2 - length4);

			boolean sameLength1 = diff1 < (line1.getLength() / 10);
			boolean sameLength2 = diff2 < (line2.getLength() / 10);
			if (sameLength1 && sameLength2) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param sourceFootprint
	 * @return
	 */
	public static LinearRing2D simplifyPolygon(LinearRing2D sourceFootprint) {
		LinearRing2D result = sourceFootprint;
		if (sourceFootprint.getVertexNumber() > 5) {
			result = ShortEdgesDeletion(sourceFootprint);
			if (result == null) {
				result = sourceFootprint;
			}
		}
		return result;

	}

	/**
	 * remove some vertex to simplify polygon
	 * 
	 * @param polygon
	 * @return
	 */
	public static LinearRing2D ShortEdgesDeletion(LinearRing2D sourceFootprint) {
		try {
			//test on way 51812313
			// we create a jts polygon from the linear ring
			Polygon sourcePoly = linearRing2DToPolygon(sourceFootprint);

			// we create a simplified polygon
//			Geometry cleanPoly = LatLonShortEdgesDeletion.get(sourcePoly, 5);
			Geometry cleanPoly = LatLonShortEdgesDeletion.get(sourcePoly, 5.0); //Min side 5 meters

			// we create a linearRing2D from the modified polygon
			LinearRing2D result = polygonToLinearRing2D(cleanPoly);

			// we check if the simplification hasn't moved one point to another
			// tile
			boolean isOnSingleTile = isLinearRingOnASingleTile(result);
			// we check if the result is a simple footprint
			boolean isASimpleFootprint = result.getVertexNumber() == 5;
			// we check if the result hasn't made too much simplification
			boolean isAreaChangeMinimal = linearRing2DToPolygon(result)
					.getArea() > (sourcePoly.getArea() / 1.5);

			if (isOnSingleTile && isASimpleFootprint && isAreaChangeMinimal) {
				return result;
			} else {
				return null;
			}

		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param polygon
	 * @return
	 */
	public static Double[] getExtremesAngle(LinearRing2D polygon) {
		Double minAngle = null;
		Double maxAngle = null;
		for (int i = 0; i < polygon.getVertexNumber() - 2; i++) {
			Double angle = Math.toDegrees(Angle2D.getAbsoluteAngle(
					polygon.getVertex(i), polygon.getVertex(i + 1),
					polygon.getVertex(i + 2)));
			if (minAngle == null || angle < minAngle) {
				minAngle = angle;
			}

			if (maxAngle == null || angle > maxAngle) {
				maxAngle = angle;
			}
		}
		Double lastAngle = Math.toDegrees(Angle2D.getAbsoluteAngle(
				polygon.getVertex(polygon.getVertexNumber() - 2),
				polygon.getVertex(0), polygon.getVertex(1)));
		if (minAngle == null || lastAngle < minAngle) {
			minAngle = lastAngle;
		}
		if (maxAngle == null || lastAngle > maxAngle) {
			maxAngle = lastAngle;
		}

		return new Double[] { minAngle, maxAngle };
	}

	/**
	 * @param nodes OSM node list
	 * @return Polygon consisting of points specified by given nodes
	 */
	public static LinearRing2D getPolygonFromOsmNodes(List<Node> nodes) {
		LinearRing2D result = new LinearRing2D();
		for (Node node : nodes) {
			result.addPoint(new Point2D(node.getLon(), node.getLat()));
		}
		return result;
	}
	
	/**
	 * @param nodes OSM node list
	 * @return Polyline consisting of points specified by given nodes
	 */
	public static Polyline2D getPolylineFromOsmNodes(List<Node> nodes) {
		if (nodes.size() > 2 && nodes.get(0).getId() == nodes.get(nodes.size() - 1).getId()) {
			return getPolygonFromOsmNodes(nodes);
		}
		Polyline2D result = new Polyline2D();
		for (Node node : nodes) {
			result.addPoint(new Point2D(node.getLon(), node.getLat()));
		}
		return result;
	}
	
	public static Point2D[] getPointsFromOsmNodes(List<Node> nodes) {
		Point2D[] result = new Point2D[nodes.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = new Point2D(nodes.get(i).getLon(), nodes.get(i).getLat());
		}
		return result;
	}

	/**
	 * @param nodes
	 * @return
	 */
	public static Point2D getNodesCenter(List<Node> nodes) {
		Point2D center = null;
		if (nodes.size() > 3) {
			LinearRing2D polygon = new LinearRing2D();

			for (Node node : nodes) {
				polygon.addPoint(new Point2D(node.getLon(), node.getLat()));
			}
			center = getPolylineCenter(polygon);
		} else {
			center = new Point2D(nodes.get(0).getLon(), nodes.get(0).getLat());
		}
		return center;

	}

	/**
	 * @param tile
	 * @param node
	 * @return
	 */
	public static boolean compareCoordinates(Point2D tile, Node node) {

		return ((int) Math.floor(tile.x) == (int) Math.floor(node.getLon()) && (int) Math
				.floor(tile.y) == (int) Math.floor(node.getLat()));

	}

	/**
	 * Set clockwise point order for given {@link LinearRing2D} if necessary
	 * @param linearRing2D
	 * @return clockwise {@link LinearRing2D}
	 */
	public static LinearRing2D setClockwise(LinearRing2D linearRing2D) {
		return setDirection(linearRing2D, true);
	}
	
	/**
	 * Set counter-clockwise point order for given {@link LinearRing2D} if necessary
	 * X-Plane needs facades to be defined in counter-clockwise order, see <a href="https://developer.x-plane.com/2010/07/facade-tuning-and-tips/">this article</a> 
	 * @param linearRing2D
	 * @return counter-clockwise {@link LinearRing2D}
	 */
	public static LinearRing2D setCCW(LinearRing2D linearRing2D) {
		return setDirection(linearRing2D, false);
	}
	
	/**
	 * Set specified point order for given {@link LinearRing2D} if necessary
	 * @param linearRing2D
	 * @param clockwise <code>true</code> to set clockwise point order, <code>false</code> - for counter-clockwise one
	 * @return
	 */
	public static LinearRing2D setDirection(LinearRing2D linearRing2D, boolean clockwise) {
		double edgeSum = 0;
		for (int i = 0; i < linearRing2D.getVertices().size() - 1; i++) {
			double a = (linearRing2D.getVertex(i + 1).x - linearRing2D
					.getVertex(i).x);
			double b = (linearRing2D.getVertex(i + 1).y + linearRing2D
					.getVertex(i).y);
			edgeSum = edgeSum + (a * b);
		}
		if ((edgeSum < 0 && clockwise) || (edgeSum > 0 && !clockwise)) {
			Collection<Point2D> newVectors = linearRing2D
					.getReverseCurve().getVertices();
			linearRing2D.clearVertices();
			linearRing2D.getVertices().addAll(newVectors);
		}
		return linearRing2D;
	}

	/**
	 * Check if a linear ring is counter-clockwise and reverse direction in case it's not CCW
	 * X-Plane facades outer ring needs to be <b>CCW</b> (see https://developer.x-plane.com/2010/07/facade-tuning-and-tips/)
	 * @param ring2d {@link LinearRing2D} to check
	 * @return ring2d itself if it's CCW, reversed direction ring otherwise
	 */
	public static LinearRing2D forceCCW(LinearRing2D ring2d) {
		LinearRing2D result = null;
		if (ring2d.getVertices().size() > 4) { //4 because it makes sense only in case we have 3 points. 4th one is equal to 1st one 

			Coordinate[] coords = new Coordinate[ring2d.getVertices().size()];
			for (int i = 0; i < ring2d.getVertices().size(); i++) {
				coords[i] = new Coordinate(ring2d.getVertex(i).x,
						ring2d.getVertex(i).y);
			}

			if (CGAlgorithms.isCCW(coords)) {
				result = ring2d;
			} else {
				Collection<Point2D> clockwiseVectors = new ArrayList<Point2D>();
				for (int i = ring2d.getVertices().size() - 1; i > -1; i--) {
					clockwiseVectors.add(ring2d.getVertex(i));
				}

				result = new LinearRing2D(clockwiseVectors);
			}
		} else {
			result = ring2d;
		}

		return result;
	}
	
	/**
	 * Check if a linear ring is clockwise and reverse direction in case it's not CW
	 * X-Plane facades inner ring needs to be clockwise
	 * @param ring2d {@link LinearRing2D} to check
	 * @return ring2d itself if it's CW, reversed direction ring otherwise
	 */
	public static LinearRing2D forceCW(LinearRing2D ring2d) {
		if (ring2d.getVertices().size() > 4) { //4 because it makes sense only in case we have 3 points. 4th one is equal to 1st one 
			List<Coordinate> coords = ring2d.getVertices().stream().map(vertex -> new Coordinate(vertex.x, vertex.y)).collect(Collectors.toList());
//			Coordinate[] coords = new Coordinate[ring2d.getVertices().size()];
//			for (int i = 0; i < ring2d.getVertices().size(); i++) {
//				coords[i] = new Coordinate(ring2d.getVertex(i).x,
//						ring2d.getVertex(i).y);
//			}
			if (CGAlgorithms.isCCW(coords.toArray(new Coordinate[0]))) {
				Collection<Point2D> clockwiseVectors = new ArrayList<Point2D>();
				for (int i = ring2d.getVertices().size() - 1; i > -1; i--) {
					clockwiseVectors.add(ring2d.getVertex(i));
				}
				return new LinearRing2D(clockwiseVectors);
			} 
		} 
		
		return ring2d;
	}

	/**
	 * @param polygon
	 * @return
	 */
	public static Point2D getPolylineCenter(Polyline2D polygon) {
		Point2D center = null;
		if (polygon.getVertices().size() > 3) {
			CentroidArea centroidArea = new CentroidArea();
			List<Coordinate> ring = new ArrayList<Coordinate>();
			for (Point2D pt : polygon.getVertices()) {
				ring.add(new Coordinate(pt.x, pt.y));
			}
			Coordinate[] coordinates = new Coordinate[ring.size()];
			centroidArea.add(ring.toArray(coordinates));
			center = new Point2D(centroidArea.getCentroid().x,
					centroidArea.getCentroid().y);
		} else {
			center = polygon.getFirstPoint();
		}
		return center;
	}

	public static Point2D getRotationPoint(Point2D origin, Point2D ptX,
			Point2D ptY, Point2D lastPoint, int xCoord, int yCoord) {

		Point2D result = null;

		LineSegment segmentX = new LineSegment(new Coordinate(origin.x,
				origin.y), new Coordinate(ptX.x, ptX.y));
		LineSegment segmentY = new LineSegment(new Coordinate(origin.x,
				origin.y), new Coordinate(ptY.x, ptY.y));
		LineSegment segment2X = new LineSegment(new Coordinate(ptY.x, ptY.y),
				new Coordinate(lastPoint.x, lastPoint.y));
		LineSegment segment2Y = new LineSegment(new Coordinate(ptX.x, ptX.y),
				new Coordinate(lastPoint.x, lastPoint.y));

		// compute the X point wanted by the user
		float xFragment = (float) xCoord / 100;
		Coordinate xUserPoint = segmentX.pointAlong(xFragment);
		Coordinate x2UserPoint = segment2X.pointAlong(xFragment);
		// compute the Y point wanted by the user
		float yFragment = (float) yCoord / 100;
		Coordinate yUserPoint = segmentY.pointAlong(yFragment);
		Coordinate y2UserPoint = segment2Y.pointAlong(yFragment);

		Line2D xSeg = new Line2D(xUserPoint.x, xUserPoint.y, x2UserPoint.x,
				x2UserPoint.y);
		Line2D ySeg = new Line2D(yUserPoint.x, yUserPoint.y, y2UserPoint.x,
				y2UserPoint.y);
		result = xSeg.getIntersection(ySeg);

		return result;
	}

	public static double RadToDeg(double radians) {
		return radians * (180 / Math.PI);
	}

	public static double DegToRad(double degrees) {
		return degrees * (Math.PI / 180);
	}

	public static double getBearing(double lat1, double long1, double lat2,
			double long2) {
		// Convert input values to radians
		lat1 = DegToRad(lat1);
		long1 = DegToRad(long1);
		lat2 = DegToRad(lat2);
		long2 = DegToRad(long2);

		double deltaLong = long2 - long1;

		double x = Math.sin(deltaLong) * Math.cos(lat2);
		double y = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
				* Math.cos(lat2) * Math.cos(deltaLong);
		double bearing = Math.atan2(x, y);
		return ConvertToBearing(RadToDeg(bearing));
	}

	public static double ConvertToBearing(double deg) {
		return (deg + 360) % 360;
	}

	/**
	 * lod13 algo by Jeema
	 * 
	 * @param latitude
	 * @param longitude
	 * @return Lod13Location
	 */
	public static Lod13Location getLod13Location(double latitude,
			double longitude) {
		StringBuilder lod13String = new StringBuilder();

		double leftLong, rightLong, topLat, bottomLat, centerLat, centerLong;
		double newLeftLong, newRightLong, newTopLat, newBottomLat;
		double xOffset, yOffset;
		int i, quadNumber;

		leftLong = -180.0;
		rightLong = 300.0;
		topLat = 90.0;
		bottomLat = -270.0;

		// ----- Iterate through the 15 level of detail levels to compute the
		// LOD13 location value
		for (i = 0; i < 15; i++) {
			// ----- Get the lat/long values for the center of the current
			// lat/long bounds
			centerLong = (leftLong + rightLong) / 2;
			centerLat = (topLat + bottomLat) / 2;

			// ----- Is the target longitude to the left of the current center
			// longitude?
			if (longitude < centerLong) {
				// ----- Then the new quad is on the left
				quadNumber = 0;
				newLeftLong = leftLong;
				newRightLong = centerLong;
			} else {
				// ----- Otherwise the new quad is on the right
				quadNumber = 1;
				newLeftLong = centerLong;
				newRightLong = rightLong;
			}

			// ----- Is the target latitude above the current center latitude?
			if (latitude > centerLat) {
				// ----- Then the new quad is on the top
				newTopLat = topLat;
				newBottomLat = centerLat;
			} else {
				// ----- Otherwise the new quad is on the bottom
				quadNumber += 2;
				newTopLat = centerLat;
				newBottomLat = bottomLat;
			}

			// ----- Concactenate the quad number onto the LOD13 string
			lod13String.append(quadNumber);

			// ----- Update the left/right/top/bottom bounds for the next
			// iteration
			leftLong = newLeftLong;
			rightLong = newRightLong;
			topLat = newTopLat;
			bottomLat = newBottomLat;
		}

		// ----- Now calculate the X and Y offsets within the LOD13 square
		xOffset = (longitude - leftLong) / (rightLong - leftLong);
		yOffset = (topLat - latitude) / (topLat - bottomLat);

		// ----- Build the Lod13Location object and return it to the caller
		Lod13Location location = new Lod13Location();
		location.setLod13String(lod13String.toString());

		location.setxOffset(xOffset);
		location.setyOffset(yOffset);

		return location;
	}

	public static boolean boxContainsAnotherBox(Box2D box1, Box2D box2) {
		// poly inside another?
		Rectangle2D rect1 = new Rectangle2D(box1.getAsAWTRectangle2D());
		Rectangle2D rect2 = new Rectangle2D(box2.getAsAWTRectangle2D());
		if (box1.containsBounds(rect2)) {
			return true;
		}
		if (box2.containsBounds(rect1)) {
			return true;
		}

		// check intersections
		// for (LinearShape2D line : box1.getEdges()) {
		// for (LinearShape2D line2 : box2.getEdges()) {
		// if (line.getIntersection(line2) != null) {
		// return true;
		// }
		// }
		// }

		return false;
	}

	public static List<Node> removeExtraEnd(List<Node> nodes) {
		if (nodes.size() > 3 && nodes.get(0).getId() == nodes.get(nodes.size() - 1).getId()) {
			nodes.remove(nodes.size() - 1);
		}
		return nodes;
	}
	
	public static boolean isValid(Polyline2D polyline2d) {
		return polylineToJtsGeom(polyline2d).isValid();
	}
	
	/**
	 * Get / create a valid version of the geometry given. If the geometry is a polygon or multi polygon, self intersections /
	 * inconsistencies are fixed. Otherwise the geometry is returned.
	 * 
	 * @param geom
	 * @return a geometry 
	 */
	public static Geometry fix(Geometry geom){
	    if(geom instanceof Polygon){
	        if(geom.isValid()){
//	            geom.normalize(); // validate does not pick up rings in the wrong order - this will fix that //TODO not sure it's needed for our task
	            return geom; // If the polygon is valid just return it
	        }
	        Polygonizer polygonizer = new Polygonizer();
	        addPolygon((Polygon)geom, polygonizer);
	        return toPolygonGeometry(polygonizer.getPolygons(), geom.getFactory());
	    }else if(geom instanceof MultiPolygon){
	        if(geom.isValid()){
	            geom.normalize(); // validate does not pick up rings in the wrong order - this will fix that
	            return geom; // If the multipolygon is valid just return it
	        }
	        Polygonizer polygonizer = new Polygonizer();
	        for(int n = geom.getNumGeometries(); n-- > 0;){
	            addPolygon((Polygon)geom.getGeometryN(n), polygonizer);
	        }
	        return toPolygonGeometry(polygonizer.getPolygons(), geom.getFactory());
	    }else{
	        return geom; // In my case, I only care about polygon / multipolygon geometries
	    }
	}

	/**
	 * Add all line strings from the polygon given to the polygonizer given
	 * 
	 * @param polygon polygon from which to extract line strings
	 * @param polygonizer polygonizer
	 */
	static void addPolygon(Polygon polygon, Polygonizer polygonizer){
	    addLineString(polygon.getExteriorRing(), polygonizer);
	    for(int n = polygon.getNumInteriorRing(); n-- > 0;){
	        addLineString(polygon.getInteriorRingN(n), polygonizer);
	    }
	}

	/**
	 * Add the linestring given to the polygonizer
	 * 
	 * @param linestring line string
	 * @param polygonizer polygonizer
	 */
	static void addLineString(LineString lineString, Polygonizer polygonizer){

	    if(lineString instanceof LinearRing){ // LinearRings are treated differently to line strings : we need a LineString NOT a LinearRing
	        lineString = lineString.getFactory().createLineString(lineString.getCoordinateSequence());
	    }

	    // unioning the linestring with the point makes any self intersections explicit.
	    Point point = lineString.getFactory().createPoint(lineString.getCoordinateN(0));
	    Geometry toAdd = lineString.union(point); 

	    //Add result to polygonizer
	    polygonizer.add(toAdd);
	}

	/**
	 * Get a geometry from a collection of polygons.
	 * 
	 * @param polygons collection
	 * @param factory factory to generate MultiPolygon if required
	 * @return null if there were no polygons, the polygon if there was only one, or a MultiPolygon containing all polygons otherwise
	 */
	static Geometry toPolygonGeometry(Collection<Polygon> polygons, GeometryFactory factory){
	    switch(polygons.size()){
	        case 0:
	            return null; // No valid polygons!
	        case 1:
	            return polygons.iterator().next(); // single polygon - no need to wrap
	        default:
	            //polygons may still overlap! Need to sym difference them
	            Iterator<Polygon> iter = polygons.iterator();
	            Geometry ret = iter.next();
	            while(iter.hasNext()){
	            	if (ret instanceof GeometryCollection) {
	            		List<Polygon> polys = flatMapToPoly(ret);
	            		if (!polys.isEmpty()) {
	            			ret = polys.get(0);
	            		} else {
	            			return null;
	            		}
	            	}
	                ret = ret.symDifference(iter.next());
	            }
	            return ret;
	    }
	}
	
	public static List<Geometry> flatMap(Geometry  geometry) {
		List<Geometry> resList = new ArrayList<Geometry>();
		if (geometry instanceof GeometryCollection) {
			for (int i = 0; i < geometry.getNumGeometries(); i++) {
				Geometry curGeom = geometry.getGeometryN(i);
				if (curGeom != null) {
					resList.addAll(flatMap(curGeom));
				}
			}
		} else if (geometry != null){
			resList.add(geometry);
		}
		return resList;		
	}
	
	public static List<Polygon> flatMapToPoly(Geometry geometry) {
		List<Polygon> resList = new ArrayList<Polygon>();
		if (geometry instanceof GeometryCollection) {
			for (int i = 0; i < geometry.getNumGeometries(); i++) {
				Geometry curGeom = geometry.getGeometryN(i);
				if (curGeom != null) {
					resList.addAll(flatMapToPoly(curGeom));
				}
			}
		} else if (geometry instanceof Polygon){
			resList.add((Polygon) geometry);
		}
		return resList;		
	}

	/**
	 * Get / create a valid version of given {@link LinearRing2D}. If the geometry is a polygon or multi polygon, self intersections /
	 * inconsistencies are fixed. Otherwise polygon itself is returned.
	 * 
	 * @param polygon
	 * @return
	 */
	public static List<LinearRing2D> fix(LinearRing2D polygon) {
		Geometry fixed = fix(linearRing2DToJtsPolygon(polygon));
		if (fixed instanceof Polygon) {
			return Collections.singletonList(polygonToLinearRing2D(fixed));
		} else if (fixed instanceof MultiPolygon) {
			List<LinearRing2D> resList = new ArrayList<LinearRing2D>();
			for (int i = 0; i < fixed.getNumGeometries(); i++) {
				Geometry geom = fixed.getGeometryN(i);
				if (geom instanceof Polygon) {
					resList.add(polygonToLinearRing2D(geom));
				}
			}
			return resList;
		}
		return Collections.singletonList(polygon);
	}
	

	@SuppressWarnings("unchecked")
	public static Geometry polygonize(Geometry geometry) {
		List<?> lines = LineStringExtracter.getLines(geometry);
		Polygonizer polygonizer = new Polygonizer();
		polygonizer.add(lines);
		Collection<Polygon> polys = polygonizer.getPolygons();
		Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);
		return geometry.getFactory().createGeometryCollection(polyArray);
	}

	public static Geometry splitPolygon(Geometry poly, Geometry line) {
		Geometry nodedLinework = poly.getBoundary().union(line);
		Geometry polys = polygonize(nodedLinework);

		// Only keep polygons which are inside the input
		List<Polygon> output = new ArrayList<Polygon>();
		for (int i = 0; i < polys.getNumGeometries(); i++) {
			Polygon candpoly = (Polygon) polys.getGeometryN(i);
			if (poly.contains(candpoly.getInteriorPoint())) {
				output.add(candpoly);
			}
		}
		return poly.getFactory().createGeometryCollection(GeometryFactory.toGeometryArray(output));
	}
	
	public static Collection<? extends Geometry> cutHoles(Geometry geometry, int maxHoleCount) {
		if (!(geometry instanceof Polygon)) {
			return Collections.singletonList(geometry);
		}
		int numHoles = ((Polygon) geometry).getNumInteriorRing();
		if (numHoles <= maxHoleCount) {
			return Collections.singletonList(geometry);
		}
		GeometryFactory geometryFactory = new GeometryFactory(GeomUtils.getDefaultPrecisionModel());
		Polygon poly = (Polygon) geometry;
		Envelope envelope = poly.getExteriorRing().getCoordinateSequence().expandEnvelope(new Envelope());
		Coordinate p1 = poly.getInteriorRingN(0).getCentroid().getCoordinate();
		//If we have only one hole, cutting line is horizontal line containing it's center
		//If we have more - cutting line is a line going through centers of two first holes - this would allow to git rid of two holes at least per each cut
		LineString cuttingLine;								
		if (numHoles > 1) {
			Coordinate p2 =  poly.getInteriorRingN(1).getCentroid().getCoordinate();
			double dx = p2.x-p1.x;
			if (Math.abs(dx) < GeomUtils.E) { //Use vertical cutting line
				cuttingLine = geometryFactory.createLineString(new Coordinate[] {
						new Coordinate(p1.x, envelope.getMinY()), 
						new Coordinate(p1.x, envelope.getMaxY()),
						});
			} else {
				double k = (p2.y-p1.y)/dx;
				double b = p1.y - k* p1.x;
				cuttingLine = geometryFactory.createLineString(new Coordinate[] {
						new Coordinate(envelope.getMinX(), k * envelope.getMinX() + b), 
						new Coordinate(envelope.getMaxX(), k * envelope.getMaxX() + b)
						});
			}
		} else {
			cuttingLine = geometryFactory.createLineString(new Coordinate[] {
					new Coordinate(envelope.getMinX(), p1.y), 
					new Coordinate(envelope.getMaxX(), p1.y)
					});
		}
		List<Geometry> cutResult = GeomUtils.flatMap(GeomUtils.splitPolygon(geometry, cuttingLine));
		List<Geometry> resultList = new ArrayList<Geometry>();
		for (Geometry curGeom : cutResult) {
			resultList.addAll(cutHoles(curGeom, maxHoleCount));
		}
		return resultList;
	}

}
