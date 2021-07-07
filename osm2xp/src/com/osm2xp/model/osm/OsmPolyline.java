package com.osm2xp.model.osm;

import java.util.List;
import java.util.Optional;

import com.osm2xp.utils.geometry.GeomUtils;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polyline2D;

public class OsmPolyline {

	protected Long id;
	protected List<Tag> tags;
	protected List<Node> nodes;
	protected Integer height;
	protected Polyline2D polyline;
	protected Point2D center;
	/**
	 * Indicates whether current polygon is a part of some complex polygon
	 */
	protected boolean part;
	protected Boolean valid = null;

	public OsmPolyline(long id, List<Tag> tags, List<Node> nodes, boolean part) {
		super();
		this.id = id;
		this.tags = tags;
		this.nodes = nodes;
		this.part = part;
	}

	public boolean isOnOneTile() {
		if (nodes == null || nodes.size() == 0) {
			return true;
		}
		Point2D original = GeomUtils.cleanCoordinatePoint(nodes.get(0).lat, nodes.get(0).lon);
		for (int i = 1; i < nodes.size(); i++) {
			if (!original.equals(GeomUtils.cleanCoordinatePoint(nodes.get(i).lat, nodes.get(i).lon))) {
				return false;
			}
		}
		return true;
	}
	
	protected void initCurve() {
		 this.polyline = GeomUtils.getPolylineFromOsmNodes(nodes);
	}

	/**
	 * Split current polygon along tiles
	 * 
	 * @return
	 */
//	public List<OsmPolyline> splitPolygonAroundTiles() {
//		// if the polygon is on only one tile, return the current polygon
//		if (isOnOneTile()) {
//			return Collections.singletonList(this);
//		} else {
//			List<OsmPolyline> result = new ArrayList<>();
//			// the polygon is on more than one tile, split it.
//			if (this.polyline == null) {
//				initCurve();
//			}
//			Map<Point2D, OsmPolygon> polygons = new HashMap<Point2D, OsmPolygon>();
//			for (Point2D point : polyline.getVertices()) {
//				Point2D tilePoint = GeomUtils.cleanCoordinatePoint(point);
//				if (polygons.get(tilePoint) == null) {
//					polygons.put(tilePoint, new OsmPolygon(id, tags,
//							new ArrayList<Node>(), false));
//				}
//				polygons.get(tilePoint).getNodes()
//						.add(new Node(null, point.y, point.x, 1));
//	
//			}
//	
//			for (Map.Entry<Point2D, OsmPolygon> entry : polygons.entrySet()) {
//				result.add(entry.getValue());
//			}
//			return result;
//		}
//	}

	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Tag> getTags() {
		return tags;
	}

	/**
	 * Return tag value for given key, of present. Return <code>null</code> otherwise
	 * @param tagKey tag key
	 * @return value for given key, of present, <code>null</code> otherwise
	 */
	public String getTagValue(String tagKey) {
		Optional<Tag> first = tags.stream().filter(tag -> tagKey.equals(tag.key)).findFirst();
		return first.isPresent() ? first.get().getValue() : null;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public Double getMinVectorSize() {
		Double result = null;
		if (this.getPolyline() != null) {
			Double[] vectors = GeomUtils.computeExtremeVectors(polyline);
			result = vectors[0];
		}
		return result;
	
	}

	public Double getMaxVectorSize() {
		Double result = null;
		if (this.getPolyline() != null) {
			Double[] vectors = GeomUtils.computeExtremeVectors(polyline);
			result = vectors[1];
		}
		return result;
	}

	public Point2D getCenter() {
		if (this.nodes.size() > 1) {
			this.center = GeomUtils.getPolylineCenter(polyline);
		} else {
			this.center = new Point2D(nodes.get(0).lon, nodes.get(0).lat);
		}
		return center;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	/**
	 * Whether current polygon is a part of some complex polygon
	 * In this case special logics can be applied for it - e.g. no simplification, using same facade/forest type for all parts etc. 
	 * @return <code>true</code> current polygon is a part of some complex polygon, <code>false</code> otherwise
	 */
	public boolean isPart() {
		return part;
	}

	public void setPart(boolean part) {
		this.part = part;
	}

	@Override
	public String toString() {
		return "OsmPolygon [id=" + id + ", tag=" + tags + "]";
	}

	public boolean isValid() {
		if (valid == null) {
			valid = GeomUtils.isValid(getPolyline());
		}
		return valid;
	}

	public Polyline2D getPolyline() {
		if (polyline == null) {
			initCurve();
		}
		return polyline;
	}

	public void setPolyline(Polyline2D polyline) {
		this.polyline = polyline;
	}

}