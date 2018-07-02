package com.osm2xp.parsers.impl;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.osmbinary.Osmformat.Relation;

import com.bbn.openmap.layer.shape.ESRIPoly.ESRIFloatPoly;
import com.bbn.openmap.layer.shape.ESRIPolygonRecord;
import com.bbn.openmap.layer.shape.ShapeFile;
import com.osm2xp.dataProcessors.IDataSink;
import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.exceptions.OsmParsingException;
import com.osm2xp.model.osm.Nd;
import com.osm2xp.model.osm.Node;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.model.osm.Way;
import com.osm2xp.parsers.IParser;
import com.osm2xp.translators.ITranslator;
import com.osm2xp.utils.helpers.GuiOptionsHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * Shapefile parser implementation.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class ShapefileParserImpl implements IParser {
	private ShapeFile shapeFile;
	private ITranslator translator;
	private int nodeIndex = 1;
	private int wayIndex = 1;
	private IDataSink processor;

	@Override
	public void init(File file, ITranslator translator,
			Map<Long, Color> roofsColorMap, IDataSink processor) {
		try {
			this.shapeFile = new ShapeFile(file);
			this.translator = translator;
		} catch (IOException e) {
			Osm2xpLogger.error("Error loading shapefile file", e);
		}

	}

	@Override
	public void process() throws OsmParsingException {
		try {
			translator.init();
			ESRIPolygonRecord esriPolygonRecord = (ESRIPolygonRecord) shapeFile
					.getNextRecord();

			while (esriPolygonRecord != null) {

				ESRIFloatPoly poly = (ESRIFloatPoly) esriPolygonRecord.polygons[0];
				Way way = new Way();
				way.getTag().add(GuiOptionsHelper.getShapefileTag());
				way.setId(++wayIndex);

				for (int i = 0; i < poly.getRadians().length - 1; i = i + 2) {

					Node node = new Node(null,
							Math.toDegrees(poly.getRadians()[i]),
							Math.toDegrees(poly.getRadians()[i + 1]), nodeIndex);

					// add the node ref to the way
					way.getNd().add(new Nd(nodeIndex));
					// send the node to the translator for storage
					translator.processNode(node);
					if (translator.mustStoreNode(node)) {
						processor.storeNode(node);
					}
					// increment node index
					nodeIndex++;

				}
				List<Long> ids = new ArrayList<Long>();
				for (Nd nd : way.getNd()) {
					ids.add(nd.getRef());
				}
				List<Node> nodes = processor.getNodes(ids);
				if (nodes != null) {
					OsmPolygon polygon = new OsmPolygon(way.getId(),
							way.getTag(), nodes, nodes.size() < ids.size());
					translator.processPolyline(polygon);
				}
				esriPolygonRecord = (ESRIPolygonRecord) shapeFile
						.getNextRecord();
			}

			complete();

		} catch (IOException e) {
			throw new OsmParsingException("Error parsing shapeFile.", e);
		} catch (DataSinkException e) {
			throw new OsmParsingException("DataSink error.", e);
		} catch (Osm2xpBusinessException e) {
			throw new OsmParsingException("Translation error.", e);
		}

	}

	@Override
	public void complete() {
		translator.complete();

	}

}
