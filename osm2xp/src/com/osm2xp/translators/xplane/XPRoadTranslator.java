package com.osm2xp.translators.xplane;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.translators.IPolyHandler;
import com.osm2xp.utils.helpers.GuiOptionsHelper;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

public class XPRoadTranslator extends XPPathTranslator {
	
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
		if (ArrayUtils.contains(allowedHighwayTypes, poly.getTagValue("highway"))) {
			String surface = poly.getTagValue("surface"); //Generate if surface type is either missing or among allowe values
			if (StringUtils.stripToEmpty(surface).trim().isEmpty() || ArrayUtils.contains(allowedHighwaySurfaceTypes, surface)) {
				addSegmentsFrom(poly);
				return true;
			}
		}
		return false;
	}

	protected int getPathType(OsmPolygon poly) {
		// TODO Implement actual logics for this, using const value for now
		return 50;
	}
	
	@Override
	protected String getComment(OsmPolygon poly) {
		return "road";
	}


}
