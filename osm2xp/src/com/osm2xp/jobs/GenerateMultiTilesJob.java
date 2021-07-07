package com.osm2xp.jobs;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.exceptions.OsmParsingException;
import com.osm2xp.model.osm.Relation;
import com.osm2xp.parsers.IBasicParser;
import com.osm2xp.parsers.ParserBuilder;
import com.osm2xp.utils.helpers.Osm2xpProjectHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;

import math.geom2d.Point2D;

/**
 * Job for generating scenario for multiple tiles 
 * 
 * @author Dmitry Karpenko
 * 
 */
public class GenerateMultiTilesJob extends GenerateJob {
	protected List<Point2D> tiles;

	public GenerateMultiTilesJob(String name, File currentFile, List<Point2D> tiles,
			String folderPath, List<Relation> relationsList, String family) {
		super(name, currentFile, folderPath, relationsList, family);
		this.tiles = tiles;
		Osm2xpLogger.info("Starting  generation of " + tiles.size() + " tiles, target folder " + folderPath);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IBasicParser parser;
		try {
			parser = ParserBuilder.getMultiTileParser(tiles, currentFile,
					folderPath, relationsList);
			parser.process();
			Osm2xpProjectHelper.removeTiles(tiles);
			Osm2xpLogger.info("Finished generation of " +  tiles.size() + " tiles, target folder " + folderPath);
		} catch (DataSinkException e) {
			Osm2xpLogger.error("Data sink exception : ", e);
		} catch (OsmParsingException e) {
			Osm2xpLogger.error("Parsing exception : ", e);
		} 

		return Status.OK_STATUS;

	}

}
