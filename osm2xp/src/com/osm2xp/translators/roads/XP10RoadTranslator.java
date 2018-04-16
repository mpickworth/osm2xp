package com.osm2xp.translators.roads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.osm2xp.model.osm.Node;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.translators.IPolyHandler;
import com.osm2xp.utils.GeomUtils;
import com.osm2xp.utils.helpers.GuiOptionsHelper;
import com.osm2xp.writers.IWriter;

public class XP10RoadTranslator implements IPolyHandler {
	
	private String[] allowedHighwayTypes = GuiOptionsHelper.getAllowedHighwayTypes();
	private String[] allowedHighwaySurfaceTypes = GuiOptionsHelper.getAllowedHighwaySurfaceTypes();
	private Set<Long> roadNodeIds= new HashSet<Long>();
	private Set<Long> roadCrossingIds= new HashSet<Long>();
	private Map<Long, Integer> crossingRenumberMap = new HashMap<Long, Integer>(); //We need this map to get crossing ids starting from 1
	private List<OsmPolygon> roadPolys = new ArrayList<OsmPolygon>();
	private int renumberCounter = 1;
	
	private IWriter writer;

	public XP10RoadTranslator(IWriter writer) {
		this.writer = writer;
	}

	@Override
	public boolean handlePoly(OsmPolygon poly) {
		if (ArrayUtils.contains(allowedHighwayTypes, poly.getTagValue("highway"))) {
			String surface = poly.getTagValue("surface"); //Generate if surface type is either missing or among allowe values
			if (StringUtils.stripToEmpty(surface).trim().isEmpty() || ArrayUtils.contains(allowedHighwaySurfaceTypes, surface)) {
				poly.getNodes().stream().map(node -> node.getId()).forEach(id -> {
					if (roadNodeIds.contains(id)) {
						roadCrossingIds.add(id);
					}
					roadNodeIds.add(id);
				});
				roadPolys.add(poly);
			}
		}
		return false;
	}

	@Override
	public void translationComplete() {
		for (OsmPolygon poly : roadPolys) {
			List<XP10RoadSegment> segments = getSegmentsFor(poly);
			for (XP10RoadSegment roadSegment : segments) {
				writer.write(roadSegment.toString(), GeomUtils.cleanCoordinatePoint(roadSegment.getPoint(0)));
			}
		}
	}

	private List<XP10RoadSegment> getSegmentsFor(OsmPolygon poly) {
		List<XP10RoadSegment> result = new ArrayList<XP10RoadSegment>();
		List<Node> currentSegment = new ArrayList<Node>();
		List<Node> nodes = poly.getNodes();
		if (nodes.size() <= 1) {
			return Collections.emptyList();
		}
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			currentSegment.add(node);
			if ((currentSegment.size() > 1 && roadCrossingIds.contains(node.getId())) || i == nodes.size() - 1) {
				result.add(new XP10RoadSegment(getRoadType(poly), getCrossingId(currentSegment.get(0)), getCrossingId(node),
						GeomUtils.getPointsFromOsmNodes(currentSegment)));
				currentSegment.clear();
				if (i < nodes.size() - 1) {
					currentSegment.add(node);
				}
			}
		}		
		return result;
	}
	
	private int getCrossingId(Node node) {
		Integer newId = crossingRenumberMap.get(node.getId());
		if (newId == null) {
			newId = renumberCounter;
			renumberCounter++;
			crossingRenumberMap.put(node.getId(), newId);
		}
		return newId;
	}

	private int getRoadType(OsmPolygon poly) {
		// TODO Implement actual logics for this, using constvalue for now
		return 50;
	}

}
