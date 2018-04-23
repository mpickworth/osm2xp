package com.osm2xp.translators.xplane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.osm2xp.model.osm.Node;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.utils.GeomUtils;
import com.osm2xp.writers.IWriter;

public abstract class XPPathTranslator extends XPWritingTranslator {

	private Set<Long> pathNodeIds = new HashSet<Long>();
	private Set<Long> pathCrossingIds = new HashSet<Long>();
	private List<OsmPolygon> pathPolys = new ArrayList<OsmPolygon>();
	
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
				writer.write(pathSegment.toString(), GeomUtils.cleanCoordinatePoint(pathSegment.getPoint(0)));
			}
		}
	}

	private List<XPPathSegment> getSegmentsFor(OsmPolygon poly) {
		List<XPPathSegment> result = new ArrayList<XPPathSegment>();
		List<Node> currentSegment = new ArrayList<Node>();
		List<Node> nodes = poly.getNodes();
		if (nodes.size() <= 1) {
			return Collections.emptyList();
		}
		for (int i = 0; i < nodes.size(); i++) {
			Node node = nodes.get(i);
			currentSegment.add(node);
			if ((currentSegment.size() > 1 && pathCrossingIds.contains(node.getId())) || i == nodes.size() - 1) {
				XPPathSegment segment = new XPPathSegment(getPathType(poly), 
						IDRenumbererService.getNewId(currentSegment.get(0).getId()), 
						IDRenumbererService.getNewId(node.getId()),
						GeomUtils.getPointsFromOsmNodes(currentSegment));
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