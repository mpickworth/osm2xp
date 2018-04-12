package com.osm2xp.utils.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;

import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.model.options.BuildingsExclusionsList;
import com.osm2xp.model.options.FlyLegacyOptions;
import com.osm2xp.model.options.WatchedTagsList;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * FlyLegacyOptionsHelper.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class FlyLegacyOptionsHelper extends OptionsHelper {

	private static FlyLegacyOptions options;
	private static final String FLY_LEGACY_OPTIONS_FILE_PATH = ResourcesPlugin
			.getWorkspace().getRoot().getLocation()
			+ File.separator + "flyLegacyOptions.xml";

	static {
		if (new File(FLY_LEGACY_OPTIONS_FILE_PATH).exists()) {
			try {
				options = (FlyLegacyOptions) XmlHelper.loadFileFromXml(
						new File(FLY_LEGACY_OPTIONS_FILE_PATH),
						FlyLegacyOptions.class);
			} catch (Osm2xpBusinessException e) {
				Osm2xpLogger.error(
						"Error initializing Fly Legacy options helper", e);
			}
		} 
		if (options == null) {
			options = createNewFlyLegacyOptionsBean();
		}
	}

	/**
	 * @return
	 */
	private static FlyLegacyOptions createNewFlyLegacyOptionsBean() {
		FlyLegacyOptions result = new FlyLegacyOptions(
				createNewWatchedTagsList());
		return result;
	}

	/**
	 * @return
	 */
	public static FlyLegacyOptions getOptions() {
		return options;
	}

	/**
	 * @return
	 */
	private static WatchedTagsList createNewWatchedTagsList() {
		List<Tag> watchedTags = new ArrayList<Tag>();
		watchedTags.add(new Tag("amenity", "*"));
		watchedTags.add(new Tag("building", "*"));
		watchedTags.add(new Tag("landuse", "*"));
		WatchedTagsList result = new WatchedTagsList(watchedTags);
		return result;
	}

	public static void saveOptions() throws Osm2xpBusinessException {
		XmlHelper.saveToXml(getOptions(),
				new File(FLY_LEGACY_OPTIONS_FILE_PATH));

	}

	/**
	 * @param file
	 */
	public static void importWatchedTags(File file) {
		try {
			Object result = XmlHelper.loadFileFromXml(file,
					BuildingsExclusionsList.class);
			getOptions().setWatchedTagsList((WatchedTagsList) result);
		} catch (Osm2xpBusinessException e) {
			Osm2xpLogger.error("Error importing watched tags list file", e);
		}

	}
}
