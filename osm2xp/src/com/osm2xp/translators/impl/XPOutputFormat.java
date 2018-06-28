package com.osm2xp.translators.impl;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.osm2xp.model.osm.OsmMultiPolygon;
import com.osm2xp.model.osm.OsmPolygon;

import math.geom2d.Point2D;
import math.geom2d.polygon.LinearRing2D;

public class XPOutputFormat {

	public String getPolygonString (OsmPolygon osmPolygon, String arg1, String arg2) {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN_POLYGON " + arg1 + " "
				+ arg2 + " 2");
		sb.append(XPlaneTranslatorImpl.LINE_SEP);
		sb.append(getWindingStr(osmPolygon.getPolygon().getVertices()));
		
		if (osmPolygon instanceof OsmMultiPolygon) {
			List<LinearRing2D> innerPolys = ((OsmMultiPolygon) osmPolygon).getInnerPolys();
			if (innerPolys != null && !innerPolys.isEmpty()) {
				for (LinearRing2D linearRing2D : innerPolys) {
					sb.append(getWindingStr(linearRing2D.getVertices()));
				}
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
	
}
