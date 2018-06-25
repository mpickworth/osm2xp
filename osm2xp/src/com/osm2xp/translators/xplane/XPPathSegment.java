package com.osm2xp.translators.xplane;

import math.geom2d.Point2D;
import static com.osm2xp.translators.impl.XPlaneTranslatorImpl.LINE_SEP;

import java.util.Locale;

import com.osm2xp.utils.helpers.XplaneOptionsHelper;

public class XPPathSegment {
	
	private int type;
	private long startId;
	private long endId;
	private Point2D[] points;
	private double startHeight = 0;
	private double endHeight = 0;
	
	private String comment;
	private boolean bridge;

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
		if (XplaneOptionsHelper.getOptions().isGenerateComments() && getComment() != null) {
			builder.append("#");
			builder.append(getComment());
			builder.append(LINE_SEP);
		}
		builder.append(String.format(Locale.ROOT, "BEGIN_SEGMENT 0 %d %d %3.9f %4.9f %5.9f", type, startId, points[0].x, points[0].y, startHeight));
		builder.append(LINE_SEP);
		for (int i = 1; i < points.length - 1; i++) {
			builder.append(String.format(Locale.ROOT, "SHAPE_POINT %1.9f %2.9f 0.000000000",points[i].x, points[i].y));
			builder.append(LINE_SEP);
		}
		builder.append(String.format(Locale.ROOT, "END_SEGMENT %d %2.9f %3.9f %4.9f", endId, points[points.length - 1].x, points[points.length - 1].y, endHeight));
		builder.append(LINE_SEP);
		return builder.toString();
	}

	public Point2D getPoint(int i) {
		if (points.length <= i) {
			throw new IndexOutOfBoundsException("Point index out of bounds " + i);
		}
		return points[i];
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public double getStartHeight() {
		return startHeight;
	}

	public void setStartHeight(double startHeight) {
		this.startHeight = startHeight;
	}

	public double getEndHeight() {
		return endHeight;
	}

	public void setEndHeight(double endHeight) {
		this.endHeight = endHeight;
	}

	public void setBridge(boolean bridge) {
		this.bridge = bridge;
		if (bridge) {
			setStartHeight(1);
			setEndHeight(1);
		}
	}

	public boolean isBridge() {
		return bridge;
	}

	public long getStartId() {
		return startId;
	}

	public void setStartId(long startId) {
		this.startId = startId;
	}

	public long getEndId() {
		return endId;
	}

	public void setEndId(long endId) {
		this.endId = endId;
	}

	public Point2D[] getPoints() {
		return points;
	}

	public int getType() {
		return type;
	}

}
