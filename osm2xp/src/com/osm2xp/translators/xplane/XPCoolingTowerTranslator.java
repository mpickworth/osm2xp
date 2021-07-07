package com.osm2xp.translators.xplane;

import com.osm2xp.constants.Osm2xpConstants;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.utils.DsfObjectsProvider;
import com.osm2xp.utils.geometry.GeomUtils;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

public class XPCoolingTowerTranslator extends XPSpecObjectTranslator {
	
	private static final int MIN_TOWER_DIAMETER = 30;

	public XPCoolingTowerTranslator(IWriter writer, DsfObjectsProvider objectsProvider) {
		super(writer, objectsProvider);
	}

	@Override
	public void translationComplete() {
		// Do nothing
		
	}

	@Override
	protected boolean canProcess(OsmPolygon osmPolygon) {
		return XplaneOptionsHelper.getOptions().isGenerateCoolingTowers() &&
				("cooling_tower".equalsIgnoreCase(osmPolygon.getTagValue(Osm2xpConstants.MAN_MADE_TAG))
				|| "cooling".equalsIgnoreCase(osmPolygon.getTagValue("tower:type")));
	}

	@Override
	protected int getObjectSize(OsmPolygon osmPolygon) {
		double length = GeomUtils.computeEdgesLength(osmPolygon.getPolygon());
		int diameter = (int) Math.round(length / Math.PI);
		if (diameter < MIN_TOWER_DIAMETER) {
			return -1;
		}
		return diameter;
	}

	@Override
	protected String getObjectFilePreffix() {
		return "cooling_tower";
	}

	@Override
	protected boolean generationEnabled() {
		return XplaneOptionsHelper.getOptions().isGenerateCoolingTowers();
	}
	
	protected String getComment(OsmPolygon osmPolygon) {
		StringBuilder commentBuilder = new StringBuilder("#Cooling tower");	
		commentBuilder.append(System.getProperty("line.separator"));
		return commentBuilder.toString();
	}
	
}
