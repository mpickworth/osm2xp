package com.osm2xp.translators.xplane;

import math.geom2d.Point2D;

public class XPPathSegment {
	
	private int type;
	private long startId;
	private long endId;
	private Point2D[] points;

	public XPPathSegment(int type, long startId, long endId, Point2D[] points) {
		if (points.length < 2) {
			throw new IllegalArgumentException("Need at least 2 points to generate road segment");
		}
		this.type = type;
		this.startId = startId;
		this.endId = endId;
		this.points = points;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("BEGIN_SEGMENT 0 %d %d %3.9f %4.9f 0.000000000\n", type, startId, points[0].y, points[0].x));
		for (int i = 1; i < points.length - 1; i++) {
			builder.append(String.format("SHAPE_POINT %1.9f %2.9f 0.000000000\n",points[i].y, points[i].x));	
		}
		builder.append(String.format("END_SEGMENT %d %2.9f %3.9f 0.000000000\n", endId, points[points.length - 1].y, points[points.length - 1].x));
		return builder.toString();
	}

	public Point2D getPoint(int i) {
		if (points.length <= i) {
			throw new IndexOutOfBoundsException("Point index out of bounds " + i);
		}
		return points[i];
	}

}
