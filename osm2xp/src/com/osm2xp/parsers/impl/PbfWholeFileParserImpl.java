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
import com.osm2xp.model.osm.Nd;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.parsers.IParser;
import com.osm2xp.translators.ITranslator;
import com.osm2xp.utils.helpers.GuiOptionsHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * Protobuff parser Single pass implementation. (Stream whole file once to store
 * usefull nodes then stream another time to store ways for those nodes).
 * 
 * @author Benjamin Blanchet.
 * 
 */
public class PbfWholeFileParserImpl extends TranslatingParserImpl implements IParser {

	private File binaryFile;
	private boolean nodesRefCollectionDone;

	public void init(File binaryFile, ITranslator translator,
			Map<Long, Color> roofsColorMap, IDataSink processor) {
		super.init(translator, roofsColorMap, processor);
		this.binaryFile = binaryFile;

	}

	/**
	 * 
	 */
	public void complete() {
		if (!nodesRefCollectionDone) {
			nodesRefCollectionDone = true;
			try {
				Osm2xpLogger.info("First pass done, "
						+ processor.getNodesNumber()
						+ " nodes are needed to generate scenery");
				process();
			} catch (OsmParsingException e) {
				Osm2xpLogger.error(e.getMessage());
			}
		} else {
			translator.complete();
		}

	}

	@Override
	protected void parseDense(DenseNodes nodes) {
		// parse nodes only if we're not on a single pass mode, or if the nodes
		// collection of single pass mode is done
		if (this.nodesRefCollectionDone) {
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
//				if (di != null) {
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
						// if we're on a single pass mode, and if
						// nodesRefCollectionDone is true it means we already
						// have the reference of usefull nodes, so we check if
						// this node is one of them, if yes, store it
						else {
							if (processor.getNode(node.getId()) != null) {
								processor.storeNode(node);
							}

						}
					} catch (Osm2xpBusinessException e) {
						Osm2xpLogger.error("Error processing node.", e);
					} catch (DataSinkException e) {
						Osm2xpLogger.error("Error processing node.", e);
					}
//				}
			}
		}
	}

	@Override
	protected void parseNodes(List<Node> nodes) {
	}

	private void sendWaysToTranslator(List<Way> ways) {
		for (Osmformat.Way curWay : ways) {
			processWay(curWay);
		}
	}

	private void checkWaysForUsefullNodes(List<Way> ways) {
		for (Osmformat.Way curWay : ways) {
			com.osm2xp.model.osm.Way way = createWayFromParsed(curWay);
			
			processor.storeWay(way);
			
			try {
				List<Long> ids = new ArrayList<Long>();
				for (Nd nd : way.getNd()) {
					ids.add(nd.getRef());
				}
				if (translator.mustStoreWay(way)) {
					for (Nd nd : way.getNd()) {
						com.osm2xp.model.osm.Node node = new com.osm2xp.model.osm.Node();
						node.setId(nd.getRef());
						node.setLat(0);
						node.setLon(0);
						processor.storeNode(node);
					}
				}

			} catch (DataSinkException e) {
				Osm2xpLogger.error("Error processing way.", e);
			}
		}
	}

	@Override
	protected void parseWays(List<Way> ways) {
		// if we're not on a single pass mode, send ways to translator
		if (nodesRefCollectionDone) {
			sendWaysToTranslator(ways);
		} else {
			checkWaysForUsefullNodes(ways);
		}

	}

	@Override
	protected void parse(HeaderBlock header) {
		translator.processBoundingBox(header.getBbox());
	}

	public void process() throws OsmParsingException {

		InputStream input;
		try {
			translator.init();
			input = new FileInputStream(this.binaryFile);
			BlockInputStream bm = new BlockInputStream(input, this);
			bm.process();
		} catch (FileNotFoundException e1) {
			throw new OsmParsingException("Error loading file "
					+ binaryFile.getPath(), e1);
		} catch (IOException e) {
			Osm2xpLogger.error(e.getMessage());
		}

	}

}
