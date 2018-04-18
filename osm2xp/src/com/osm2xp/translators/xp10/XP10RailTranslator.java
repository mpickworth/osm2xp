package com.osm2xp.translators.xp10;

import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.writers.IWriter;

public class XP10RailTranslator extends XP10WritingTranslator {

	public XP10RailTranslator(IWriter writer) {
		super(writer);
	}

	@Override
	public boolean handlePoly(OsmPolygon osmPolygon) {
		if ("rail".equals(osmPolygon.getTagValue("railway"))) {
			
			return true; //TODO generate railway
		}
		return false;
	}

	@Override
	public void translationComplete() {
		// TODO Auto-generated method stub

	}

}
