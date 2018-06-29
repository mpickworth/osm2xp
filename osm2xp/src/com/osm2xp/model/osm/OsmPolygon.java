package com.osm2xp.model.osm;

import java.awt.Color;
import java.util.List;

import com.osm2xp.utils.GeomUtils;
import com.osm2xp.utils.OsmUtils;

import math.geom2d.Point2D;
import math.geom2d.polygon.LinearRing2D;

/**
 * OsmPolygon.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class OsmPolygon extends OsmCurve {

	private Double area;
	Point2D center;
	public OsmPolygon(long id, List<Tag> tags, List<Node> nodes, boolean partial) {
		super();
		this.id = id;
		this.tags = tags;
		this.nodes = nodes;
		this.partial = partial;
		this.height = OsmUtils.getHeightFromTags(tags);

	}

	public OsmPolygon() {

	}
	
//	public void simplifyPolygon() {
//		if (this.polygon != null) {
//			LinearRing2D result = GeomUtils.simplifyPolygon(this.polygon);
//			this.polygon = result;
//		}
//	}

	public Double getArea() {
		if (nodes.size() > 2) {
			this.area = ((LinearRing2D) curve).getArea();
		} else {
			this.area = 0D;
		}
		return area;
	}

	public LinearRing2D getPolygon() {
		if (this.curve == null) {
			initCurve();
		}
		return (LinearRing2D) curve;
	}

	public void setPolygon(LinearRing2D polygon) {
		this.curve = polygon;
	}

	public Color getRoofColor() {
		return OsmUtils.getRoofColorFromTags(this.tags);
	}

	@Override
	protected void initCurve() {
		curve = GeomUtils.getPolygonFromOsmNodes(nodes);
	}


}
