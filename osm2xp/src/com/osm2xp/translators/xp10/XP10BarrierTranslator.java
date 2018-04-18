package com.osm2xp.translators.xp10;

import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.utils.GeomUtils;
import com.osm2xp.writers.IWriter;

public class XP10BarrierTranslator extends XP10WritingTranslator {

	private static final Double MIN_BARRIER_PERIMETER = 200.0; //TODO make configurable from UI

	public XP10BarrierTranslator(IWriter writer) {
		super(writer);
	}

	@Override
	public boolean handlePoly(OsmPolygon osmPolygon) {
		String barrierType = osmPolygon.getTagValue("barrier");
		if (barrierType != null && GeomUtils.computePerimeter(osmPolygon.getPolygon()) > MIN_BARRIER_PERIMETER) {
			//TODO generate fence
		}
		return false;
	}

	@Override
	public void translationComplete() {
		// TODO Auto-generated method stub

	}

}
