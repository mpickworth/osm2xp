package com.osm2xp.translators.xplane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.osm2xp.model.osm.Node;
import com.osm2xp.model.osm.OsmPolyline;
import com.osm2xp.utils.GeomUtils;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

import math.geom2d.Point2D;

public abstract class XPPathTranslator extends XPWritingTranslator {

	private Set<Long> pathNodeIds = new HashSet<Long>();
	private Set<Long> pathCrossingIds = new HashSet<Long>();
	private List<OsmPolyline> pathPolys = new ArrayList<>();
	
	private Set<Integer> bridgeNodeIds = new HashSet<Integer>();
	
	public XPPathTranslator(IWriter writer) {
		super(writer);
	}

	protected void addSegmentsFrom(OsmPolyline poly) {
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
		List<XPPathSegment> segmentList = new ArrayList<XPPathSegment>();
		for (OsmPolyline poly : pathPolys) {
			segmentList.addAll(getSegmentsFor(poly));
		}
		List <XPPathSegment> resultSegmentList = segmentList;
		if (!bridgeNodeIds.isEmpty()) {
			resultSegmentList = new ArrayList<>(segmentList.size());
			for (XPPathSegment pathSegment : segmentList) {
				if (bridgeNodeIds.contains(new Integer((int) pathSegment.getStartId()))) {
					pathSegment.setStartHeight(1);
				}
				if (bridgeNodeIds.contains(new Integer((int) pathSegment.getEndId()))) {
					pathSegment.setEndHeight(1);
				}
				if (!pathSegment.isBridge() && 
					(pathSegment.getStartHeight() > 0 || pathSegment.getEndHeight() > 0)) {
					resultSegmentList.addAll(splitIfNecessary(pathSegment)); 
				} else {
					resultSegmentList.add(pathSegment);
				}
			}
		}
		for (XPPathSegment pathSegment : resultSegmentList) {
			writer.write(pathSegment.toString(), GeomUtils.cleanCoordinatePoint(pathSegment.getPoint(0)));
		}
		
	}
	
	private Collection<? extends XPPathSegment> splitIfNecessary(XPPathSegment pathSegment) {
		int minLength = getBridgeRampLength();
		if (minLength > 0) {
			Point2D[] points = pathSegment.getPoints();
			double length = GeomUtils.computeLengthInMeters(points);
			boolean needStartEntrance = pathSegment.getStartHeight() > 0;
			boolean needEndEntrance = pathSegment.getEndHeight() > 0;		
			if ((needStartEntrance && needEndEntrance && length > minLength * 2) ||
			   ((needStartEntrance != needEndEntrance) && length > minLength)) {
				XPPathSegment startSegment = null, endSegment = null;
				if (needStartEntrance) {
					double distance = 0;
					for (int i = 1; i < points.length; i++) {
						Double curLen = GeomUtils.latLongDistance(points[i-1].y,
								points[i-1].x, points[i].y,points[i].x);
						if (distance + curLen > minLength) {
							double newSegLen = minLength - distance;
							double k = newSegLen / curLen;
							double newX = points[i-1].x + (points[i].x-points[i-1].x)*k;
							double newY = points[i-1].y + (points[i].y-points[i-1].y)*k;
							Point2D[] newSegPts = Arrays.copyOf(points, i + 1);
							newSegPts[i] = new Point2D(newX, newY);
							int newPointId = IDRenumbererService.getIncrementId();
							startSegment = new XPPathSegment(pathSegment.getType(), pathSegment.getStartId(), newPointId, newSegPts);
							startSegment.setStartHeight(1);
							Point2D[] tailPts = Arrays.copyOfRange(points, i, points.length);
							tailPts = (Point2D[]) ArrayUtils.add(tailPts, 0, new Point2D(newX, newY));
							pathSegment = new XPPathSegment(pathSegment.getType(), newPointId, pathSegment.getEndId(), tailPts); //Leave a tail of original segment
							break;
						}
						distance += curLen;
					}
				}
				if (needEndEntrance) {
					points = pathSegment.getPoints();
					double distance = 0;
					for (int i = points.length - 2; i >= 0; i--) {
						Double curLen = GeomUtils.latLongDistance(points[i+1].y,
								points[i+1].x, points[i].y,points[i].x);
						if (distance + curLen > minLength) {
							double newSegLen = minLength - distance;
							double k = newSegLen / curLen;
							double newX = points[i+1].x + (points[i].x-points[i+1].x)*k;
							double newY = points[i+1].y + (points[i].y-points[i+1].y)*k;
							Point2D[] newSegPts = Arrays.copyOf(points, i + 2);
							newSegPts[i+1] = new Point2D(newX, newY);
							long originalEndId = pathSegment.getEndId();
							int newPointId = IDRenumbererService.getIncrementId();
							pathSegment = new XPPathSegment(pathSegment.getType(), pathSegment.getStartId(), newPointId, newSegPts);
							Point2D[] tailPts = Arrays.copyOfRange(points, i+1, points.length); //was i 
							tailPts = (Point2D[]) ArrayUtils.add(tailPts, 0, new Point2D(newX, newY));
							endSegment = new XPPathSegment(pathSegment.getType(), newPointId, originalEndId, tailPts); //Leave a tail of original segment
							endSegment.setEndHeight(1);
							break;
						}
						distance += curLen;
					}
				}
				List<XPPathSegment> resList = new ArrayList<XPPathSegment>();
				if (startSegment != null) {
					resList.add(startSegment);
				}
				resList.add(pathSegment);
				if (endSegment != null) {
					resList.add(endSegment);
				}
				return resList;
			}
		}
		return Collections.singletonList(pathSegment);
	}

	/**
	 * @return Min bridge *entrance* segment length 
	 */
	protected int getBridgeRampLength() {
		return 100;
	}

	private List<XPPathSegment> getSegmentsFor(OsmPolyline poly) {
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

	protected boolean isBridge(OsmPolyline poly) {
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
	protected String getComment(OsmPolyline poly) {
		return null;
	}

	protected abstract int getPathType(OsmPolyline polygon); 

}