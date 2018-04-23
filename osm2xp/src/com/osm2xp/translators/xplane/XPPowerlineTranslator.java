package com.osm2xp.translators.xplane;

import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

public class XPPowerlineTranslator extends XPPathTranslator {

	public XPPowerlineTranslator(IWriter writer) {
		super(writer);
	}

	@Override
	public boolean handlePoly(OsmPolygon osmPolygon) {
		if (!XplaneOptionsHelper.getOptions().isGeneratePowerlines()) {
			return false;
		}
		if ("line".equals(osmPolygon.getTagValue("power"))) {
			addSegmentsFrom(osmPolygon);
			return true; 
		}
		return false;
	}

	@Override
	protected int getPathType(OsmPolygon polygon) {
		return 220; //TODO using only one type for now
	}
	
	@Override
	protected String getComment(OsmPolygon poly) {
		return "power line";
	}

}
