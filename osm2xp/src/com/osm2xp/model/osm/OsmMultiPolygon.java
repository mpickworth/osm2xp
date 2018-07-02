package com.osm2xp.model.osm;

import java.util.List;
import java.util.stream.Collectors;

import com.osm2xp.utils.GeomUtils;

import math.geom2d.polygon.LinearRing2D;

/**
 * {@link OsmPolygon} subclass representing multipolygon, e.g. area with holes in it
 * @author 32kda
 */
public class OsmMultiPolygon extends OsmPolygon {

	private List<List<Node>> innerPolyNodes;
	private List<LinearRing2D> innerPolys = null;

	public OsmMultiPolygon(long id, List<Tag> tags, List<Node> nodes, List<List<Node>> innerPolyNodes, boolean partial) {
		super(id, tags, nodes, partial);
		this.innerPolyNodes = innerPolyNodes;
	}

	public List<List<Node>> getInnerPolyNodes() {
		return innerPolyNodes;
	}
	
	public List<LinearRing2D> getInnerPolys() {
		if (innerPolys == null && innerPolyNodes != null) {
			innerPolys = innerPolyNodes.stream()
					.map(nodes -> GeomUtils.forceCCW(GeomUtils.getPolygonFromOsmNodes(GeomUtils.removeExtraEnd(nodes))))
					.collect(Collectors.toList());
		}
		return innerPolys;
	}
	
	@Override
	public Boolean isSimplePolygon() {
		if (innerPolyNodes != null && !innerPolyNodes.isEmpty()) {
			return false;
		}
		return super.isSimplePolygon();
	}
	
	

}
