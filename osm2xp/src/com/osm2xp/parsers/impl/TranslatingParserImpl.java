package com.osm2xp.parsers.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Status;
import org.openstreetmap.osmosis.osmbinary.BinaryParser;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.openstreetmap.osmosis.osmbinary.Osmformat.Relation;

import com.osm2xp.dataProcessors.IDataSink;
import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.gui.Activator;
import com.osm2xp.model.osm.Nd;
import com.osm2xp.model.osm.OsmMultiPolygon;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.model.osm.OsmPolyline;
import com.osm2xp.model.osm.OsmPolylineFactory;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.model.osm.Way;
import com.osm2xp.translators.ITranslator;
import com.osm2xp.utils.logging.Osm2xpLogger;

public abstract class TranslatingParserImpl extends BinaryParser {

	protected IDataSink processor;
	protected Map<Long, Color> roofsColorMap;
	protected ITranslator translator;

	public TranslatingParserImpl() {
		super();
	}

	public void init(ITranslator translator, Map<Long, Color> roofsColorMap, IDataSink processor) {
		this.processor = processor;
		this.roofsColorMap = roofsColorMap;
		this.translator = translator;
	}

	protected boolean isClosed(List<Long> curList) {
		if (curList.size() > 1) {
			return curList.get(0).equals(curList.get(curList.size() - 1));
		}
		return false;
	}

	protected List<List<Long>> getPolygonsFrom(List<List<Long>> curves) {
		List<List<Long>> result = new ArrayList<List<Long>>();
		for (Iterator<List<Long>> iterator = curves.iterator(); iterator.hasNext();) {
			List<Long> curList = (List<Long>) iterator.next();
			if (isClosed(curList)) { //If some way forms closed contour - remove it without further analysis
				result.add(curList);
				iterator.remove();
			}
		}
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

	protected void processWay(Osmformat.Way curWay) {
		Way way = createWayFromParsed(curWay);
	
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
				OsmPolyline polyline = OsmPolylineFactory.createPolylineFrom(way.getId(),
						way.getTag(), nodes, nodes.size() < ids.size());
				translator.processPolyline(polyline);
			}
	
		} catch (Osm2xpBusinessException e) {
			Osm2xpLogger.error("Error processing way.", e);
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

	protected com.osm2xp.model.osm.Way createWayFromParsed(Osmformat.Way curWay) {
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
		return way;
	}

	@Override
	protected void parseRelations(List<Relation> rels) {
			for (Relation pbfRelation : rels) {
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
		            if (polygons.size() == 1 && innerPolygons.size() > 0) { //TODO for now support inner rings only if there's one outer ring
		            	try {
							List<Tag> tagsModel = tags.keySet().stream().map(key -> new Tag(key, tags.get(key))).collect(Collectors.toList());
							List<com.osm2xp.model.osm.Node> outerNodes = processor.getNodes(polygons.get(0));
							if (outerNodes != null) {
								List<List<com.osm2xp.model.osm.Node>> innerNodes = innerPolygons.stream()
										.map(polyIds -> getNodes(polyIds))
										.filter(list -> list != null).collect(Collectors.toList());
								OsmMultiPolygon polygon = new OsmMultiPolygon(pbfRelation.getId(),
										tagsModel, outerNodes, innerNodes, outerNodes.size() < polygons.get(0).size());
								if (polygon.isOnOneTile() && !polygon.isPartial()) { //TODO splitting multipolygon can give variety of results and isn't supported yet
									translator.processPolyline(polygon);
								}
							}
						} catch (Exception e) {
							Activator.log(e);
						}
		            	
		            } else { 
			            for (List<Long> polyNodeIds : polygons) {
			            	try {
								List<com.osm2xp.model.osm.Node> nodes = getNodes(polyNodeIds);
								
								if (nodes != null && nodes.size() > 3) {
									
									List<Tag> tagsModel = tags.keySet().stream().map(key -> new Tag(key, tags.get(key))).collect(Collectors.toList());
									OsmPolygon polygon = new OsmPolygon(pbfRelation.getId(),
											tagsModel, nodes, nodes.size() < polyNodeIds.size());
									translator.processPolyline(polygon);
								}
							} catch (Exception e) {
								Activator.log(e);
							}
						}
		            }
	            }
			}
		}

}