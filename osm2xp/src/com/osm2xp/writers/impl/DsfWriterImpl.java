package com.osm2xp.writers.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import math.geom2d.Point2D;

import org.apache.commons.io.FileUtils;

import com.osm2xp.constants.XplaneConstants;
import com.osm2xp.utils.DsfObjectsProvider;
import com.osm2xp.utils.DsfUtils;
import com.osm2xp.utils.FilesUtils;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;
import com.osm2xp.writers.IWriter;

/**
 * Dsf Writer implementation.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class DsfWriterImpl implements IWriter {

	private String sceneFolder;
	private DsfObjectsProvider dsfObjectsProvider;
	private Map<Point2D, File> dsfFiles = new HashMap<Point2D, File>();
	private Map<Point2D, BufferedWriter> dsfWriters = new HashMap<Point2D, BufferedWriter>();

	public DsfWriterImpl(String sceneFolder,
			DsfObjectsProvider dsfObjectsProvider) {
		this.sceneFolder = sceneFolder;
		this.dsfObjectsProvider = dsfObjectsProvider;
	}

	public void write(Object data, Point2D coordinates) {
		try {
			if (data != null) {
				// look if we have a dsf file for this coordinate point
				checkDsfFile(coordinates);
				// write into this dsf file
				dsfWriters.get(coordinates).write((String) data);
			}
		} catch (IOException e) {
			Osm2xpLogger.error(e.getMessage());
		}
	}

	@Override
	public void complete(Object data) {
		// flush/close all writers
		for (Entry<Point2D, BufferedWriter> entry : dsfWriters.entrySet()) {
			try {
				entry.getValue().flush();
				entry.getValue().close();
			} catch (IOException e) {
				Osm2xpLogger.error(e.getMessage());
			}

		}

		if (data != null) {
			try {
				injectSmartExclusions((String) data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (Map.Entry<Point2D, File> entry : dsfFiles.entrySet()) {
			DsfUtils.textToDsf(entry.getValue(), new File(entry.getValue()
					.getPath().replaceAll(".txt", "")));
		}

	}

	@Override
	public void init(Point2D coordinates) {

		// create stats folder
		if (XplaneOptionsHelper.getOptions().isGeneratePdfStats()
				|| XplaneOptionsHelper.getOptions().isGenerateXmlStats()) {
			new File(sceneFolder + File.separatorChar + "stats").mkdirs();
		}

		// write the libraty file if needed
		if (!XplaneOptionsHelper.getOptions().isPackageFacades()) {
			DsfUtils.writeLibraryFile(sceneFolder, dsfObjectsProvider);
		}

	}

	public File checkDsfFile(Point2D coordinates) throws IOException {
		File dsfFile = dsfFiles.get(coordinates); 
		if (dsfFile != null) {
			return dsfFile;
		}
		// compute the dsf file path
		dsfFile = DsfUtils.computeXPlaneDsfFilePath(sceneFolder,
				coordinates);
		// if file doesn't exists
//		if (!dsfFile.exists()) {
			// create the parent folder file
			File parentFolder = new File(dsfFile.getParent());
			parentFolder.mkdirs();
			// create writer for this file
			FileWriter writer;

			writer = new FileWriter(dsfFile, true);
			BufferedWriter output = new BufferedWriter(writer);

			// write its header
			String dsfHeader = DsfUtils.getDsfHeader(coordinates,
					this.dsfObjectsProvider);
			output.write(dsfHeader);
			// add the file to the local files list
			dsfFiles.put(coordinates, dsfFile);
			// add the writer to the writers list
			dsfWriters.put(coordinates, output);
			// delete on exist
			dsfFile.deleteOnExit();

//		}

		// return the dsf file
		return dsfFile;
	}

	@Override
	public void write(Object data) {

	}

	private void injectSmartExclusions(String exclusionText) throws IOException {

		for (Map.Entry<Point2D, File> entry : dsfFiles.entrySet()) {

			// temp file
			File tempFile = new File(entry.getValue().getAbsolutePath()
					+ "_temp");
			BufferedReader br = new BufferedReader(new FileReader(
					entry.getValue()));
			String line;
			Boolean exclusionInjected = false;

			FileWriter writer = new FileWriter(tempFile.getPath(), true);
			BufferedWriter output = new BufferedWriter(writer);

			while ((line = br.readLine()) != null) {
				if (!exclusionInjected
						&& line.contains(XplaneConstants.EXCLUSION_PLACEHOLDER)) {
					output.write(exclusionText);
					exclusionInjected = true;
				} else {
					output.write(line + "\n");
				}
			}
			br.close();
			output.flush();
			output.close();

			FileUtils.copyFile(tempFile, entry.getValue());
			tempFile.delete();
			tempFile.deleteOnExit();

		}
	}
}
