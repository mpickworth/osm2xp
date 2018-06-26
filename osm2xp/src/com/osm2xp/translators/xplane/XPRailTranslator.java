package com.osm2xp.translators.xplane;

import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

public class XPRailTranslator extends XPPathTranslator {

	public XPRailTranslator(IWriter writer) {
		super(writer);
	}

	@Override
	public boolean handlePoly(OsmPolygon osmPolygon) {
		if (!XplaneOptionsHelper.getOptions().isGenerateRailways()) {
			return false;
		}
		if ("rail".equals(osmPolygon.getTagValue("railway"))) {
			addSegmentsFrom(osmPolygon);
			return true; 
		}
		return false;
	}

	@Override
	protected int getPathType(OsmPolygon polygon) {
		return 151; //TODO using only one type for now
	}
	
	@Override
	protected String getComment(OsmPolygon poly) {
		return "railway";
	}

	@Override
	protected int getBridgeRampLength() {
		return XplaneOptionsHelper.getOptions().getRailBridgeRampLen();
	}
}
