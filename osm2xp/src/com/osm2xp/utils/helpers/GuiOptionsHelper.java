package com.osm2xp.utils.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;

import com.osm2xp.constants.Osm2xpConstants;
import com.osm2xp.constants.Perspectives;
import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.gui.Activator;
import com.osm2xp.model.options.GuiOptions;
import com.osm2xp.model.options.LastFiles;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.utils.logging.Osm2xpLogger;

import math.geom2d.Point2D;

/**
 * GuiOptionsHelper.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class GuiOptionsHelper {

	private static GuiOptions options;
	public static final String SCENE_NAME = "sceneName";
	public static final String USED_FILES = "usedFiles";
	private static Tag shapefileTag;
	private static File roofColorFile;
	private static List<String> lastFiles = new ArrayList<>();

	private static final String INTERFACE_OPTIONS_FILE_PATH = ResourcesPlugin
			.getWorkspace().getRoot().getLocation()
			+ File.separator + "GuiOptions.xml";
	private static Point2D selectedCoordinates;

	static {
		if (new File(INTERFACE_OPTIONS_FILE_PATH).exists()) {
			try {
				options = (GuiOptions) XmlHelper.loadFileFromXml(new File(
						INTERFACE_OPTIONS_FILE_PATH), GuiOptions.class);
			} catch (Osm2xpBusinessException e) {
				Osm2xpLogger.error("Error initializing GUI options helper", e);
			}
		} else {
			options = createNewGuiOptionsBean();
		}
		String filesStr = getStringProperty(USED_FILES);
		if (filesStr.length() > 0) {
			lastFiles.addAll(Arrays.asList(filesStr.split(File.separator)));
		}
	}

	/**
	 * return true if the choosen output mode is generating files
	 * 
	 * @return boolean
	 */
	public static boolean isOutputFormatAFileGenerator() {
		return (getOptions().getOutputFormat().equalsIgnoreCase(
				Perspectives.PERSPECTIVE_FLY_LEGACY)
				|| getOptions().getOutputFormat().equalsIgnoreCase(
						Perspectives.PERSPECTIVE_XPLANE10)
				|| getOptions().getOutputFormat().equalsIgnoreCase(
						Perspectives.PERSPECTIVE_XPLANE9)
				|| getOptions().getOutputFormat().equalsIgnoreCase(
						Perspectives.PERSPECTIVE_WAVEFRONT)
				|| getOptions().getOutputFormat().equalsIgnoreCase(
						Perspectives.PERSPECTIVE_OSM) || getOptions()
				.getOutputFormat().equalsIgnoreCase(
						Perspectives.PERSPECTIVE_FLIGHT_GEAR));
	}

	/**
	 * @return
	 */
	private static GuiOptions createNewGuiOptionsBean() {
		GuiOptions result = new GuiOptions(false, false, false, true, null,
				Perspectives.PERSPECTIVE_XPLANE10, false,
				new ArrayList<String>());
		return result;
	}

	public static GuiOptions getOptions() {
		return options;
	}

	public static void setSceneName(String sceneName) {
		putProperty(SCENE_NAME, sceneName);
	}
	
	private static String getStringProperty(String property) {
		return InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).get(property, "");
	}
	
	private static void putProperty(String property, String value) {
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		node.put(property, value);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}

	public static String getSceneName() {
		return getStringProperty(SCENE_NAME);
	}

	public static void saveOptions() throws Osm2xpBusinessException {
		XmlHelper
				.saveToXml(getOptions(), new File(INTERFACE_OPTIONS_FILE_PATH));
	}

	public static void addUsedFile(String fileName) {
		if (lastFiles.indexOf(fileName) == -1) {
			lastFiles.add(fileName);
		}
		putProperty(USED_FILES, options.getLastFiles().stream().collect(Collectors.joining(File.pathSeparator)));
	}

	public static void askShapeFileNature(Shell shell) {
		MessageDialog messageDialog = new MessageDialog(shell,
				"shapefile nature", null, "What is the nature of "
						+ new File(getOptions().getCurrentFilePath()).getName()
						+ "?", MessageDialog.QUESTION, new String[] {
						"Buildings", "Forests" }, 1);
		if (messageDialog.open() == 0) {
			shapefileTag = new Tag("yes", "building");
		} else {
			shapefileTag = new Tag("forest", "landuse");
		}

	}

	public static Tag getShapefileTag() {
		return shapefileTag;
	}

	public static void setShapefileTag(Tag shapefileTag) {
		GuiOptionsHelper.shapefileTag = shapefileTag;
	}

	public static File getRoofColorFile() {
		return roofColorFile;
	}

	public static void setRoofColorFile(File roofColorFile) {
		GuiOptionsHelper.roofColorFile = roofColorFile;
	}

	public static void setSelectedCoordinates(Point2D selectedCoordinates) {
		GuiOptionsHelper.selectedCoordinates = selectedCoordinates;
	}

	public static Point2D getSelectedCoordinates() {
		return selectedCoordinates;
	}
	
	public static List<String> getLastFiles() {
		return lastFiles;
	}
}
