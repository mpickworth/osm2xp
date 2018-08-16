package com.osm2xp.translators.xplane;

import java.util.List;

import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.model.osm.OsmPolyline;
import com.osm2xp.model.stats.GenerationStats;
import com.osm2xp.translators.impl.XPOutputFormat;
import com.osm2xp.utils.DsfObjectsProvider;
import com.osm2xp.utils.geometry.GeomUtils;
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
					List<LinearRing2D> fixed = GeomUtils.fix((LinearRing2D)osmPolyline.getPolyline());
					for (LinearRing2D linearRing2D : fixed) {
						writer.write(outputFormat.getPolygonString(linearRing2D, forestIndexAndDensity[0] + "", forestIndexAndDensity[1] + ""));
					}
				} else {
					writeForestToDsf((OsmPolygon) osmPolyline, forestIndexAndDensity);
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
	
		writer.write(outputFormat.getPolygonString(osmPolygon, forestIndexAndDensity[0] + "", forestIndexAndDensity[1] + ""));
		// stats
		StatsHelper.addForestType(
				dsfObjectsProvider.getPolygonsList().get(
						forestIndexAndDensity[0]), stats);
	
	}

}
