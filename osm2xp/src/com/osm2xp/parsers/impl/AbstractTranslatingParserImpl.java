package com.osm2xp.parsers.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Status;
import org.openstreetmap.osmosis.osmbinary.BinaryParser;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Relation;

import com.osm2xp.dataProcessors.IDataSink;
import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.gui.Activator;
import com.osm2xp.model.osm.Nd;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.model.osm.Way;
import com.osm2xp.utils.geometry.GeomUtils;
import com.osm2xp.utils.geometry.NodeCoordinate;
import com.osm2xp.utils.logging.Osm2xpLogger;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public abstract class AbstractTranslatingParserImpl extends BinaryParser {

	protected IDataSink processor;
	protected Map<Long, Color> roofsColorMap;

	public AbstractTranslatingParserImpl() {
		super();
	}

	public AbstractTranslatingParserImpl(Map<Long, Color> roofsColorMap, IDataSink processor) {
		this.roofsColorMap = roofsColorMap;
		this.processor = processor;
	}

	protected boolean isClosed(List<Long> curList) {
		if (curList.size() > 1) {
			return curList.get(0).equals(curList.get(curList.size() - 1));
		}
		return false;
	}

	protected List<List<Long>> getPolygonsFrom(List<List<Long>> curves) {
		List<List<Long>> result = new ArrayList<List<Long>>();
		for (Iterator<List<Long>> iterator = curves.iterator(); iterator.hasNext();) {
			List<Long> curList = (List<Long>) iterator.next();
			if (isClosed(curList)) { // If some way forms closed contour - remove it without further analysis
				result.add(curList);
				iterator.remove();
			}
		}
		while (!curves.isEmpty()) {
			List<Long> segment = curves.remove(0);
			List<Long> current = new ArrayList<>();
			while (segment != null) {
				if (!current.isEmpty()) {
					segment = segment.subList(1, segment.size());
				}
				current.addAll(segment);
				segment = null;
				Long lastNodeId = current.get(current.size() - 1);
				for (int i = 0; i < curves.size(); i++) {
					List<Long> curve = curves.get(i);
					if (!curve.isEmpty()) { 
						if (curve.get(0).equals(lastNodeId)) {
							segment = curves.remove(i);
							break;
						} else if (curve.get(curve.size() - 1).equals(lastNodeId)) {
							segment = curves.remove(i);							
							Collections.reverse(segment);
							break;
						}
					}
				}
			}
			if (current.get(0).equals(current.get(current.size() - 1))) {
				result.add(current);
			}
	
		}
		return result;
	}

	protected List<Geometry> fix(List<? extends Geometry> geometries) {
		return geometries.stream().map(geom -> GeomUtils.fix(geom)).filter(geom -> geom != null).collect(Collectors.toList());
	}

	protected List<com.osm2xp.model.osm.Node> getNodes(List<Long> polyIds) {
		try {
			return processor.getNodes(polyIds);
		} catch (DataSinkException e) {
			Activator.log(e);
		}
		return null;
	}

	protected com.osm2xp.model.osm.Way createWayFromParsed(Osmformat.Way curWay) {
		List<Tag> listedTags = new ArrayList<Tag>();
		for (int j = 0; j < curWay.getKeysCount(); j++) {
			Tag tag = new Tag();
			tag.setKey(getStringById(curWay.getKeys(j)));
			tag.setValue(getStringById(curWay.getVals(j)));
			listedTags.add(tag);
		}
	
		long lastId = 0;
		List<Nd> listedLocalisationsRef = new ArrayList<Nd>();
		for (long j : curWay.getRefsList()) {
			Nd nd = new Nd();
			nd.setRef(j + lastId);
			listedLocalisationsRef.add(nd);
			lastId = j + lastId;
		}
	
		com.osm2xp.model.osm.Way way = new com.osm2xp.model.osm.Way();
		way.getTag().addAll(listedTags);
		way.setId(curWay.getId());
		way.getNd().addAll(listedLocalisationsRef);
		return way;
	}

	@Override
	protected void parseRelations(List<Relation> rels) {
			for (Relation pbfRelation : rels) {
				Map<String, String> tags = new HashMap<String, String>();
				for (int j = 0; j < pbfRelation.getKeysCount(); j++) {
					tags.put(getStringById(pbfRelation.getKeys(j)), getStringById(pbfRelation.getVals(j)));
				}
				long lastMemberId = 0;
				List<Tag> tagsModel = tags.keySet().stream().map(key -> new Tag(key, tags.get(key)))
						.collect(Collectors.toList());
				if ("multipolygon".equals(tags.get("type")) && mustProcessPolyline(tagsModel)) {
					List<com.osm2xp.model.osm.Way> outerWays = new ArrayList<>();
					List<com.osm2xp.model.osm.Way> innerWays = new ArrayList<>();
					for (int j = 0; j < pbfRelation.getMemidsCount(); j++) {
						long memberId = lastMemberId + pbfRelation.getMemids(j);
						lastMemberId = memberId;
						String role = getStringById(pbfRelation.getRolesSid(j));
						if ("outer".equals(role)) {
							com.osm2xp.model.osm.Way way = processor.getWay(memberId);
							if (way != null) {
								outerWays.add(way);
							} else {
								Activator.log(Status.ERROR, "Invalid way id: " + memberId);
							}
						}
						if ("inner".equals(role)) {
							com.osm2xp.model.osm.Way way = processor.getWay(memberId);
							if (way != null) {
								innerWays.add(way);
							} else {
								Activator.log(Status.ERROR, "Invalid way id: " + memberId);
							}
						}
					}
					List<List<Long>> collected = outerWays.stream()
							.map(way -> way.getNd().stream().map(nd -> nd.getRef()).collect(Collectors.toList()))
							.collect(Collectors.toList());
					List<List<Long>> collectedInner = innerWays.stream()
							.map(way -> way.getNd().stream().map(nd -> nd.getRef()).collect(Collectors.toList()))
							.collect(Collectors.toList());
	
					List<List<Long>> polygons = getPolygonsFrom(collected);
					List<List<Long>> innerPolygons = getPolygonsFrom(collectedInner);
					List<Polygon> cleanedPolys = doCleanup(polygons, innerPolygons);
					translatePolys(pbfRelation.getId(), tagsModel, cleanedPolys);
				}
			}
		}

	protected abstract boolean mustProcessPolyline(List<Tag> tagsModel);

	protected abstract void translatePolys(long id, List<Tag> tagsModel, List<Polygon> cleanedPolys);

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
		return resultList;
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

	protected void processWay(Osmformat.Way curWay) {
		Way way = createWayFromParsed(curWay);
	
		// if roof color information is available, add it to the current way
		if (this.roofsColorMap != null && this.roofsColorMap.get(way.getId()) != null) {
			String hexColor = Integer.toHexString(this.roofsColorMap.get(way.getId()).getRGB() & 0x00ffffff);
			Tag roofColorTag = new Tag("building:roof:color", hexColor);
			way.getTag().add(roofColorTag);
		}
	
		processor.storeWay(way);
		
		if (!mustProcessPolyline(way.getTag())) {
			return;
		}
	
		try {
			List<Long> ids = new ArrayList<Long>();
			for (Nd nd : way.getNd()) {
				ids.add(nd.getRef());
			}
			
			translateWay(way, ids);
	
		} catch (Osm2xpBusinessException e) {
			Osm2xpLogger.error("Error processing way.", e);
		}
	}

	protected abstract void translateWay(Way way, List<Long> ids) throws Osm2xpBusinessException;

}