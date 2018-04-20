package com.osm2xp.constants;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Osm2xp Constants.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class Osm2xpConstants {

	public static final String OSM2XP_VERSION = "3.0.0";
	public static final String PREFS_PATH = "." + File.separator + "ressources"
			+ File.separator + "prefs" + File.separator;
	public static final String FACADES_SETS_PATH = ResourcesPlugin
			.getWorkspace().getRoot().getLocation()
			+ File.separator + "resources" + File.separatorChar + "facades";
	public static final String ORIENTDB_PATH = ResourcesPlugin.getWorkspace()
			.getRoot().getLocation()
			+ File.separator + "resources" + File.separatorChar + "orientDB";
	public static final String JDBM_PATH = ResourcesPlugin.getWorkspace()
			.getRoot().getLocation()
			+ File.separator + "resources" + File.separatorChar + "JDBM";
	public static final String UTILS_PATH = ResourcesPlugin.getWorkspace()
			.getRoot().getLocation()
			+ File.separator + "resources" + File.separatorChar + "utils";

}
