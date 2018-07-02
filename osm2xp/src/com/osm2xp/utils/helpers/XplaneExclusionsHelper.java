package com.osm2xp.utils.helpers;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.polygon.LinearRing2D;

import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.utils.GeomUtils;
import com.osm2xp.utils.logging.Osm2xpLogger;

public class XplaneExclusionsHelper extends Thread {

	private List<Box2D> exclusions = new ArrayList<Box2D>();
	private List<OsmPolygon> todoPolygons = new ArrayList<OsmPolygon>();

	public int getExclusionsSize() {
		return exclusions.size();
	}

	private void addBuildingToExclusions(OsmPolygon osmPolygon) {

		// check distance from other
		if (!exclusions.isEmpty()) {
			Boolean exclusionAdded = false;
			List<Box2D> footprintsToRemove = new ArrayList<Box2D>();
			List<Box2D> footprintsToAdd = new ArrayList<Box2D>();
			exclusionloop: for (Box2D footprint : exclusions) {
				Point2D centerA = GeomUtils.getPolylineCenter(new LinearRing2D(
						footprint.getVertices()));
				Point2D centerB = GeomUtils.getPolylineCenter(osmPolygon
						.getPolygon());
				double distanceBetweenPolygons = (centerA.distance(centerB)) * 1000;

				if (distanceBetweenPolygons < XplaneOptionsHelper.getOptions()
						.getSmartExclusionDistance()) {
					Box2D newFootprint = footprint.merge(new Box2D(osmPolygon
							.getPolygon().getAsAWTShape().getBounds2D()));

					footprintsToRemove.add(footprint);
					footprintsToAdd.add(newFootprint);
					exclusionAdded = true;
					break exclusionloop;
				}

			}
			if (!exclusionAdded) {
				exclusions.add(new Box2D(osmPolygon.getPolygon()
						.getAsAWTShape().getBounds2D()));
			}
			if (!footprintsToRemove.isEmpty()) {
				exclusions.removeAll(footprintsToRemove);
			}
			if (!footprintsToAdd.isEmpty()) {
				exclusions.addAll(footprintsToAdd);
			}
		} else {
			exclusions.add(new Box2D(osmPolygon.getPolygon().getAsAWTShape()
					.getBounds2D()));
		}
	}

	public void removeSmallExclusions() {
		List<Box2D> footprintsToRemove = new ArrayList<Box2D>();
		for (Box2D footprint : exclusions) {
			double area = (footprint.getHeight() * footprint.getWidth()) * 1000000;
			if (area < XplaneOptionsHelper.getOptions().getSmartExclusionSize()) {
				footprintsToRemove.add(footprint);
			}
		}
		exclusions.removeAll(footprintsToRemove);
	}

	public void optimiseExclusion() {
		// sort all exclusions based on size
		Comparator<Box2D> comparator = new Comparator<Box2D>() {

			@Override
			public int compare(Box2D box1, Box2D box2) {
				Double size1 = box1.getWidth() * box1.getHeight();
				Double size2 = box2.getWidth() * box2.getHeight();
				return size1.compareTo(size2);
			}
		};
		Collections.sort(exclusions, comparator);

		// optimize
		List<Box2D> footprintsToRemove = new ArrayList<Box2D>();
		List<Box2D> footprintsToAdd = new ArrayList<Box2D>();

		// FIXME so dirty... just for tests
		optimisationLoop: for (Box2D footprint : exclusions) {
			for (Box2D footprint2 : exclusions) {

				if (footprint != footprint2
						&& GeomUtils.boxContainsAnotherBox(footprint,
								footprint2)) {
					Box2D newFootprint = footprint.merge(footprint2);
					footprintsToAdd.add(newFootprint);
					footprintsToRemove.add(footprint2);
					footprintsToRemove.add(footprint);
					break optimisationLoop;
				}

			}
		}
		if (!footprintsToAdd.isEmpty()) {
			exclusions.removeAll(footprintsToRemove);
			exclusions.addAll(footprintsToAdd);
			optimiseExclusion();
		}
	}

	public String exportExclusions() {
		Osm2xpLogger.info("Obj exclusions size:" + exclusions.size()
				+ " ,starting optimisation.");
		removeSmallExclusions();
		optimiseExclusion();
		Osm2xpLogger.info("Optimisation done, Obj exclusions size:"
				+ exclusions.size() + ".");

		StringBuilder sbBuilder = new StringBuilder();
		DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(
				Locale.ENGLISH);
		NumberFormat numberFormat = new DecimalFormat(
				"#.############################################################",
				formatSymbols);

		for (Box2D exclusion : exclusions) {
			Double xMin = null;
			Double xMax = null;
			Double yMin = null;
			Double yMax = null;

			for (Point2D point : exclusion.getVertices()) {

				if (xMin == null || point.getX() < xMin) {
					xMin = point.getX();
				}

				if (yMin == null || point.getY() < yMin) {
					yMin = point.getY();
				}

				if (xMax == null || point.getX() > xMax) {
					xMax = point.getX();
				}

				if (yMax == null || point.getY() > yMax) {
					yMax = point.getY();
				}

			}

			sbBuilder.append("PROPERTY sim/exclude_obj "
					+ numberFormat.format(yMin) + "/"
					+ numberFormat.format(xMin) + "/"
					+ numberFormat.format(yMax) + "/"
					+ numberFormat.format(xMax) + "\n");

		}
		return sbBuilder.toString();
	}

	private void workExclusion() {
		List<OsmPolygon> polyToRemove = new ArrayList<OsmPolygon>();
		for (OsmPolygon polygon : todoPolygons) {
			addBuildingToExclusions(polygon);
			polyToRemove.add(polygon);
		}
		todoPolygons.removeAll(polyToRemove);
	}

	@Override
	public void run() {
		workExclusion();
	}

	public void addTodoPolygon(OsmPolygon polygon) {
		this.todoPolygons.add(polygon);
		// workExclusion();
		// addBuildingToExclusions(polygon);
	}
}
