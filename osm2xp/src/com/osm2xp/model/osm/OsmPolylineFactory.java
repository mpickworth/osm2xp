package com.osm2xp.model.osm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.osm2xp.translators.xplane.IDGenerationService;
import com.osm2xp.utils.geometry.NodeCoordinate;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class OsmPolylineFactory {

	public static OsmPolyline createPolylineFrom(long id, List<Tag> tags, List<Node> nodes, boolean partial) {
		if (nodes.size() > 3 && nodes.get(0).getId() == nodes.get(nodes.size() - 1).getId()) {
			return new OsmPolygon(id, tags, nodes, partial);
		}
		return new OsmPolyline(id, tags, nodes, partial);
	}
	
	public static List<OsmPolyline> createPolylinesFromJTSGeometry(long id, List<Tag> tags, Geometry geometry) {
		if (geometry instanceof Polygon) {
			Coordinate[] outerCoords = ((Polygon) geometry).getExteriorRing().getCoordinates();
			List<Coordinate[]> innerCoords = new ArrayList<>();
			for (int i = 0; i < ((Polygon) geometry).getNumInteriorRing(); i++) {
				LineString interiorRingN = ((Polygon) geometry).getInteriorRingN(i);
				innerCoords.add(interiorRingN.getCoordinates());
			}
			List<Node> nodes = createRingNodes(outerCoords);
			if (nodes != null) {
				List<List<Node>> innerRings = innerCoords.stream().map(ring -> createRingNodes(ring)).filter(list -> list != null).collect(Collectors.toList());
				if (innerRings.size() > 0) {
					return Collections.singletonList(new OsmMultiPolygon(id, tags, nodes, innerRings, false));
				} else {
					return Collections.singletonList(new OsmPolygon(id, tags, nodes, false));
				}
			}
			return null;
		} else if (geometry instanceof LineString) {
			List<Node> nodes = createNodes(geometry.getCoordinates());
			if (nodes != null) {
				return Collections.singletonList(new OsmPolyline(id, tags, nodes, false));
			}
		} else if (geometry instanceof GeometryCollection) {
			List<OsmPolyline> resList = new ArrayList<OsmPolyline>();
			for (int i = 0; i < geometry.getNumGeometries(); i++) {
				List<OsmPolyline> polylines = createPolylinesFromJTSGeometry(id, tags, geometry.getGeometryN(i));
				if (polylines != null) {
					resList.addAll(polylines);
				}
			}
			return resList;
		}
		return null;
	}
	
	public static List<Node> createRingNodes(Coordinate[] coords) {
		if (coords.length < 4) {
			return null;
		}
		List<Node> resList = new ArrayList<Node>();
		for (int i = 0; i < coords.length; i++) {
			long newId;
			if (coords[i] instanceof NodeCoordinate) {
				newId = ((NodeCoordinate) coords[i]).getNodeId();
			} else if (i < coords.length - 1) {
				newId = IDGenerationService.getIncrementId();
			} else {
				newId = resList.get(0).getId();
			}
			resList.add(new Node(null, coords[i].y, coords[i].x, newId));
		}
		return resList;
	}
	
	public static List<Node> createNodes(Coordinate[] coords) {
		if (coords.length < 2) {
			return null;
		}
		List<Node> resList = new ArrayList<Node>();
		for (int i = 0; i < coords.length; i++) {
			long newId;
			if (coords[i] instanceof NodeCoordinate) {
				newId = ((NodeCoordinate) coords[i]).getNodeId();
			} else {
				newId = IDGenerationService.getIncrementId();
			}
			resList.add(new Node(null, coords[i].y, coords[i].x, newId));
		}
		return resList;
	}
}
