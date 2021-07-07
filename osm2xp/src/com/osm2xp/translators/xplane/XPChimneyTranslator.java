package com.osm2xp.translators.xplane;

import com.osm2xp.constants.Osm2xpConstants;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.utils.DsfObjectsProvider;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

public class XPChimneyTranslator extends XPSpecObjectTranslator {
	
	public XPChimneyTranslator(IWriter writer, DsfObjectsProvider objectsProvider) {
		super(writer, objectsProvider);
	}

	@Override
	public void translationComplete() {
		// Do nothing
		
	}

	@Override
	protected boolean canProcess(OsmPolygon osmPolygon) {
		return XplaneOptionsHelper.getOptions().isGenerateChimneys() &&
				"chimney".equalsIgnoreCase(osmPolygon.getTagValue(Osm2xpConstants.MAN_MADE_TAG));
	}

	@Override
	protected int getObjectSize(OsmPolygon osmPolygon) {
		Integer height = osmPolygon.getHeight();
		if (height == null) {
			return 50; //Default value
		}
		return height;
	}

	@Override
	protected String getObjectFilePreffix() {
		return "chimney";
	}
	
	@Override
	protected boolean generationEnabled() {
		return XplaneOptionsHelper.getOptions().isGenerateChimneys();
	}
	
	protected String getComment(OsmPolygon osmPolygon) {
		StringBuilder commentBuilder = new StringBuilder("#Chimney");
		if (osmPolygon.getHeight() != null) {
			commentBuilder.append(" ");
			commentBuilder.append(osmPolygon.getHeight());
			commentBuilder.append("m");
		}
		commentBuilder.append(System.getProperty("line.separator"));
		return commentBuilder.toString();
	}
	
}
