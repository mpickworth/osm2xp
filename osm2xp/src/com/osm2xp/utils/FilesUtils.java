package com.osm2xp.utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.osm2xp.constants.Osm2xpConstants;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.exceptions.Osm2xpTechnicalException;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * FilesUtils.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class FilesUtils {

	/**
	 * Load roof color information from a g2xpl text file
	 * 
	 * @param file
	 *            G2xpl file
	 * @return Map<Long,Color>
	 * @throws Osm2xpBusinessException
	 */
	public static Map<Long, Color> loadG2xplColorFile(File file)
			throws Osm2xpTechnicalException {
		Map<Long, Color> result = new HashMap<Long, Color>();

		try {
			BufferedReader buff = new BufferedReader(new FileReader(
					file.getPath()));

			try {
				String line;
				while ((line = buff.readLine()) != null) {
					Long wayId = Long.parseLong(line.substring(0,
							line.indexOf(":")));
					String lineWork = line;
					// Integer blue = Integer.parseInt(lineWork.substring(
					// lineWork.lastIndexOf(",") + 1, lineWork.length()));
					// lineWork = lineWork.substring(0,
					// lineWork.lastIndexOf(","));
					// Integer green = Integer.parseInt(lineWork.substring(
					// lineWork.lastIndexOf(",") + 1, lineWork.length()));
					// lineWork = lineWork.substring(0,
					// lineWork.lastIndexOf(","));
					// Integer red = Integer.parseInt(lineWork.substring(
					// lineWork.lastIndexOf(",") + 1, lineWork.length()));
					Color redColor = new Color(255, 0, 0);
					Color greyColor = new Color(190, 190, 190);

					if (line.substring(line.length() - 1).equalsIgnoreCase("1")) {
						result.put(wayId, redColor);
					} else {
						result.put(wayId, greyColor);
					}
					// result.put(wayId, redColor);

				}
			} finally {

				buff.close();
			}
		} catch (IOException e) {
			throw new Osm2xpTechnicalException(
					"Error parsing g2xpl roof color file " + e.getMessage());
		}

		return result;
	}

	/**
	 * Write some text in a file
	 * 
	 * @param file
	 * @param text
	 * @param append
	 */
	public static void writeTextToFile(File file, String text, Boolean append) {
		if (text != null) {

			if (!new File(file.getParent()).exists()) {
				new File(file.getParent()).mkdirs();
			}

			try {
				FileWriter writer = new FileWriter(file.getPath(), append);
				BufferedWriter output = new BufferedWriter(writer);
				output.write(text);
				output.flush();
				output.close();
			} catch (IOException e) {
				Osm2xpLogger
						.error("Error writing to file " + file.getName(), e);
			}
		}
	}

	/**
	 * List existing facade sets
	 * 
	 * @param repertoire
	 * @throws Osm2xpBusinessException
	 */
	public static List<String> listFacadesSets() throws Osm2xpBusinessException {
		List<String> listeSetsFacades = new ArrayList<String>();

		File repertoire = new File(Osm2xpConstants.FACADES_SETS_PATH);

		File[] list = repertoire.listFiles();
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				if (list[i].isDirectory()) {
					listeSetsFacades.add(list[i].getName());
				}
			}
		}
		if (listeSetsFacades.isEmpty()) {
			throw new Osm2xpBusinessException(
					"No facades sets found in /ressources/facades!");
		}
		return listeSetsFacades;
	}
	
	public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
		copyDirectory(sourceLocation, targetLocation, false);
	}

	/**
	 * @param sourceLocation
	 * @param targetLocation
	 * @throws IOException
	 */
	public static void copyDirectory(File sourceLocation, File targetLocation, boolean rewrite)
			throws IOException {
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]), rewrite);
			}
		} else {
			if (!rewrite && targetLocation.isFile()) {
				return;
			}
			try (InputStream in = new FileInputStream(sourceLocation);OutputStream out = new FileOutputStream(targetLocation)) {
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			}
		}
	}

	/**
	 * @param facadeSetFolder
	 * @return
	 */
	public static String[] listFacadesFiles(String facadeSetFolder) {
		if (!new File(facadeSetFolder).isDirectory()) {
			return new String[0];
		}

		FilenameFilter facFilter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().contains(".fac");
			}
		};
		String[] files = new File(facadeSetFolder).list(facFilter);
		Arrays.sort(files, new WindowsExplorerComparator());

		return files;

	}

	/**
	 * @param folder
	 */
	public static void deleteDirectory(File folder) {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				deleteDirectory(file);
			} else {
				file.delete();
			}
		}
		folder.delete();
	}

}
