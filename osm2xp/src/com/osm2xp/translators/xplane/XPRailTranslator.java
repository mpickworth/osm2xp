package com.osm2xp.translators.xplane;

import com.osm2xp.model.osm.OsmPolyline;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

public class XPRailTranslator extends XPPathTranslator {

	public XPRailTranslator(IWriter writer) {
		super(writer);
	}

	@Override
	public boolean handlePoly(OsmPolyline osmPolyline) {
		if (!XplaneOptionsHelper.getOptions().isGenerateRailways()) {
			return false;
		}
		if ("rail".equals(osmPolyline.getTagValue("railway"))) {
			addSegmentsFrom(osmPolyline);
			return true; 
		}
		return false;
	}

	@Override
	protected int getPathType(OsmPolyline polygon) {
		return 151; //TODO using only one type for now
	}
	
	@Override
	protected String getComment(OsmPolyline poly) {
		return "railway";
	}

	@Override
	protected int getBridgeRampLength() {
		return XplaneOptionsHelper.getOptions().getRailBridgeRampLen();
	}
}
