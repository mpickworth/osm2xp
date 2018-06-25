package com.osm2xp.jobs;

import java.awt.Point;
import java.io.File;
import java.util.List;
import java.util.Locale;

import math.geom2d.Point2D;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.exceptions.OsmParsingException;
import com.osm2xp.model.osm.Relation;
import com.osm2xp.parsers.IParser;
import com.osm2xp.parsers.ParserBuilder;
import com.osm2xp.utils.helpers.Osm2xpProjectHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * GenerateTileJob.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class GenerateTileJob extends Job {

	private transient Point2D coordinates;
	private transient File currentFile;
	private transient String folderPath;
	private transient List<Relation> relationsList;
	private String familly;

	public GenerateTileJob(String name, File currentFile, Point2D coordinates,
			String folderPath, List<Relation> relationsList, String familly) {
		super(name);
		Osm2xpLogger.info("Starting  generation of " + getCoordinatesStr(coordinates) + ", target folder " + folderPath);
		this.coordinates = coordinates;
		this.currentFile = currentFile;
		this.folderPath = folderPath;
		this.relationsList = relationsList;
		this.familly = familly;

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
		return familly.equals(family);
	}

	public String getFamilly() {
		return familly;
	}

	public void setFamilly(String familly) {
		this.familly = familly;
	}

}
