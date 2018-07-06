package com.osm2xp.translators.xplane;

import static com.osm2xp.translators.impl.XPlaneTranslatorImpl.LINE_SEP;

import com.osm2xp.model.facades.SpecialFacadeType;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.model.osm.OsmPolyline;
import com.osm2xp.translators.impl.XPOutputFormat;
import com.osm2xp.utils.DsfObjectsProvider;
import com.osm2xp.utils.geometry.GeomUtils;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.writers.IWriter;

import math.geom2d.polygon.LinearRing2D;

public class XPBarrierTranslator extends XPWritingTranslator {

	private static final Double MIN_BARRIER_PERIMETER = 200.0; //TODO make configurable from UI
	private DsfObjectsProvider dsfObjectsProvider;
	private XPOutputFormat outputFormat;

	public XPBarrierTranslator(IWriter writer, DsfObjectsProvider dsfObjectsProvider, XPOutputFormat outputFormat) {
		super(writer);
		this.dsfObjectsProvider = dsfObjectsProvider;
		this.outputFormat = outputFormat;
	}

	@Override
	public boolean handlePoly(OsmPolyline osmPolyline) {
		if (!XplaneOptionsHelper.getOptions().isGenerateFence() || osmPolyline.isPartial()) {
			return false;
		}
		String barrierType = osmPolyline.getTagValue("barrier");
		if (barrierType != null && GeomUtils.computeEdgesLength(osmPolyline.getPolyline()) > MIN_BARRIER_PERIMETER && osmPolyline.isValid()) {
			Integer facade = dsfObjectsProvider.computeSpecialFacadeDsfIndex(getBarrierType(barrierType),osmPolyline);
			if (facade != null && facade >= 0) {
				StringBuffer sb = new StringBuffer();
				if (XplaneOptionsHelper.getOptions().isGenerateComments()) {
					sb.append("#Barrier " + barrierType + " facade " + facade);
					sb.append(LINE_SEP);
				}
				if (osmPolyline instanceof OsmPolygon) {
					((OsmPolygon)osmPolyline).setPolygon(GeomUtils.setCCW((LinearRing2D) osmPolyline
						.getPolyline()));
				}
				
				sb.append(outputFormat.getPolygonString(osmPolyline.getPolyline(), facade + "", "2")); //TODO need actual wall height here, using "2" for now
				writer.write(sb.toString(), GeomUtils
						.cleanCoordinatePoint(osmPolyline.getPolyline()
								.getFirstPoint()));
			}
			return true;
		}
		return false;
	}

	private SpecialFacadeType getBarrierType(String barrierTypeStr) {
		if ("wall".equalsIgnoreCase(barrierTypeStr)) {
			return SpecialFacadeType.WALL;
		}
		return SpecialFacadeType.FENCE;
	}

	@Override
	public void translationComplete() {
		// Do nothing
	}

}
