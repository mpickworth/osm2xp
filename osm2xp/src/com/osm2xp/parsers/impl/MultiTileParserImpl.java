package com.osm2xp.parsers.impl;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Status;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.openstreetmap.osmosis.osmbinary.Osmformat.DenseInfo;
import org.openstreetmap.osmosis.osmbinary.Osmformat.DenseNodes;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBlock;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Node;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Relation;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Way;
import org.openstreetmap.osmosis.osmbinary.file.BlockInputStream;

import com.osm2xp.dataProcessors.IDataSink;
import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.exceptions.OsmParsingException;
import com.osm2xp.gui.Activator;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.parsers.IBasicParser;
import com.osm2xp.utils.geometry.GeomUtils;
import com.osm2xp.utils.logging.Osm2xpLogger;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;


/**
 * PBF parser with ability to split parsed data around several polygons using JTS to clip, cut, simplify and fix OSM polygons 
 * 
 * @author Dmitry Karpenko, OnPositive
 * 
 */
public class MultiTileParserImpl extends AbstractTranslatingParserImpl implements IBasicParser {

	private File binaryFile;
	private List<TileTranslationAdapter> translationAdapters;
	
	public MultiTileParserImpl(File binaryFile, List<TileTranslationAdapter> traanslationAdapters, Map<Long, Color> roofsColorMap, IDataSink processor) {
		super(roofsColorMap, processor);
		this.binaryFile = binaryFile;
		this.translationAdapters = traanslationAdapters;
	}

	/**
	 * 
	 */
	public void complete() {
		for (TileTranslationAdapter tileTranslationAdapter : translationAdapters) {
			tileTranslationAdapter.complete();
		}
	}

	@Override
	protected void parseDense(DenseNodes nodes) {
		// parse nodes only if we're not on a single pass mode, or if the nodes
		// collection of single pass mode is done

		long lastId = 0, lastLat = 0, lastLon = 0;
		int j = 0;
		DenseInfo di = null;
		if (nodes.hasDenseinfo()) {
			di = nodes.getDenseinfo();
		}
		for (int i = 0; i < nodes.getIdCount(); i++) {
			List<Tag> tags = new ArrayList<Tag>();
			long lat = nodes.getLat(i) + lastLat;
			lastLat = lat;
			long lon = nodes.getLon(i) + lastLon;
			lastLon = lon;
			long id = nodes.getId(i) + lastId;
			lastId = id;
			double latf = parseLat(lat), lonf = parseLon(lon);
			if (nodes.getKeysValsCount() > 0) {
				while (nodes.getKeysVals(j) != 0) {
					int keyid = nodes.getKeysVals(j++);
					int valid = nodes.getKeysVals(j++);
					Tag tag = new Tag();
					tag.setKey(getStringById(keyid));
					tag.setValue(getStringById(valid));
					tags.add(tag);
				}
				j++;
			}
			com.osm2xp.model.osm.Node node = new com.osm2xp.model.osm.Node();
			node.setId(id);
			node.setLat(latf);
			node.setLon(lonf);
			node.getTag().addAll(tags);
			try {
				// give the node to the translator for processing
				for (TileTranslationAdapter adapter : translationAdapters) {
					adapter.processNode(node);
				}
				// ask translator if we have to store this node if we
				// aren't on a single pass mode

				if (mustStoreNode(node)) {
					processor.storeNode(node);
				}
			} catch (DataSinkException e) {
				Osm2xpLogger.error("Error processing node.", e);
			} catch (Osm2xpBusinessException e) {
				Osm2xpLogger.error("Node translation error.", e);
			}
		}
	}

	private boolean mustStoreNode(com.osm2xp.model.osm.Node node) {
		for (TileTranslationAdapter tileTranslationAdapter : translationAdapters) {
			if (tileTranslationAdapter.mustStoreNode(node)) {
				return true;		
			}
		}
		return false;
	}

	@Override
	protected void parseNodes(List<Node> nodes) {
	}

	@Override
	protected void parseWays(List<Way> ways) {

		for (Osmformat.Way curWay : ways) {
			processWay(curWay);
		}

	}
	
	protected void translateWay(com.osm2xp.model.osm.Way way, List<Long> ids) throws Osm2xpBusinessException {
		Geometry geometry = getGeometry(ids);
		if (geometry == null) {
			return;
		}
		List<Geometry> fixed = fix(Collections.singletonList(geometry));
		for (TileTranslationAdapter adapter : translationAdapters) {
			adapter.processWays(way.getId(), way.getTag(), geometry, fixed);
		}
		if (fixed.isEmpty()) {
			return;
		} 
	}
	
