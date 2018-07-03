package com.osm2xp.translators.xplane;

import com.osm2xp.model.osm.OsmPolyline;
import com.osm2xp.translators.impl.XPOutputFormat;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

public class XPPowerlineTranslator extends XPPathTranslator {

	public XPPowerlineTranslator(IWriter writer, XPOutputFormat outputFormat) {
		super(writer, outputFormat);
	}

	@Override
	public boolean handlePoly(OsmPolyline osmPolyline) {
		if (!XplaneOptionsHelper.getOptions().isGeneratePowerlines()) {
			return false;
		}
		if ("line".equals(osmPolyline.getTagValue("power"))) {
			addSegmentsFrom(osmPolyline);
			return true; 
		}
		return false;
	}

	@Override
	protected int getPathType(OsmPolyline polygon) {
		return 220; //TODO using only one type for now
	}
	
	@Override
	protected String getComment(OsmPolyline poly) {
		return "power line";
	}

	@Override
	protected boolean isBridge(OsmPolyline poly) {
		return false; //Not supported for power lines
	}
	
	@Override
	protected int getBridgeRampLength() {
		return 0;  //Not supported for power lines
	}
}
