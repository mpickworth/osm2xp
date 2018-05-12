package com.osm2xp.translators.xplane;

import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.utils.DsfObjectsProvider;
import com.osm2xp.utils.GeomUtils;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

import math.geom2d.Point2D;

public class XPChimneyTranslator extends XPWritingTranslator {

	protected DsfObjectsProvider objectsProvider;
	
	public XPChimneyTranslator(IWriter writer, DsfObjectsProvider objectsProvider) {
		super(writer);
		this.objectsProvider = objectsProvider;
	}

	@Override
	public boolean handlePoly(OsmPolygon osmPolygon) {
		if (XplaneOptionsHelper.getOptions().isGenerateChimneys() &&
				"chimney".equalsIgnoreCase(osmPolygon.getTagValue("man_made"))) {
			Integer chimneyObjectIdx = objectsProvider.getChimneyObject(osmPolygon.getHeight());
			Point2D center = osmPolygon.getCenter();
			String objStr =  String.format("OBJECT %1d %2.9f %3.9f 0" + System.getProperty("line.separator"), chimneyObjectIdx, center.x, center.y);
			Point2D point = GeomUtils.cleanCoordinatePoint(osmPolygon.getCenter());
			StringBuilder commentBuilder = new StringBuilder("#Chimney");
			if (osmPolygon.getHeight() != null) {
				commentBuilder.append(" ");
				commentBuilder.append(osmPolygon.getHeight());
				commentBuilder.append("m");
			}
			commentBuilder.append(System.getProperty("line.separator"));
			writer.write(commentBuilder.toString(), point);
			writer.write(objStr, point);
			return true;
		}
		return false;
	}

	@Override
	public void translationComplete() {
		// Do nothing
		
	}
	
}
