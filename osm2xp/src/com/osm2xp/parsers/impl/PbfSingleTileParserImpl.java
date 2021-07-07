package com.osm2xp.parsers.impl;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.geotools.geometry.jts.GeometryClipper;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.openstreetmap.osmosis.osmbinary.Osmformat.DenseInfo;
import org.openstreetmap.osmosis.osmbinary.Osmformat.DenseNodes;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBlock;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Node;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Way;
import org.openstreetmap.osmosis.osmbinary.file.BlockInputStream;

import com.osm2xp.dataProcessors.IDataSink;
import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.exceptions.OsmParsingException;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.parsers.IParser;
import com.osm2xp.translators.ITranslator;
import com.osm2xp.utils.geometry.GeomUtils;
import com.osm2xp.utils.helpers.GuiOptionsHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import math.geom2d.Point2D;


/**
 * PBF parser implementation.
 * {@link Deprecated} 
 * Use {@link MultiTileParserImpl} with single tile passed to it instead
 * 
 * @author Benjamin Blanchet
 * 
 */
@Deprecated
public class PbfSingleTileParserImpl extends TranslatingParserImpl implements IParser {

	private File binaryFile;
	private GeometryClipper tileClipper;
	private Envelope bounds; 
	
	public PbfSingleTileParserImpl(Point2D currentTile) {
		bounds = new Envelope(currentTile.x, currentTile.x + 1, currentTile.y, currentTile.y + 1);
		tileClipper = new GeometryClipper(bounds); //XXX need actual getting tile bounds instead  
	}

	public void init(File binaryFile, ITranslator translator,
			Map<Long, Color> roofsColorMap, IDataSink processor) {
		super.init(translator, roofsColorMap, processor);
		this.binaryFile = binaryFile;
	}

	/**
	 * 
	 */
	public void complete() {
		translator.complete();
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
//			if (di != null) {
				com.osm2xp.model.osm.Node node = new com.osm2xp.model.osm.Node();
				node.setId(id);
				node.setLat(latf);
				node.setLon(lonf);
				node.getTag().addAll(tags);
				try {
					// give the node to the translator for processing
					translator.processNode(node);
					// ask translator if we have to store this node if we
					// aren't on a single pass mode

					if (!GuiOptionsHelper.getOptions().isSinglePass() && translator.mustStoreNode(node)) {
						processor.storeNode(node);
					}
				} catch (DataSinkException e) {
					Osm2xpLogger.error("Error processing node.", e);
				} catch (Osm2xpBusinessException e) {
					Osm2xpLogger.error("Node translation error.", e);
				}
//			}
		}
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
	
//	@Override
//	protected void translateWay(com.osm2xp.model.osm.Way way, List<Long> ids) throws Osm2xpBusinessException {
//		Geometry geometry = getGeometry(ids);
//		if (geometry == null) {
//			return;
//		}
//		List<Geometry> fixed = fix(Collections.singletonList(geometry));
//		if (fixed.isEmpty()) {
//			return;
//		} else if (fixed.size() == 1 && fixed.get(0) == geometry) {
//			super.translateWay(way, ids);
//		} else {
//			fixed.stream()
//			.map(poly -> OsmPolylineFactory.createPolylinesFromJTSGeometry(way.getId(), way.getTag(),
//					poly))
//			.filter(list -> list != null).flatMap(list -> list.stream()).forEach(polyline -> {
//				try {
//					translator.processPolyline(polyline);
//				} catch (Osm2xpBusinessException e) {
//					Activator.log(e);
//				}
//			});
//		}
//	}

	@Override
	protected void parse(HeaderBlock header) {
		translator.processBoundingBox(header.getBbox());
	}

	public void process() throws OsmParsingException {

		try {
			translator.init();
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
	protected List<Polygon> doCleanup(List<List<Long>> outer, List<List<Long>> inner) {
		List<Polygon> cleaned = super.doCleanup(outer, inner);
		return fix(cleaned).stream().filter(geom -> geom instanceof Polygon).map(geom -> (Polygon)geom).collect(Collectors.toList());
	}
	
	protected List<Geometry> fix(List<? extends Geometry> geometries) {
		geometries = boundsFilter(geometries);
		if (geometries.isEmpty()) {
			return Collections.emptyList();
		}
		List<Geometry> fixed = geometries.stream().map(geom -> GeomUtils.fix(geom)).filter(geom -> geom != null).collect(Collectors.toList());
		fixed = clipToTileSize(fixed);
		return fixed;
	}
	
	protected List<Geometry> boundsFilter(List<? extends Geometry> geometries) {
		return geometries.stream().filter(geom -> geom.getEnvelopeInternal().intersects(bounds)).collect(Collectors.toList());
	}

	protected List<Geometry> clipToTileSize(List<? extends Geometry> geometries) {
		List<Geometry> resGeomList = new ArrayList<>();
		for (Geometry geometry : geometries) {
			Geometry clipResult = tileClipper.clip(geometry, true);
			resGeomList.addAll(GeomUtils.flatMap(clipResult));
		}
		return resGeomList;
	}

}
