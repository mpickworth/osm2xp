package com.osm2xp.writers.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import math.geom2d.Point2D;

import com.osm2xp.jobs.MutexRule;
import com.osm2xp.jobs.RunProcessJob;
import com.osm2xp.model.fsx.FsxObject;
import com.osm2xp.model.geom.Lod13Location;
import com.osm2xp.utils.logging.Osm2xpLogger;
import com.osm2xp.writers.IWriter;

/**
 * Agn writer implementation.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class AgnWriterImpl implements IWriter {

	private String sceneFolder;

	public AgnWriterImpl(String sceneFolder) {
		this.sceneFolder = sceneFolder;
	}

	@Override
	public void init(Point2D coordinates) {
		new File(this.sceneFolder).mkdirs();
	}

	private BufferedWriter getWriter(Lod13Location location) throws IOException {
		String filePath = this.sceneFolder + File.separator
				+ location.getLod13String() + "an.txt";
		BufferedWriter output = null;
		Boolean isNewFile = !new File(filePath).exists();
		FileWriter writer = new FileWriter(filePath, true);
		output = new BufferedWriter(writer);

		if (isNewFile) {
			new File(filePath).deleteOnExit();
			output.write("Location " + location.getLod13String() + "\n");
			output.write("Version 1091777331\n");
			DecimalFormatSymbols.getInstance().getDecimalSeparator();
			String buildingHeight = NumberFormat.getNumberInstance(
					Locale.getDefault()).format(0.5D);
			output.write("AGNBuildingHeight " + buildingHeight + " "
					+ buildingHeight + " " + buildingHeight + " "
					+ buildingHeight + "\n");
			output.flush();
		}

		return output;
	}

	@Override
	public void write(Object data) {
		if (data != null && data instanceof FsxObject) {
			FsxObject fsxObject = (FsxObject) data;
			try {
				BufferedWriter writer = getWriter(fsxObject.getLod13Locations()
						.get(0));
				writer.write(fsxObject.toString());
				writer.flush();
				writer.close();
			} catch (IOException e) {
				Osm2xpLogger.error("Error writing data.", e);
			}

		}
	}

	@Override
	public void complete(Object data) {

		String agn2txt = "C:/Documents and Settings/bblanche/Bureau/latest_development_releases_package"
				+ File.separatorChar + "txt2agn.exe";
		FilenameFilter txtFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().contains(".txt");
			}
		};
		String[] files = new File(sceneFolder).list(txtFilter);

		Osm2xpLogger.info("Starting .agn compilation jobs.");
		for (String filePath : Arrays.asList(files)) {

			RunProcessJob job = new RunProcessJob("Agn compilation of file "
					+ filePath, "agnCompilation", agn2txt + " " + sceneFolder
					+ File.separator + filePath + " " + sceneFolder);
			job.setRule(new MutexRule());
			job.schedule();

		}

	}

}
