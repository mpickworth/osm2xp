package com.osm2xp.parsers;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import math.geom2d.Point2D;

import com.osm2xp.dataProcessors.DataSinkFactory;
import com.osm2xp.dataProcessors.IDataSink;
import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.model.osm.Relation;
import com.osm2xp.parsers.impl.MultiTileParserImpl;
import com.osm2xp.parsers.impl.PbfSingleTileParserImpl;
import com.osm2xp.parsers.impl.PbfWholeFileParserImpl;
import com.osm2xp.parsers.impl.SaxParserImpl;
import com.osm2xp.parsers.impl.ShapefileParserImpl;
import com.osm2xp.parsers.impl.TileTranslationAdapter;
import com.osm2xp.translators.ITranslator;
import com.osm2xp.translators.TranslatorBuilder;
import com.osm2xp.utils.FilesUtils;
import com.osm2xp.utils.helpers.GuiOptionsHelper;

/**
 * ParserBuilder.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class ParserBuilder {

	/**
	 * Build the parser implementation for the type of file
	 * 
	 * @param folderPath
	 * @param relationsList
	 * 
	 * @param tuile
	 * @return
	 * @throws Osm2xpBusinessException
	 * @throws DataSinkException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public static IParser getParser(Point2D currentTile, File currentFile,
			String folderPath, List<Relation> relationsList)
			throws DataSinkException {
		IParser parser = null;
		ITranslator translator = TranslatorBuilder.getTranslator(currentFile,
				currentTile, folderPath, relationsList);
		// if a roof color file is available, load it into a map and give it to
		// the parser
		Map<Long, Color> roofsColorMap = null;
		if (GuiOptionsHelper.getRoofColorFile() != null) {
			roofsColorMap = FilesUtils.loadG2xplColorFile(GuiOptionsHelper
					.getRoofColorFile());
		}
		IDataSink processor = DataSinkFactory.getProcessor();
		// PBF FILE
		if (GuiOptionsHelper.getOptions().getCurrentFilePath().toLowerCase()
				.contains(".pbf")) {
			if (GuiOptionsHelper.getOptions().isSinglePass()) {
				parser = new PbfWholeFileParserImpl();
			} else {
				parser = new PbfSingleTileParserImpl(currentTile);
			}

		}
		// OSM FILE
		else if (GuiOptionsHelper.getOptions().getCurrentFilePath()
				.toLowerCase().contains(".osm")) {
			parser = new SaxParserImpl();

		}
		// SHP FILE
		else if (GuiOptionsHelper.getOptions().getCurrentFilePath()
				.toLowerCase().contains(".shp")) {
			parser = new ShapefileParserImpl();

		}
		parser.init(currentFile, translator, roofsColorMap, processor);
		return parser;
	}
	
	/**
	 * Build the parser implementation for the type of file
	 * 
	 * @param folderPath
	 * @param relationsList
	 * 
	 * @param tiles Tiles list
	 * @return
	 * @throws Osm2xpBusinessException
	 * @throws DataSinkException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public static IBasicParser getMultiTileParser(List<Point2D> tiles, File currentFile,
			String folderPath, List<Relation> relationsList)
			throws DataSinkException {
		IDataSink processor = DataSinkFactory.getProcessor();
		List<TileTranslationAdapter> translationAdapters = new ArrayList<TileTranslationAdapter>();
		for (Point2D tile : tiles) {
			ITranslator translator = TranslatorBuilder.getTranslator(currentFile,
					tile, folderPath, relationsList);
			translationAdapters.add(new TileTranslationAdapter(tile, processor, translator));
			
		}
		// if a roof color file is available, load it into a map and give it to
		// the parser
		Map<Long, Color> roofsColorMap = null;
		if (GuiOptionsHelper.getRoofColorFile() != null) {
			roofsColorMap = FilesUtils.loadG2xplColorFile(GuiOptionsHelper
					.getRoofColorFile());
		}
		//TODO supported only for pbf yet 
		if (GuiOptionsHelper.getOptions().getCurrentFilePath().toLowerCase()
				.contains(".pbf")) {
			return new MultiTileParserImpl(currentFile,translationAdapters, roofsColorMap,processor);

		}
		return null;
	}

}
