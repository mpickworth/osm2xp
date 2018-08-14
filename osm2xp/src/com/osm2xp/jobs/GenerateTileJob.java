package com.osm2xp.jobs;

import java.awt.Point;
import java.io.File;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.exceptions.OsmParsingException;
import com.osm2xp.model.osm.Relation;
import com.osm2xp.parsers.IParser;
import com.osm2xp.parsers.ParserBuilder;
import com.osm2xp.utils.helpers.Osm2xpProjectHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;

import math.geom2d.Point2D;

/**
 * GenerateTileJob.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class GenerateTileJob extends GenerateJob {

	private transient Point2D coordinates;

	public GenerateTileJob(String name, File currentFile, Point2D coordinates,
			String folderPath, List<Relation> relationsList, String family) {
		super(name, currentFile, folderPath, relationsList, family);
		Osm2xpLogger.info("Starting  generation of " + getCoordinatesStr(coordinates) + ", target folder " + folderPath);
		this.coordinates = coordinates;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IParser parser;
		try {
			parser = ParserBuilder.getParser(coordinates, currentFile,
					folderPath, relationsList);
			parser.process();
			Osm2xpProjectHelper.removeTile(coordinates);
			Osm2xpLogger.info("Finished generation of " + getCoordinatesStr(coordinates) + ", target folder " + folderPath);
		} catch (DataSinkException e) {
			Osm2xpLogger.error("Data sink exception : ", e);
		} catch (OsmParsingException e) {
			Osm2xpLogger.error("Parsing exception : ", e);
		} catch (Osm2xpBusinessException e) {
			Osm2xpLogger.error("Business exception : ", e);
		}

		return Status.OK_STATUS;

	}

	private String getCoordinatesStr(Point2D coords) {
		if (coords == null) {
			return "whole file";
		}
		Point intPt = coords.getAsInt();
		return String.format(Locale.ROOT, "tile (%d,%d)", intPt.x, intPt.y);
	}
	
	public boolean belongsTo(Object family) {
		return family.equals(family);
	}

	public String getFamilly() {
		return family;
	}

	public void setFamilly(String familly) {
		this.family = familly;
	}

}
