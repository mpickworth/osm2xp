package com.osm2xp.translators.xplane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.osm2xp.model.osm.Node;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.utils.GeomUtils;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

public abstract class XPPathTranslator extends XPWritingTranslator {

	private Set<Long> pathNodeIds = new HashSet<Long>();
	private Set<Long> pathCrossingIds = new HashSet<Long>();
	private List<OsmPolygon> pathPolys = new ArrayList<OsmPolygon>();
	
	private Set<Integer> bridgeNodeIds = new HashSet<Integer>();
	
	public XPPathTranslator(IWriter writer) {
		super(writer);
	}

	protected void addSegmentsFrom(OsmPolygon poly) {
		poly.getNodes().stream().map(node -> node.getId()).forEach(id -> {
			if (pathNodeIds.contains(id)) {
				pathCrossingIds.add(id);
			}
			pathNodeIds.add(id);
		});
		pathPolys.add(poly);
	}

	@Override
	public void translationComplete() {
		for (OsmPolygon poly : pathPolys) {
			List<XPPathSegment> segments = getSegmentsFor(poly);
			for (XPPathSegment pathSegment : segments) {
				if (bridgeNodeIds.contains(new Integer((int) pathSegment.getStartId()))) {
					pathSegment.setStartHeight(1);
				}
				if (bridgeNodeIds.contains(new Integer((int) pathSegment.getEndId()))) {
					pathSegment.setEndHeight(1);
				}
				writer.write(pathSegment.toString(), GeomUtils.cleanCoordinatePoint(pathSegment.getPoint(0)));
			}
		}
	}

	private List<XPPathSegment> getSegmentsFor(OsmPolygon poly) {
		List<XPPathSegment> result = new ArrayList<XPPathSegment>();
		List<Node> currentSegment = new ArrayList<Node>();
		boolean bridge = isBridge(poly);
		List<Node> nodes = poly.getNodes();
		if (nodes.size() <= 1) {
			return Collections.emptyList();
		}
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			currentSegment.add(node);
//			boolean isTilesBorder = i < nodes.size() - 1 ? isDifferentTiles(node, nodes.get(i+1)) : false;
			if ((i == nodes.size() - 1) ||
				(currentSegment.size() > 1 && pathCrossingIds.contains(node.getId()))) {
				int newStartId = IDRenumbererService.getNewId(currentSegment.get(0).getId());
				int newEndId = IDRenumbererService.getNewId(node.getId());
				if (bridge) {
					bridgeNodeIds.add(newStartId);
					bridgeNodeIds.add(newEndId);
				}
				XPPathSegment segment = new XPPathSegment(getPathType(poly), 
						newStartId, 
						newEndId,
						GeomUtils.getPointsFromOsmNodes(currentSegment));
				segment.setBridge(bridge);
//				if (bridge || bridgeNodeIds.contains(newStartId)) {
//					segment.setStartHeight(1);
//				}
//				if (bridge || bridgeNodeIds.contains(newEndId)) {
//					segment.setEndHeight(1);
//				}
				segment.setComment(getComment(poly));
				result.add(segment);
				currentSegment.clear();
				if (i < nodes.size() - 1) {
					currentSegment.add(node);
				}
			}
		}		
		return result;
	}

	protected boolean isBridge(OsmPolygon poly) {
		return XplaneOptionsHelper.getOptions().isGenerateBridges() && !StringUtils.isEmpty(poly.getTagValue("bridge"));
	}

//	private boolean isDifferentTiles(Node node, Node nextNode) {
//		int latDiff = (int) (Math.floor(node.getLat()) - Math.floor(nextNode.getLat()));
//		int lonDiff = (int) (Math.floor(node.getLon()) - Math.floor(nextNode.getLon()));
//		return latDiff != 0 || lonDiff != 0;
//	}

	/**
	 * Get comment to be written into DSF file for this path/poly
	 * @param poly
	 * @return comment string, without "#' mark before. <code>null</code> by default, override if necessary
	 */
	protected String getComment(OsmPolygon poly) {
		return null;
	}

	protected abstract int getPathType(OsmPolygon polygon); 

}