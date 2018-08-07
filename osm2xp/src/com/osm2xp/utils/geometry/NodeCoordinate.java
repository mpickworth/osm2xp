package com.osm2xp.utils.geometry;

import com.vividsolutions.jts.geom.Coordinate;

public class NodeCoordinate extends Coordinate {

	private static final long serialVersionUID = -639250971228091932L;
	private long nodeId;

	public NodeCoordinate(double x, double y, long nodeId) {
		super(x, y);
	}

	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}

}