	@Override
	protected void parse(HeaderBlock header) {
		for (TileTranslationAdapter tileTranslationAdapter : translationAdapters) {
			tileTranslationAdapter.processBoundingBox(header.getBbox());
		}
	}

	public void process() throws OsmParsingException {

		try {
			for (TileTranslationAdapter tileTranslationAdapter : translationAdapters) {
				tileTranslationAdapter.init();
			}
			InputStream input;
			input = new FileInputStream(this.binaryFile);
			BlockInputStream bm = new BlockInputStream(input, this);
			bm.process();
		} catch (FileNotFoundException e1) {
			throw new OsmParsingException("Error loading file "
					+ binaryFile.getPath(), e1);
		} catch (IOException e) {
			throw new OsmParsingException(e);
		}

	}
	
	@Override
	protected void parseRelations(List<Relation> rels) {
		for (Relation pbfRelation : rels) {
			Map<String, String> tags = new HashMap<String, String>();
			for (int j = 0; j < pbfRelation.getKeysCount(); j++) {
				tags.put(getStringById(pbfRelation.getKeys(j)), getStringById(pbfRelation.getVals(j)));
			}
			long lastMemberId = 0;
			List<Tag> tagsModel = tags.keySet().stream().map(key -> new Tag(key, tags.get(key)))
					.collect(Collectors.toList());
			if ("multipolygon".equals(tags.get("type")) && mustProcessPolyline(tagsModel)) {
				List<com.osm2xp.model.osm.Way> outerWays = new ArrayList<>();
				List<com.osm2xp.model.osm.Way> innerWays = new ArrayList<>();
				for (int j = 0; j < pbfRelation.getMemidsCount(); j++) {
					long memberId = lastMemberId + pbfRelation.getMemids(j);
					lastMemberId = memberId;
					String role = getStringById(pbfRelation.getRolesSid(j));
					if ("outer".equals(role)) {
						com.osm2xp.model.osm.Way way = processor.getWay(memberId);
						if (way != null) {
							outerWays.add(way);
						} else {
							Activator.log(Status.ERROR, "Invalid way id: " + memberId);
						}
					}
					if ("inner".equals(role)) {
						com.osm2xp.model.osm.Way way = processor.getWay(memberId);
						if (way != null) {
							innerWays.add(way);
						} else {
							Activator.log(Status.ERROR, "Invalid way id: " + memberId);
						}
					}
				}
				List<List<Long>> collected = outerWays.stream()
						.map(way -> way.getNd().stream().map(nd -> nd.getRef()).collect(Collectors.toList()))
						.collect(Collectors.toList());
				List<List<Long>> collectedInner = innerWays.stream()
						.map(way -> way.getNd().stream().map(nd -> nd.getRef()).collect(Collectors.toList()))
						.collect(Collectors.toList());

				List<List<Long>> polygons = getPolygonsFrom(collected);
				List<List<Long>> innerPolygons = getPolygonsFrom(collectedInner);
				List<Polygon> cleanedPolys = doCleanup(polygons, innerPolygons);
				translatePolys(pbfRelation.getId(), tagsModel, cleanedPolys);
			}
		}
	}
	
	@Override
	protected List<Polygon> doCleanup(List<List<Long>> outer, List<List<Long>> inner) {
		List<Polygon> cleaned = super.doCleanup(outer, inner);
		return fix(cleaned).stream().filter(geom -> geom instanceof Polygon).map(geom -> (Polygon)geom).collect(Collectors.toList());
	}
	
	protected List<Geometry> fix(List<? extends Geometry> geometries) {
		return geometries.stream().map(geom -> GeomUtils.fix(geom)).filter(geom -> geom != null).collect(Collectors.toList());
	}

	@Override
	protected boolean mustProcessPolyline(List<Tag> tagsModel) {
		for (TileTranslationAdapter adapter : translationAdapters) {
			if (adapter.mustProcessPolyline(tagsModel)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void translatePolys(long id, List<Tag> tagsModel, List<Polygon> cleanedPolys) {
		for (TileTranslationAdapter adapter : translationAdapters) {
			adapter.processWays(id, tagsModel, null, cleanedPolys);
		}
	}

}
