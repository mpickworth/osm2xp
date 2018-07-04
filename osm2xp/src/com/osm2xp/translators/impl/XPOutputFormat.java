package com.osm2xp.translators.impl;

import static com.osm2xp.translators.impl.XPlaneTranslatorImpl.LINE_SEP;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.osm2xp.model.osm.OsmMultiPolygon;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.translators.xplane.XPPathSegment;
import com.osm2xp.utils.GeomUtils;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;

import math.geom2d.Point2D;
import math.geom2d.polygon.LinearRing2D;
import math.geom2d.polygon.Polyline2D;

public class XPOutputFormat {
	
	public String getPolygonString (Polyline2D poly, String arg1, String arg2) {
		return getPolygonString(poly, null, arg1, arg2);
	}

	public String getPolygonString (Polyline2D poly, List<? extends Polyline2D> innerPolys, String arg1, String arg2) {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN_POLYGON " + arg1 + " "
				+ arg2 + " 2");
		sb.append(XPlaneTranslatorImpl.LINE_SEP);
		sb.append(getWindingStr(poly.getVertices()));
		
		if (innerPolys != null && !innerPolys.isEmpty()) {
			for (Polyline2D polyline2d : innerPolys) {
				sb.append(getWindingStr(GeomUtils.forceCW((LinearRing2D) polyline2d).getVertices()));
			}
		}
		
		sb.append("END_POLYGON");
		sb.append(XPlaneTranslatorImpl.LINE_SEP);
		return sb.toString();
	}

	protected String getWindingStr(Collection<Point2D> vertices) {
		if (vertices instanceof List && vertices.size() > 1 && 
				((List<Point2D>) vertices).get(0).equals(((List<Point2D>) vertices).get(vertices.size() - 1))) {
			((List<Point2D>) vertices).remove(vertices.size() - 1);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN_WINDING");
		sb.append(XPlaneTranslatorImpl.LINE_SEP);
		for (Point2D loc : vertices) {
			sb.append(String.format(Locale.ROOT, "POLYGON_POINT %1.9f %2.9f", loc.x, loc.y));
			sb.append(XPlaneTranslatorImpl.LINE_SEP);
		}
		sb.append("END_WINDING");
		sb.append(XPlaneTranslatorImpl.LINE_SEP);
		return sb.toString();
	}

	public String getPolygonString(OsmPolygon osmPolygon, String arg1, String arg2) {
		if (osmPolygon instanceof OsmMultiPolygon) {
			return getPolygonString(osmPolygon.getPolygon(), ((OsmMultiPolygon) osmPolygon).getInnerPolys(), arg1, arg2);
		}
		return getPolygonString(osmPolygon.getPolygon(), arg1, arg2);
	}
	
	public String getPathStr(XPPathSegment pathSegment) {
		StringBuilder builder = new StringBuilder();
		if (XplaneOptionsHelper.getOptions().isGenerateComments() && pathSegment.getComment() != null) {
			builder.append("#");
			builder.append(pathSegment.getComment());
			builder.append(LINE_SEP);
		}
		Point2D[] points = pathSegment.getPoints();
		builder.append(String.format(Locale.ROOT, "BEGIN_SEGMENT 0 %d %d %3.9f %4.9f %5.9f", pathSegment.getType(), pathSegment.getStartId(), points[0].x, points[0].y, pathSegment.getStartHeight()));
		builder.append(LINE_SEP);
		for (int i = 1; i < points.length - 1; i++) {
			builder.append(String.format(Locale.ROOT, "SHAPE_POINT %1.9f %2.9f 0.000000000",points[i].x, points[i].y));
			builder.append(LINE_SEP);
		}
		builder.append(String.format(Locale.ROOT, "END_SEGMENT %d %2.9f %3.9f %4.9f", pathSegment.getEndId(), points[points.length - 1].x, points[points.length - 1].y, pathSegment.getEndHeight()));
		builder.append(LINE_SEP);
		return builder.toString();
	}
	
}
