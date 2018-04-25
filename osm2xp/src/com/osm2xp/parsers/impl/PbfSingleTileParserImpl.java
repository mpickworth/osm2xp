package com.osm2xp.parsers.impl;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.osmbinary.BinaryParser;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.openstreetmap.osmosis.osmbinary.Osmformat.*;
import org.openstreetmap.osmosis.osmbinary.file.BlockInputStream;

import com.osm2xp.dataProcessors.IDataSink;
import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.exceptions.OsmParsingException;
import com.osm2xp.model.osm.Nd;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.parsers.IParser;
import com.osm2xp.translators.ITranslator;
import com.osm2xp.utils.helpers.GuiOptionsHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;


/**
 * PBF parser implementation.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class PbfSingleTileParserImpl extends BinaryParser implements IParser {

	private File binaryFile;
	private ITranslator translator;
	private Map<Long, Color> roofsColorMap;
	private IDataSink processor;

	public void init(File binaryFile, ITranslator translator,
			Map<Long, Color> roofsColorMap, IDataSink processor) {

		this.binaryFile = binaryFile;
		this.translator = translator;
		this.roofsColorMap = roofsColorMap;
		this.processor = processor;

	}

	/**
	 * 
	 */
	public void complete() {
		translator.complete();
	}

	@Override
	protected void parseRelations(List<Relation> rels) {
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

					if (!GuiOptionsHelper.getOptions().isSinglePass()) {
						if (translator.mustStoreNode(node)) {
							processor.storeNode(node);
						}
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

		for (Osmformat.Way i : ways) {
			List<Tag> listedTags = new ArrayList<Tag>();
			for (int j = 0; j < i.getKeysCount(); j++) {
				Tag tag = new Tag();
				tag.setKey(getStringById(i.getKeys(j)));
				tag.setValue(getStringById(i.getVals(j)));
				listedTags.add(tag);
			}

			long lastId = 0;
			List<Nd> listedLocalisationsRef = new ArrayList<Nd>();
			for (long j : i.getRefsList()) {
				Nd nd = new Nd();
				nd.setRef(j + lastId);
				listedLocalisationsRef.add(nd);
				lastId = j + lastId;
			}

			com.osm2xp.model.osm.Way way = new com.osm2xp.model.osm.Way();
			way.getTag().addAll(listedTags);
			way.setId(i.getId());
			way.getNd().addAll(listedLocalisationsRef);

			// if roof color information is available, add it to the current way
			if (this.roofsColorMap != null
					&& this.roofsColorMap.get(way.getId()) != null) {
				String hexColor = Integer.toHexString(this.roofsColorMap.get(
						way.getId()).getRGB() & 0x00ffffff);
				Tag roofColorTag = new Tag("building:roof:color", hexColor);
				way.getTag().add(roofColorTag);
			}

			try {
				List<Long> ids = new ArrayList<Long>();
				for (Nd nd : way.getNd()) {
					ids.add(nd.getRef());
				}
				// get nodes from translator
				List<com.osm2xp.model.osm.Node> nodes = processor.getNodes(ids);

				if (nodes != null) {
					OsmPolygon polygon = new OsmPolygon(way.getId(),
							way.getTag(), nodes, nodes.size() < ids.size());
					translator.processPolygon(polygon);
				}

			} catch (Osm2xpBusinessException e) {
				Osm2xpLogger.error("Error processing way.", e);
			} catch (DataSinkException e) {
				Osm2xpLogger.error("Error processing way.", e);
			}
		}

	}

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

}
