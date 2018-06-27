package com.osm2xp.translators.xplane;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.osm2xp.model.osm.OsmMultiPolygon;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.model.stats.GenerationStats;
import com.osm2xp.translators.impl.XPlaneTranslatorImpl;
import com.osm2xp.utils.DsfObjectsProvider;
import com.osm2xp.utils.GeomUtils;
import com.osm2xp.utils.helpers.StatsHelper;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

import math.geom2d.Point2D;
import math.geom2d.polygon.LinearRing2D;

public class XPForestTranslator extends XPWritingTranslator {

	private DsfObjectsProvider dsfObjectsProvider;
	private GenerationStats stats;

	public XPForestTranslator(IWriter writer, DsfObjectsProvider dsfObjectsProvider, GenerationStats stats) {
		super(writer);
		this.dsfObjectsProvider = dsfObjectsProvider;
		this.stats = stats;
	}

	@Override
	public boolean handlePoly(OsmPolygon osmPolygon) {
		if (XplaneOptionsHelper.getOptions().isGenerateFor()) {
			Integer[] forestIndexAndDensity = dsfObjectsProvider
					.getRandomForestIndexAndDensity(osmPolygon.getTags());
			if (forestIndexAndDensity != null) {
				writeForestToDsf(osmPolygon, forestIndexAndDensity);
				return true;
			}
		}
		return false;
	}

	@Override
	public void translationComplete() {
		// Do nothing
	}
	
	/**
	 * @param polygon
	 *            the forest polygon
	 * @param forestIndexAndDensity
	 *            index and density of the forest rule
	 */
	private void writeForestToDsf(OsmPolygon osmPolygon, Integer[] forestIndexAndDensity) {
		StringBuffer sb = new StringBuffer();
		sb.append("BEGIN_POLYGON " + forestIndexAndDensity[0] + " "
				+ forestIndexAndDensity[1] + " 2");
		sb.append(XPlaneTranslatorImpl.LINE_SEP);
		writeWinding(osmPolygon.getPolygon().getVertices(), sb);
		
		if (osmPolygon instanceof OsmMultiPolygon) {
			List<LinearRing2D> innerPolys = ((OsmMultiPolygon) osmPolygon).getInnerPolys();
			if (innerPolys != null && !innerPolys.isEmpty()) {
				for (LinearRing2D linearRing2D : innerPolys) {
					writeWinding(linearRing2D.getVertices(), sb);
				}
			}
		}
		
		sb.append("END_POLYGON");
		sb.append(XPlaneTranslatorImpl.LINE_SEP);
	
		// stats
		StatsHelper.addForestType(
				dsfObjectsProvider.getPolygonsList().get(
						forestIndexAndDensity[0]), stats);
	
		writer.write(sb.toString(), GeomUtils.cleanCoordinatePoint(osmPolygon
				.getPolygon().getFirstPoint()));
	}

	protected void writeWinding(Collection<Point2D> vertices, StringBuffer sb) {
		sb.append("BEGIN_WINDING");
		sb.append(XPlaneTranslatorImpl.LINE_SEP);
		for (Point2D loc : vertices) {
			sb.append(String.format(Locale.ROOT, "POLYGON_POINT %1.9f %2.9f", loc.x, loc.y));
			sb.append(XPlaneTranslatorImpl.LINE_SEP);
		}
		sb.append("END_WINDING");
		sb.append(XPlaneTranslatorImpl.LINE_SEP);
	}

}
