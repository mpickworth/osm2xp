package com.osm2xp.translators.xplane;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.utils.helpers.GuiOptionsHelper;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

public class XPRoadTranslator extends XPPathTranslator {
	
	private static final String[] WIDE_ROAD_TYPES = {"motorway", "trunk", "primary", "secondary"}; 
	private static final String HIGHWAY_TAG = "highway";
	private String[] allowedHighwayTypes = GuiOptionsHelper.getAllowedHighwayTypes();
	private String[] allowedHighwaySurfaceTypes = GuiOptionsHelper.getAllowedHighwaySurfaceTypes();
	public XPRoadTranslator(IWriter writer) {
		super(writer);
	}

	@Override
	public boolean handlePoly(OsmPolygon poly) {
		if (!XplaneOptionsHelper.getOptions().isGenerateRoads()) {
			return false;
		}
		if (ArrayUtils.contains(allowedHighwayTypes, poly.getTagValue(HIGHWAY_TAG))) {
			String surface = poly.getTagValue("surface"); //Generate if surface type is either missing or among allowe values
			if (StringUtils.stripToEmpty(surface).trim().isEmpty() || ArrayUtils.contains(allowedHighwaySurfaceTypes, surface)) {
				addSegmentsFrom(poly);
				return true;
			}
		}
		return false;
	}

	/**
	 * Return X-Plane type constant for given road. Written in "hard" way to make some match between OSM tags and X-Plane type constants
	 * TODO Should me made configurable in future
	 * From https://forums.x-plane.org/index.php?/files/file/19074-roads-tutorial/ :
	 * 4 lanes - type 10
	 * 2 lanes - type 40
	 * 1 lane - type 50
	 */
	protected int getPathType(OsmPolygon poly) {
		String val = poly.getTagValue("lanes"); //Try to get lane count directly first
		if (val != null) {
			try {
				int value = Integer.parseInt(val.trim());
				if (value >= 4) {
					return 10;
				} else if (value >=2) {
					return 40;
				} else {
					return 50;
				}
			}catch (NumberFormatException e) {
				// ignore
			}
		}
//		String type = poly.getTagValue(HIGHWAY_TAG).toLowerCase(); //If no lane count - guess from the highway type
//		if (ArrayUtils.indexOf(WIDE_ROAD_TYPES, type) >= 0) {
//			return 40;
//		}
		if ("yes".equalsIgnoreCase(poly.getTagValue("oneWay")) && ArrayUtils.indexOf(WIDE_ROAD_TYPES, poly.getTagValue(HIGHWAY_TAG)) == -1) {
			return 50; //One-way road is treated as one-lane, if lane count is unspecified, and we doesn't have "wide" road	
		}
		return 40; //Use 2 lanes road by default
	}
	
	@Override
	protected String getComment(OsmPolygon poly) {
		return "road";
	}


}
