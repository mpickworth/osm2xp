package com.osm2xp.parsers.impl;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.osm2xp.dataProcessors.IDataSink;
import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.exceptions.OsmParsingException;
import com.osm2xp.model.osm.Nd;
import com.osm2xp.model.osm.Node;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.model.osm.Way;
import com.osm2xp.parsers.IParser;
import com.osm2xp.translators.ITranslator;
import com.osm2xp.utils.helpers.GuiOptionsHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * Sax parser implementation.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class SaxParserImpl implements ContentHandler, IParser {

	private static final String XML_ATTRIBUTE_LONGITUDE = "lon";
	private static final String XML_ATTRIBUTE_LATITUDE = "lat";
	private static final String XML_ATTRIBUTE_ID = "id";
	private static final String XML_ATTRIBUTE_REF = "ref";
	private static final String XML_NODE_ND = "nd";
	private static final String XML_NODE_TAG = "tag";
	private static final String XML_NODE_NODE = "node";
	private static final String XML_NODE_WAY = "way";
	protected Locator locator;
	protected List<Tag> tagList;
	protected OsmAttributes currentAttributes;
	protected List<Nd> ndList;
	private File xmlFile;
	private ITranslator translator;
	private Map<Long, Color> roofsColorMap;
	private IDataSink processor;
	private boolean nodesRefCollectionDone;

	public void init(File xmlFile, ITranslator translator,
			Map<Long, Color> roofsColorMap, IDataSink processor) {
		this.xmlFile = xmlFile;
		this.translator = translator;
		this.roofsColorMap = roofsColorMap;
		this.processor = processor;
	}

	/**
	 * 
	 * @param uri
	 * @throws Osm2xpBusinessException
	 * @throws Exception
	 */
	public void parseDocument() throws SAXException, Osm2xpBusinessException {
		XMLReader saxReader;
		saxReader = XMLReaderFactory.createXMLReader();
		saxReader.setContentHandler(this);
		try {
			saxReader.parse(this.xmlFile.getAbsolutePath());
		} catch (IOException e) {
			throw new Osm2xpBusinessException(e.getMessage());
		}

	}

	public void setDocumentLocator(Locator value) {
		this.locator = value;
	}

	public Locator getDocumentLocator() {
		return this.locator;
	}

	public void startDocument() throws SAXException {

	}

	public void endDocument() throws SAXException {
		complete();
	}

	public void startPrefixMapping(String prefix, String URI)
			throws SAXException {

	}

	public void endPrefixMapping(String prefix) throws SAXException {

	}

	public void startElement(String nameSpaceURI, String localName,
			String rawName, Attributes attributs) {
		if (localName.equalsIgnoreCase(XML_NODE_WAY)
				|| localName.equalsIgnoreCase(XML_NODE_NODE)) {
			tagList = new ArrayList<Tag>();
			ndList = new ArrayList<Nd>();
			currentAttributes = new OsmAttributes(attributs);
		} else {
			if (localName.equalsIgnoreCase(XML_NODE_TAG)) {
				Tag tag = new Tag();
				tag.setKey(attributs.getValue(0));
				tag.setValue(attributs.getValue(1));
				tagList.add(tag);
			} else {
				if (localName.equalsIgnoreCase(XML_NODE_ND)) {
					Nd nd = new Nd();
					nd.setRef(Long.parseLong(attributs
							.getValue(XML_ATTRIBUTE_REF)));
					this.ndList.add(nd);

				}
			}

		}
	}

	@Override
	public void endElement(String nameSpaceURI, String localName, String rawName)
			throws SAXException {
		if (localName.equals(XML_NODE_WAY)) {
			try {
				parseWay();
			} catch (Osm2xpBusinessException e) {
				Osm2xpLogger.error("Error parsing way object.", e);
			} catch (DataSinkException e) {
				Osm2xpLogger.error("Error parsing way object.", e);
			}
		} else if (localName.equals(XML_NODE_NODE)) {
			parseNode();

		}
	}

	private void sendWayToTranslator(Way way) throws Osm2xpBusinessException,
			DataSinkException {

		// if roof color information is available, add it to the current way
		if (this.roofsColorMap != null
				&& this.roofsColorMap.get(way.getId()) != null) {
			String hexColor = Integer.toHexString(this.roofsColorMap.get(
					way.getId()).getRGB() & 0x00ffffff);
			Tag roofColorTag = new Tag("building:roof:color", "#"
					+ hexColor.toUpperCase());
			way.getTag().add(roofColorTag);
		}

		List<Long> ids = new ArrayList<Long>();
		for (Nd nd : way.getNd()) {
			ids.add(nd.getRef());
		}

		List<Node> nodes;

		nodes = processor.getNodes(ids);

		if (nodes != null) {
			OsmPolygon polygon = new OsmPolygon(way.getId(), way.getTag(),
					nodes, nodes.size() < ids.size());
			translator.processPolygon(polygon);
		}

	}

	private void checkWaysForUsefullNodes(Way way)
			throws Osm2xpBusinessException, DataSinkException {
		if (translator.mustStoreWay(way)) {
			for (Nd nd : way.getNd()) {
				com.osm2xp.model.osm.Node node = new com.osm2xp.model.osm.Node();
				node.setId(nd.getRef());
				node.setLat(0);
				node.setLon(0);
				processor.storeNode(node);
			}
		}

	}

	private void parseWay() throws Osm2xpBusinessException, DataSinkException {
		Way way = new Way();
		way.setId(Long.parseLong(currentAttributes.getValue(XML_ATTRIBUTE_ID)));
		way.getTag().addAll(tagList);
		way.getNd().addAll(ndList);

		// if we're not on a single pass mode, send ways to translator
		if (!GuiOptionsHelper.getOptions().isSinglePass()
				|| (GuiOptionsHelper.getOptions().isSinglePass() && nodesRefCollectionDone)) {
			sendWayToTranslator(way);
		} else {
			checkWaysForUsefullNodes(way);
		}

	}

	private void parseNode() {
		// parse nodes only if we're not on a single pass mode, or if the nodes
		// collection of single pass mode is done
		if (!GuiOptionsHelper.getOptions().isSinglePass()
				|| (GuiOptionsHelper.getOptions().isSinglePass() && this.nodesRefCollectionDone)) {
			Node node = new Node();
			node.setId(Long.parseLong(currentAttributes
					.getValue(XML_ATTRIBUTE_ID)));
			node.setLat(Double.parseDouble(currentAttributes
					.getValue(XML_ATTRIBUTE_LATITUDE)));
			node.setLon(Double.parseDouble(currentAttributes
					.getValue(XML_ATTRIBUTE_LONGITUDE)));
			node.getTag().addAll(tagList);
			try {
				translator.processNode(node);
				if (translator.mustStoreNode(node)) {
					processor.storeNode(node);
				}
			} catch (Osm2xpBusinessException e) {
				Osm2xpLogger.error("Error processing node.", e);
			} catch (DataSinkException e) {
				Osm2xpLogger.error("Error processing node.", e);
			}
		}
	}

	public void process() throws OsmParsingException {
		try {
			translator.init();
			this.parseDocument();
		} catch (Osm2xpBusinessException e) {
			throw new OsmParsingException("Osm parser error on line "
					+ locator.getLineNumber() + " col "
					+ locator.getColumnNumber(), e);
		} catch (SAXException e) {
			throw new OsmParsingException("Sax parser initialization error.", e);
		}

	}

	public void complete() {
		if (GuiOptionsHelper.getOptions().isSinglePass()
				&& !nodesRefCollectionDone) {
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
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
	}

	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
	}

	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
	}

	@Override
	public void skippedEntity(String arg0) throws SAXException {
	}

	/**
	 * inner class to store xml attributes
	 */
	private class OsmAttributes {

		private HashMap<String, String> values = new HashMap<String, String>();

		public OsmAttributes(Attributes attributs) {
			for (int i = 0; i < attributs.getLength(); i++) {
				values.put(attributs.getLocalName(i), attributs.getValue(i));

			}
		}

		public String getValue(String key) {
			return values.get(key);
		}
	}
}
