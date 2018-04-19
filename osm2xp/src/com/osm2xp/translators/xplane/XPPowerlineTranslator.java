package com.osm2xp.translators.xplane;

import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.writers.IWriter;

public class XPPowerlineTranslator extends XPPathTranslator {

	public XPPowerlineTranslator(IWriter writer) {
		super(writer);
	}

	@Override
	public boolean handlePoly(OsmPolygon osmPolygon) {
		if ("line".equals(osmPolygon.getTagValue("power"))) {
			addSegmentsFrom(osmPolygon);
			return true; 
		}
		return false;
	}

	@Override
	protected int getRoadType(OsmPolygon polygon) {
		return 151; //TODO using only one type for now
	}

}
