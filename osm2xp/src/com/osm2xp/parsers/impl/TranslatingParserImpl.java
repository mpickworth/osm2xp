package com.osm2xp.parsers.impl;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.osm2xp.dataProcessors.IDataSink;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.gui.Activator;
import com.osm2xp.model.osm.OsmPolyline;
import com.osm2xp.model.osm.OsmPolylineFactory;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.translators.ITranslator;
import com.osm2xp.utils.geometry.CoordinateNodeIdPreserver;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public abstract class TranslatingParserImpl extends AbstractTranslatingParserImpl {

	protected ITranslator translator;

	public TranslatingParserImpl() {
		super();
	}

	public void init(ITranslator translator, Map<Long, Color> roofsColorMap, IDataSink processor) {
		this.processor = processor;
		this.roofsColorMap = roofsColorMap;
		this.translator = translator;
	}

	protected void translateWay(com.osm2xp.model.osm.Way way, List<Long> ids) throws Osm2xpBusinessException {
		Geometry geometry = getGeometry(ids);
		if (geometry == null) {
			return;
		}
		List<? extends Geometry> fixed = fix(Collections.singletonList(geometry));
		if (fixed.isEmpty()) {
			return;
		} else if (fixed.size() == 1 && fixed.get(0) == geometry) {
			List<com.osm2xp.model.osm.Node> nodes = getNodes(ids);                                            
            
			if (nodes != null) {                                                                              
				OsmPolyline polyline = OsmPolylineFactory.createPolylineFrom(way.getId(), way.getTag(), nodes,
						nodes.size() < ids.size());                                                           
				translator.processPolyline(polyline);                                                         
			}                                                                                                 
		} else {
			fixed = CoordinateNodeIdPreserver.preserveNodeIds(Collections.singletonList(geometry), fixed);
			fixed.stream()
			.map(poly -> OsmPolylineFactory.createPolylinesFromJTSGeometry(way.getId(), way.getTag(),
					poly))
			.filter(list -> list != null).flatMap(list -> list.stream()).forEach(polyline -> {
				try {
					translator.processPolyline(polyline);
				} catch (Osm2xpBusinessException e) {
					Activator.log(e);
				}
			});
		}
	}
	
	@Override
	protected void translatePolys(long id, List<Tag> tagsModel, List<Polygon> cleanedPolys) {
		cleanedPolys.stream()
		.map(poly -> OsmPolylineFactory.createPolylinesFromJTSGeometry(id, tagsModel,
				poly))
		.filter(list -> list != null).flatMap(list -> list.stream()).forEach(polyline -> {
			try {
				translator.processPolyline(polyline);
			} catch (Osm2xpBusinessException e) {
				Activator.log(e);
			}
		});

	}


//	protected void translateWay(Way way, List<Long> ids) throws Osm2xpBusinessException {
//		// get nodes from translator
//		List<com.osm2xp.model.osm.Node> nodes = getNodes(ids);
//
//		if (nodes != null) {
//			OsmPolyline polyline = OsmPolylineFactory.createPolylineFrom(way.getId(), way.getTag(), nodes,
//					nodes.size() < ids.size());
//			translator.processPolyline(polyline);
//		}
//	}

	protected boolean mustProcessPolyline(List<Tag> tagsModel) {
		return translator.mustProcessPolyline(tagsModel);
	}
}