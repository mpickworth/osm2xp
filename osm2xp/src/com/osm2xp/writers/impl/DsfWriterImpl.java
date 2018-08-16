package com.osm2xp.writers.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.osm2xp.constants.XplaneConstants;
import com.osm2xp.utils.DsfObjectsProvider;
import com.osm2xp.utils.DsfUtils;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;
import com.osm2xp.writers.IWriter;

import math.geom2d.Point2D;

/**
 * Dsf Writer implementation.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class DsfWriterImpl implements IWriter {

	private String sceneFolder;
	private DsfObjectsProvider dsfObjectsProvider;
	private File dsfFile;
	private BufferedWriter writer;

	public DsfWriterImpl(String sceneFolder, Point2D tile, DsfObjectsProvider dsfObjectsProvider) {
		this.sceneFolder = sceneFolder;
		this.dsfObjectsProvider = dsfObjectsProvider;

		dsfFile = DsfUtils.computeXPlaneDsfFilePath(sceneFolder, tile);
		// if file doesn't exists
		// if (!dsfFile.exists()) {
		// create the parent folder file
		File parentFolder = new File(dsfFile.getParent());
		parentFolder.mkdirs();
		// create writer for this file

		try {
			FileWriter fileWriter = new FileWriter(dsfFile, true);
			writer = new BufferedWriter(fileWriter);

			// write its header
			String dsfHeader = DsfUtils.getDsfHeader(tile, this.dsfObjectsProvider);
			writer.write(dsfHeader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		// delete on exist
		dsfFile.deleteOnExit();
	}

	public void write(Object data) {
		try {
			if (data != null) {
				// write into this dsf file
				writer.write((String) data);
			}
		} catch (IOException e) {
			Osm2xpLogger.error(e.getMessage());
		}
	}

	@Override
	public void complete(Object data) {
		// flush/close all writers
			try {
				writer.flush();
				writer.close();
			} catch (IOException e) {
				Osm2xpLogger.error(e.getMessage());
			}


		if (data != null) {
			try {
				injectSmartExclusions((String) data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		DsfUtils.textToDsf(dsfFile, new File(dsfFile.getPath().replaceAll(".txt", "")));

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

	private void injectSmartExclusions(String exclusionText) throws IOException {

		// temp file
		File tempFile = new File(dsfFile.getAbsolutePath() + "_temp");
		BufferedReader br = new BufferedReader(new FileReader(dsfFile));
		String line;
		Boolean exclusionInjected = false;

		FileWriter writer = new FileWriter(tempFile.getPath(), true);
		BufferedWriter output = new BufferedWriter(writer);

		while ((line = br.readLine()) != null) {
			if (!exclusionInjected && line.contains(XplaneConstants.EXCLUSION_PLACEHOLDER)) {
				output.write(exclusionText);
				exclusionInjected = true;
			} else {
				output.write(line + "\n");
			}
		}
		br.close();
		output.flush();
		output.close();

		FileUtils.copyFile(tempFile, dsfFile);
		tempFile.delete();
		tempFile.deleteOnExit();

	}
}
