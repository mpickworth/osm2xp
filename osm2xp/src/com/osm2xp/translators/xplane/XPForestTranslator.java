package com.osm2xp.translators.xplane;

import java.util.List;

import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.model.osm.OsmPolyline;
import com.osm2xp.model.stats.GenerationStats;
import com.osm2xp.translators.impl.XPOutputFormat;
import com.osm2xp.utils.DsfObjectsProvider;
import com.osm2xp.utils.GeomUtils;
import com.osm2xp.utils.helpers.StatsHelper;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

import math.geom2d.polygon.LinearRing2D;

public class XPForestTranslator extends XPWritingTranslator {

	private DsfObjectsProvider dsfObjectsProvider;
	private GenerationStats stats;
	private XPOutputFormat outputFormat;

	public XPForestTranslator(IWriter writer, DsfObjectsProvider dsfObjectsProvider, XPOutputFormat outputFormat, GenerationStats stats) {
		super(writer);
		this.dsfObjectsProvider = dsfObjectsProvider;
		this.outputFormat = outputFormat;
		this.stats = stats;
	}

	@Override
	public boolean handlePoly(OsmPolyline osmPolyline) {
		if (osmPolyline instanceof OsmPolygon && XplaneOptionsHelper.getOptions().isGenerateFor()) {
			Integer[] forestIndexAndDensity = dsfObjectsProvider
					.getRandomForestIndexAndDensity(osmPolyline.getTags());
			if (forestIndexAndDensity != null) {
				if (!osmPolyline.isValid()) {
					List<LinearRing2D> fixed = GeomUtils.validate((LinearRing2D)osmPolyline.getPolyline());
					for (LinearRing2D linearRing2D : fixed) {
						//TODO
						writeForestToDsf((OsmPolygon) osmPolyline, forestIndexAndDensity);
					}
				}
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
//		StringBuffer sb = new StringBuffer();
//		sb.append("BEGIN_POLYGON " + forestIndexAndDensity[0] + " "
//				+ forestIndexAndDensity[1] + " 2");
//		sb.append(XPlaneTranslatorImpl.LINE_SEP);
//		writeWinding(osmPolygon.getPolygon().getVertices(), sb);
//		
//		if (osmPolygon instanceof OsmMultiPolygon) {
//			List<LinearRing2D> innerPolys = ((OsmMultiPolygon) osmPolygon).getInnerPolys();
//			if (innerPolys != null && !innerPolys.isEmpty()) {
//				for (LinearRing2D linearRing2D : innerPolys) {
//					writeWinding(linearRing2D.getVertices(), sb);
//				}
//			}
//		}
//		
//		sb.append("END_POLYGON");
//		sb.append(XPlaneTranslatorImpl.LINE_SEP);
	
		writer.write(outputFormat.getPolygonString(osmPolygon, forestIndexAndDensity[0] + "", forestIndexAndDensity[1] + ""), GeomUtils.cleanCoordinatePoint(osmPolygon
				.getPolygon().getFirstPoint()));
		// stats
		StatsHelper.addForestType(
				dsfObjectsProvider.getPolygonsList().get(
						forestIndexAndDensity[0]), stats);
	
	}
//
//	protected void writeWinding(Collection<Point2D> vertices, StringBuffer sb) {
//		sb.append("BEGIN_WINDING");
//		sb.append(XPlaneTranslatorImpl.LINE_SEP);
//		for (Point2D loc : vertices) {
//			sb.append(String.format(Locale.ROOT, "POLYGON_POINT %1.9f %2.9f", loc.x, loc.y));
//			sb.append(XPlaneTranslatorImpl.LINE_SEP);
//		}
//		sb.append("END_WINDING");
//		sb.append(XPlaneTranslatorImpl.LINE_SEP);
//	}

}
