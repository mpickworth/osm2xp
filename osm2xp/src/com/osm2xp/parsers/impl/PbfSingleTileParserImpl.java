package com.osm2xp.parsers.impl;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Status;
import org.openstreetmap.osmosis.osmbinary.BinaryParser;
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
import com.osm2xp.model.osm.Nd;
import com.osm2xp.model.osm.OsmMultiPolygon;
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
		for (Relation pbfRelation : rels) {
//			if (pbfRelation.getId() == 1149353) { //XXX debug
//				System.out.println("PbfSingleTileParserImpl.parseRelations()");
//			}
			Map<String, String> tags = new HashMap<String, String>();
            for (int j = 0; j < pbfRelation.getKeysCount(); j++) {
                tags.put(getStringById(pbfRelation.getKeys(j)), getStringById(pbfRelation.getVals(j)));
            }
            
            long lastMemberId = 0;
            if ("multipolygon".equals(tags.get("type"))) {
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
	            List<List<Long>> collected = outerWays.stream().map(way -> way.getNd().stream().map(nd -> nd.getRef()).collect(Collectors.toList())).collect(Collectors.toList());
	            List<List<Long>> collectedInner = innerWays.stream().map(way -> way.getNd().stream().map(nd -> nd.getRef()).collect(Collectors.toList())).collect(Collectors.toList());
	            
	            List<List<Long>> polygons = getPolygonsFrom(collected);
	            List<List<Long>> innerPolygons = getPolygonsFrom(collectedInner);
	            // get nodes from translator
	            if (polygons.size() == 1 && innerPolygons.size() > 0) { //TODO for now support inner rings only if ther's one outer ring
	            	try {
						List<Tag> tagsModel = tags.keySet().stream().map(key -> new Tag(key, tags.get(key))).collect(Collectors.toList());
						List<com.osm2xp.model.osm.Node> outerNodes = processor.getNodes(polygons.get(0));
						if (outerNodes != null) {
							List<List<com.osm2xp.model.osm.Node>> innerNodes = innerPolygons.stream()
									.map(polyIds -> getNodes(polyIds))
									.filter(list -> list != null).collect(Collectors.toList());
							OsmMultiPolygon polygon = new OsmMultiPolygon(pbfRelation.getId(),
									tagsModel, outerNodes, innerNodes, outerNodes.size() < polygons.get(0).size());
							translator.processPolygon(polygon);
						}
					} catch (Exception e) {
						Activator.log(e);
					}
	            	
	            } else {
		            for (List<Long> polyNodeIds : polygons) {
		            	try {
							List<com.osm2xp.model.osm.Node> nodes = getNodes(polyNodeIds);
							
							if (nodes != null) {
								
								List<Tag> tagsModel = tags.keySet().stream().map(key -> new Tag(key, tags.get(key))).collect(Collectors.toList());
								OsmPolygon polygon = new OsmPolygon(pbfRelation.getId(),
										tagsModel, nodes, nodes.size() < polyNodeIds.size());
								translator.processPolygon(polygon);
							}
						} catch (Exception e) {
							Activator.log(e);
						}
					}
	            }
            }
		}
	}

	protected List<com.osm2xp.model.osm.Node> getNodes(List<Long> polyIds) {
		try {
			return processor.getNodes(polyIds);
		} catch (DataSinkException e) {
			Activator.log(e);
		}
		return null;
	}
	
	protected List<List<Long>> getPolygonsFrom(List<List<Long>> curves) {
		List<List<Long>> result = new ArrayList<List<Long>>();
		while (!curves.isEmpty()) {
			int matchIdx = 0;
			List<Long> current = new ArrayList<>();
			while (matchIdx > -1) {
				List<Long> segment = curves.get(matchIdx);
				if (!current.isEmpty()) {
					segment = segment.subList(1, segment.size());
				}
				current.addAll(segment);
				Long lastNodeId = current.get(current.size() - 1);
				curves.remove(matchIdx);
				matchIdx = -1;
				for (int i = 0; i < curves.size(); i++) {
					List<Long> curve = curves.get(i);
					if (!curve.isEmpty() && curve.get(0).equals(lastNodeId)) {
						matchIdx = i;
						break;
					}
				}
			}
			if (current.get(0).equals(current.get(current.size()-1))) {
				result.add(current);
			}
			
		}
		return result;
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

		for (Osmformat.Way curWay : ways) {
			List<Tag> listedTags = new ArrayList<Tag>();
			for (int j = 0; j < curWay.getKeysCount(); j++) {
				Tag tag = new Tag();
				tag.setKey(getStringById(curWay.getKeys(j)));
				tag.setValue(getStringById(curWay.getVals(j)));
				listedTags.add(tag);
			}

			long lastId = 0;
			List<Nd> listedLocalisationsRef = new ArrayList<Nd>();
			for (long j : curWay.getRefsList()) {
				Nd nd = new Nd();
				nd.setRef(j + lastId);
				listedLocalisationsRef.add(nd);
				lastId = j + lastId;
			}

			com.osm2xp.model.osm.Way way = new com.osm2xp.model.osm.Way();
			way.getTag().addAll(listedTags);
			way.setId(curWay.getId());
			way.getNd().addAll(listedLocalisationsRef);

			// if roof color information is available, add it to the current way
			if (this.roofsColorMap != null
					&& this.roofsColorMap.get(way.getId()) != null) {
				String hexColor = Integer.toHexString(this.roofsColorMap.get(
						way.getId()).getRGB() & 0x00ffffff);
				Tag roofColorTag = new Tag("building:roof:color", hexColor);
				way.getTag().add(roofColorTag);
			}
			
			processor.storeWay(way);

			try {
				List<Long> ids = new ArrayList<Long>();
				for (Nd nd : way.getNd()) {
					ids.add(nd.getRef());
				}
				// get nodes from translator
				List<com.osm2xp.model.osm.Node> nodes = getNodes(ids);

				if (nodes != null) {
					OsmPolygon polygon = new OsmPolygon(way.getId(),
							way.getTag(), nodes, nodes.size() < ids.size());
					translator.processPolygon(polygon);
				}

			} catch (Osm2xpBusinessException e) {
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
