package com.osm2xp.parsers.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.geotools.geometry.jts.GeometryClipper;

import com.osm2xp.dataProcessors.IDataSink;
import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.gui.Activator;
import com.osm2xp.translators.ITranslator;
import com.osm2xp.utils.geometry.GeomUtils;
import com.osm2xp.utils.geometry.NodeCoordinate;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import math.geom2d.Point2D;

public class TileTranslationAdapter {

	private GeometryClipper tileClipper;
	private Envelope bounds;
	private IDataSink processor; 
	
	public TileTranslationAdapter(Point2D currentTile, IDataSink processor, ITranslator translator) {
		this.processor = processor;
		bounds = new Envelope(currentTile.x, currentTile.x + 1, currentTile.y, currentTile.y + 1);
		tileClipper = new GeometryClipper(bounds); //XXX need actual getting tile bounds instead  
	}
	
	protected List<Polygon> doCleanup(List<List<Long>> outer, List<List<Long>> inner) {
		GeometryFactory factory = new GeometryFactory(GeomUtils.getDefaultPrecisionModel());
		if (outer.size() == 1) { // If we have only one outer ring - assign all inner rings to it and return
			LinearRing[] innerRings = inner.stream().map(ids -> getRing(ids)).filter(ring -> ring != null)
					.toArray(LinearRing[]::new);
			return Collections.singletonList(factory.createPolygon(getRing(outer.get(0)), innerRings));
		} else if (inner.isEmpty()) { // If we have no inner rings - create poly for each outer ring and return these
										// polys
			return outer.stream().map(ids -> getPolygon(ids)).collect(Collectors.toList());
		}
		List<Polygon> outerPolysList = outer.stream().map(ids -> getPolygon(ids)).filter(poly -> poly != null)
				.collect(Collectors.toList());
		List<LinearRing> innerRingsList = inner.stream().map(ids -> getRing(ids)).filter(ring -> ring != null)
				.collect(Collectors.toList());
		List<Polygon> resultList = new ArrayList<Polygon>();
		for (Polygon outerPoly : outerPolysList) {
			List<LinearRing> innerRingList = new ArrayList<LinearRing>();
			for (Iterator<LinearRing> iterator = innerRingsList.iterator(); iterator.hasNext();) {
				LinearRing innerRing = iterator.next();
				if (outerPoly.covers(innerRing)) {
					innerRingList.add(innerRing);
					iterator.remove();
				}
			}
			resultList.add(factory.createPolygon(factory.createLinearRing(outerPoly.getCoordinates()),
					innerRingList.toArray(new LinearRing[0])));
		}
		return fix(resultList).stream().filter(geom -> geom instanceof Polygon).map(geom -> (Polygon)geom).collect(Collectors.toList());
	}
	
	protected List<Geometry> fix(List<? extends Geometry> geometries) {
		geometries = boundsFilter(geometries);
		if (geometries.isEmpty()) {
			return Collections.emptyList();
		}
		List<Geometry> fixed = geometries.stream().map(geom -> GeomUtils.fix(geom)).filter(geom -> geom != null).collect(Collectors.toList());
		fixed = clipToTileSize(fixed);
		return fixed;
	}
	
	protected List<Geometry> boundsFilter(List<? extends Geometry> geometries) {
		return geometries.stream().filter(geom -> geom.getEnvelopeInternal().intersects(bounds)).collect(Collectors.toList());
	}

	protected List<Geometry> clipToTileSize(List<? extends Geometry> geometries) {
		List<Geometry> resGeomList = new ArrayList<>();
		for (Geometry geometry : geometries) {
			Geometry clipResult = tileClipper.clip(geometry, true);
			resGeomList.addAll(GeomUtils.flatMap(clipResult));
		}
		return resGeomList;
	}
	

	protected Geometry getGeometry(List<Long> nodeIds) {
		if (isClosed(nodeIds)) {
			return getPolygon(nodeIds);
		}
		Coordinate[] points = getCoords(nodeIds);
		if (points != null && points.length >= 2) {
			GeometryFactory factory = new GeometryFactory(GeomUtils.getDefaultPrecisionModel());
			return factory.createLineString(points);
		}
		return null;	
	}

	protected Polygon getPolygon(List<Long> polyNodeIds) {
		Coordinate[] points = getCoords(polyNodeIds);
		if (points != null && points.length >= 4) {
			GeometryFactory factory = new GeometryFactory(GeomUtils.getDefaultPrecisionModel());
			return factory.createPolygon(points);
		}
		return null;
	}

	protected LinearRing getRing(List<Long> nodeIds) {
		Coordinate[] points = getCoords(nodeIds);
		if (points != null && points.length >= 4) {
			GeometryFactory factory = new GeometryFactory(GeomUtils.getDefaultPrecisionModel());
			return factory.createLinearRing(points);
		}
		return null;
	}

	protected Coordinate[] getCoords(List<Long> nodeIds) {
		List<com.osm2xp.model.osm.Node> nodes = getNodes(nodeIds);
		NodeCoordinate[] points = nodes.stream().map(node -> new NodeCoordinate(node.getLon(), node.getLat(), node.getId()))
				.toArray(NodeCoordinate[]::new);
		if (points.length < 1) {
			return null;
		}
		return points;
	}
	
	protected boolean isClosed(List<Long> curList) {
		if (curList.size() > 1) {
			return curList.get(0).equals(curList.get(curList.size() - 1));
		}
		return false;
	}
	
	protected List<com.osm2xp.model.osm.Node> getNodes(List<Long> polyIds) {
		try {
			return processor.getNodes(polyIds);
		} catch (DataSinkException e) {
			Activator.log(e);
		}
		return null;
	}


}
