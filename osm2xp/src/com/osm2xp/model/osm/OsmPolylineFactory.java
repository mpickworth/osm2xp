package com.osm2xp.model.osm;

import java.util.List;

public class OsmPolylineFactory {

	public static OsmPolyline createPolylineFrom(long id, List<Tag> tags, List<Node> nodes, boolean partial) {
		if (nodes.size() > 3 && nodes.get(0).getId() == nodes.get(nodes.size() - 1).getId()) {
			return new OsmPolygon(id, tags, nodes, partial);
		}
		return new OsmPolyline(id, tags, nodes, partial);
	}
	
}
