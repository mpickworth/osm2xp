package com.osm2xp.utils.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;

import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.model.options.FlightGearObjectTagRule;
import com.osm2xp.model.options.FlightGearObjectsRulesList;
import com.osm2xp.model.options.FlightGearOptions;
import com.osm2xp.model.options.ObjectFile;
import com.osm2xp.model.options.ObjectsRulesList;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * FlightGearOptionsHelper.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class FlightGearOptionsHelper extends OptionsHelper {

	private static FlightGearOptions options;
	private static final String FLIGHTGEAR_OPTIONS_FILE_PATH = ResourcesPlugin
			.getWorkspace().getRoot().getLocation()
			+ File.separator + "FlightGearOptions.xml";

	static {
		if (new File(FLIGHTGEAR_OPTIONS_FILE_PATH).exists()) {
			try {
				options = (FlightGearOptions) XmlHelper.loadFileFromXml(
						new File(FLIGHTGEAR_OPTIONS_FILE_PATH),
						FlightGearOptions.class);
			} catch (Osm2xpBusinessException e) {
				Osm2xpLogger.error(
						"Error initializing FlightGear options helper", e);
			}
		} 
		if (options == null) {
			options = createNewFlightGearOptionsBean();
		}
	}

	public static FlightGearOptions getOptions() {
		return options;
	}

	/**
	 * @return
	 */
	private static FlightGearOptions createNewFlightGearOptionsBean() {
		FlightGearOptions result = new FlightGearOptions();
		result.setObjectsRules(createNewObjectsRules());

		return result;
	}

	/**
	 * @return
	 */
	@SuppressWarnings("serial")
	private static FlightGearObjectsRulesList createNewObjectsRules() {
		List<FlightGearObjectTagRule> FlightGearObjectTagRules = new ArrayList<FlightGearObjectTagRule>();
		FlightGearObjectTagRules.add(new FlightGearObjectTagRule(new Tag(
				"man_made", "lighthouse"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile("Models/Communications/lighthouses.xml"));
			}
		}, 0, true, false, false, 0, 0, 0, 0, false, 0, 0, false, false));
		FlightGearObjectTagRules.add(new FlightGearObjectTagRule(new Tag(
				"man_made", "water_tower"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile("Models/Communications/water-tower.xml"));
			}
		}, 0, true, false, false, 0, 0, 0, 0, false, 0, 0, false, false));

		FlightGearObjectsRulesList result = new FlightGearObjectsRulesList(
				FlightGearObjectTagRules);
		return result;
	}

	/**
	 * @throws Osm2xpBusinessException
	 */
	public static void saveOptions() throws Osm2xpBusinessException {
		XmlHelper.saveToXml(getOptions(),
				new File(FLIGHTGEAR_OPTIONS_FILE_PATH));

	}

	/**
	 * @param file
	 */
	public static void importObjectsRules(File file) {
		try {
			Object result = XmlHelper.loadFileFromXml(file,
					ObjectsRulesList.class);
			getOptions().setObjectsRules((FlightGearObjectsRulesList) result);
		} catch (Osm2xpBusinessException e) {
			Osm2xpLogger.error("Error importing objects rules file", e);
		}

	}

}
