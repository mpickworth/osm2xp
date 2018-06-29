package com.osm2xp.model.osm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.osm2xp.utils.GeomUtils;

import math.geom2d.Point2D;
import math.geom2d.polygon.LinearRing2D;
import math.geom2d.polygon.Polyline2D;

public abstract class OsmCurve {

	protected Long id;
	protected List<Tag> tags;
	protected List<Node> nodes;
	protected Integer height;
	protected Polyline2D curve;
	/**
	 * Whether only part of nodes is present for this.
	 * Can be OK to use for forest, road or powerline generation, but not OK for buildings
	 */
	protected boolean partial;
	private Boolean valid = null;

	public OsmCurve() {
		super();
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
	
	protected abstract void initCurve();

	/**
	 * Split current polygon along tiles
	 * 
	 * @return
	 */
	public List<OsmCurve> splitPolygonAroundTiles() {
		// if the polygon is on only one tile, return the current polygon
		if (isOnOneTile()) {
			return Collections.singletonList(this);
		} else {
			List<OsmCurve> result = new ArrayList<>();
			// the polygon is on more than one tile, split it.
			if (this.curve == null) {
				initCurve();
			}
			Map<Point2D, OsmPolygon> polygons = new HashMap<Point2D, OsmPolygon>();
			for (Point2D point : curve.getVertices()) {
				Point2D tilePoint = GeomUtils.cleanCoordinatePoint(point);
				if (polygons.get(tilePoint) == null) {
					polygons.put(tilePoint, new OsmPolygon(id, tags,
							new ArrayList<Node>(), false));
				}
				polygons.get(tilePoint).getNodes()
						.add(new Node(null, point.y, point.x, 1));
	
			}
	
			for (Map.Entry<Point2D, OsmPolygon> entry : polygons.entrySet()) {
				result.add(entry.getValue());
			}
			return result;
		}
	}

	public OsmCurve toSimplifiedPoly() { //Made this mutable since otherwise shapes was simplified even when it's not necessary - e.g. for forest
		if (this.curve != null) {
			OsmPolygon simplified = new OsmPolygon(id, Collections.unmodifiableList(tags), Collections.unmodifiableList(nodes), partial); 
			LinearRing2D result = GeomUtils.simplifyPolygon(this.curve);
			simplified.polygon = result;
			return simplified;
		} else {
			return this;
		}
	}

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
		if (this.getCurve() != null) {
			Double[] vectors = GeomUtils.computeExtremeVectors(curve);
			result = vectors[0];
		}
		return result;
	
	}

	public Double getMaxVectorSize() {
		Double result = null;
		if (this.getCurve() != null) {
			Double[] vectors = GeomUtils.computeExtremeVectors(curve);
			result = vectors[1];
		}
		return result;
	}

	public Boolean isSimplePolygon() {
		Boolean result = false;
		if (this.getCurve() != null) {
			result = (curve.getEdges().size() == 4 && GeomUtils
					.areParallelsSegmentsIdentics(curve));
		}
	
		return result;
	}

	public Point2D getCenter() {
		if (this.nodes.size() > 1) {
			this.center = GeomUtils.getPolygonCenter(curve);
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
	 * Whether all polygon nodes was read from source file
	 * Can be OK to generate road, railway, powerline or forest piece based on partial data,
	 * but not OK to generate building
	 * @return all polygon nodes was read from source file
	 */
	public boolean isPartial() {
		return partial;
	}

	public void setPartial(boolean partial) {
		this.partial = partial;
	}

	@Override
	public String toString() {
		return "OsmPolygon [id=" + id + ", tag=" + tags + "]";
	}

	public boolean isValid() {
		if (valid == null) {
			valid = GeomUtils.isValid(getCurve());
		}
		return valid;
	}

	public Polyline2D getCurve() {
		return curve;
	}

}