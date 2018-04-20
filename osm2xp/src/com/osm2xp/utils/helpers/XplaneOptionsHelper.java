package com.osm2xp.utils.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;

import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.model.options.BuildingsExclusionsList;
import com.osm2xp.model.options.FacadesRulesList;
import com.osm2xp.model.options.ForestTagRule;
import com.osm2xp.model.options.ForestsRulesList;
import com.osm2xp.model.options.XplaneLightTagRule;
import com.osm2xp.model.options.LightsRulesList;
import com.osm2xp.model.options.ObjectFile;
import com.osm2xp.model.options.ObjectsList;
import com.osm2xp.model.options.ObjectsRulesList;
import com.osm2xp.model.options.XplaneObjectTagRule;
import com.osm2xp.model.options.XplaneObjectsRulesList;
import com.osm2xp.model.options.XplaneOptions;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * XplaneOptionsHelper.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class XplaneOptionsHelper extends OptionsHelper {

	private static XplaneOptions options;
	private static final String XPLANE_OPTIONS_FILE_PATH = ResourcesPlugin
			.getWorkspace().getRoot().getLocation()
			+ File.separator + "xplaneOptions.xml";

	static {
		if (new File(XPLANE_OPTIONS_FILE_PATH).exists()) {
			try {
				options = (XplaneOptions) XmlHelper.loadFileFromXml(new File(
						XPLANE_OPTIONS_FILE_PATH), XplaneOptions.class);
			} catch (Osm2xpBusinessException e) {
				Osm2xpLogger.error("Error initializing Xplane options helper",
						e);
			}
		} 
		if (options == null) {
			options = createNewXplaneOptionsBean();
		}
	}

	public static XplaneOptions getOptions() {
		return options;
	}

	/**
	 * @return
	 */
	private static XplaneOptions createNewXplaneOptionsBean() {
		XplaneOptions result = new XplaneOptions();
		result.setBuildingMax(30);
		result.setBuildingMin(10);
		result.setResidentialMax(15);
		result.setResidentialMin(6);
		result.setExcludeFor(true);
		result.setExcludeObj(true);
		result.setFacadeLod(25000);
		result.setGenerateBuildings(true);
		result.setGenerateFor(true);
		result.setGenerateFence(true);
		result.setGeneratePowerlines(true);
		result.setGenerateRailways(true);
		result.setGenerateRoads(true);
		result.setGeneratePdfStats(true);
		result.setGenerateDebugImg(false);
		result.setGenerateSlopedRoofs(true);
		result.setGenerateObj(true);
		result.setPackageFacades(true);
		result.setBuildingsExclusions(createNewXplaneExclusions());
		result.setForestsRules(createNewForestRules());
		result.setObjectsRules(createNewObjectsRules());
		result.setLightsRules(createNewLightsRules());
		result.setStreetLightObjects(createNewStreetLightsObjects());
		result.setFacadesRules(new FacadesRulesList());
		result.setMinHouseSegment(2);
		result.setMinHouseArea(20);
		result.setMaxHouseSegment(200);
		result.setSmartExclusionDistance(25);
		result.setSmartExclusionSize(100);
		return result;
	}

	/**
	 * @return
	 */
	@SuppressWarnings("serial")
	private static ObjectsList createNewStreetLightsObjects() {
		List<ObjectFile> objectFiles = new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile(
						"opensceneryx/objects/furniture/lights/street/3.obj"));
				add(new ObjectFile(
						"opensceneryx/objects/furniture/lights/street/2.obj"));
			}
		};
		ObjectsList result = new ObjectsList(objectFiles);
		return result;
	}

	/**
	 * @return
	 */
	@SuppressWarnings("serial")
	private static XplaneObjectsRulesList createNewObjectsRules() {
		List<XplaneObjectTagRule> XplaneObjectTagRules = new ArrayList<XplaneObjectTagRule>();
		XplaneObjectTagRules.add(new XplaneObjectTagRule(new Tag(
				"power_source", "wind"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile(
						"opensceneryx/objects/buildings/industrial/wind_turbines/large.obj"));
			}
		}, 0, true, false, false, 0, 0, 0, 0, false, 0, 0, false, false));
		XplaneObjectTagRules.add(new XplaneObjectTagRule(new Tag("man_made",
				"lighthouse"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile(
						"opensceneryx/objects/buildings/marine/lighthouses/1.obj"));
			}
		}, 0, true, false, false, 0, 0, 0, 0, false, 0, 0, false, false));
		XplaneObjectTagRules.add(new XplaneObjectTagRule(new Tag("man_made",
				"water_tower"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile(
						"opensceneryx/objects/buildings/industrial/water_towers/cylindrical/1.obj"));
				add(new ObjectFile(
						"opensceneryx/objects/buildings/industrial/water_towers/cylindrical/2.obj"));
			}
		}, 0, true, false, false, 0, 0, 0, 0, false, 0, 0, false, false));
		XplaneObjectTagRules.add(new XplaneObjectTagRule(new Tag("man_made",
				"crane"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile(
						"opensceneryx/objects/buildings/industrial/cranes/1.obj"));
			}
		}, 0, true, false, false, 0, 0, 0, 0, false, 0, 0, false, false));
		XplaneObjectsRulesList result = new XplaneObjectsRulesList(
				XplaneObjectTagRules);
		return result;
	}

	/**
	 * @return
	 */
	private static BuildingsExclusionsList createNewXplaneExclusions() {
		List<Tag> exclusionsList = new ArrayList<Tag>();
		exclusionsList.add(new Tag("aeroway", "hangar"));
		exclusionsList.add(new Tag("aeroway", "terminal"));
		BuildingsExclusionsList result = new BuildingsExclusionsList(
				exclusionsList);
		return result;
	}

	/**
	 * @return
	 */
	@SuppressWarnings("serial")
	private static ForestsRulesList createNewForestRules() {
		List<ForestTagRule> forestsRules = new ArrayList<ForestTagRule>();
		forestsRules.add(new ForestTagRule(new Tag("landuse", "forest"),
				new ArrayList<ObjectFile>() {
					{
						add(new ObjectFile(
								"opensceneryx/forests/trees/europe/continental/mixed.for"));
						add(new ObjectFile(
								"opensceneryx/forests/trees/europe/continental/conifer.for"));
						add(new ObjectFile(
								"opensceneryx/forests/trees/europe/continental/broad_leaf.for"));
					}
				}, 255));
		forestsRules.add(new ForestTagRule(new Tag("natural", "wood"),
				new ArrayList<ObjectFile>() {
					{
						add(new ObjectFile(
								"opensceneryx/forests/trees/europe/continental/mixed.for"));
						add(new ObjectFile(
								"opensceneryx/forests/trees/europe/continental/conifer.for"));
						add(new ObjectFile(
								"opensceneryx/forests/trees/europe/continental/broad_leaf.for"));
					}
				}, 255));
		forestsRules.add(new ForestTagRule(new Tag("leisure", "garden"),
				new ArrayList<ObjectFile>() {
					{
						add(new ObjectFile(
								"opensceneryx/forests/trees/europe/continental/heathland.for"));
						add(new ObjectFile(
								"opensceneryx/forests/trees/europe/continental/sclerophyllous.for"));
						add(new ObjectFile(
								"opensceneryx/forests/trees/europe/continental/conifer.for"));
					}
				}, 255));
		forestsRules.add(new ForestTagRule(new Tag("leisure", "park"),
				new ArrayList<ObjectFile>() {
					{
						add(new ObjectFile(
								"opensceneryx/forests/trees/europe/continental/heathland.for"));
						add(new ObjectFile(
								"opensceneryx/forests/trees/europe/continental/sclerophyllous.for"));
						add(new ObjectFile(
								"opensceneryx/forests/trees/europe/continental/conifer.for"));
					}
				}, 255));
		ForestsRulesList result = new ForestsRulesList(forestsRules);
		return result;

	}

	/**
	 * @return
	 */
	@SuppressWarnings("serial")
	private static LightsRulesList createNewLightsRules() {
		List<XplaneLightTagRule> lightsRules = new ArrayList<XplaneLightTagRule>();
		LightsRulesList result = new LightsRulesList(lightsRules);
		return result;

	}

	/**
	 * @throws Osm2xpBusinessException
	 */
	public static void saveOptions() throws Osm2xpBusinessException {
		XmlHelper.saveToXml(getOptions(), new File(XPLANE_OPTIONS_FILE_PATH));

	}

	/**
	 * @param file
	 */
	public static void importExclusions(File file) {
		try {
			Object result = XmlHelper.loadFileFromXml(file,
					BuildingsExclusionsList.class);
			getOptions().setBuildingsExclusions(
					(BuildingsExclusionsList) result);
		} catch (Osm2xpBusinessException e) {
			Osm2xpLogger.error("Error importing exclusion file", e);
		}

	}

	/**
	 * @param file
	 */
	public static void importFacadesRules(File file) {
		try {
			Object result = XmlHelper.loadFileFromXml(file,
					FacadesRulesList.class);
			getOptions().setFacadesRules((FacadesRulesList) result);
		} catch (Osm2xpBusinessException e) {
			Osm2xpLogger.error("Error importing facades rules file", e);
		}

	}

	/**
	 * @param file
	 */
	public static void importForestsRules(File file) {
		try {
			Object result = XmlHelper.loadFileFromXml(file,
					ForestsRulesList.class);
			getOptions().setForestsRules((ForestsRulesList) result);
		} catch (Osm2xpBusinessException e) {
			Osm2xpLogger.error("Error importing forests rules file", e);
		}

	}

	/**
	 * @param file
	 */
	public static void importObjectsRules(File file) {
		try {
			Object result = XmlHelper.loadFileFromXml(file,
					ObjectsRulesList.class);
			getOptions().setObjectsRules((XplaneObjectsRulesList) result);
		} catch (Osm2xpBusinessException e) {
			Osm2xpLogger.error("Error importing objects rules file", e);
		}

	}

	public static void importStreetLightObjects(File file) {
		try {
			Object result = XmlHelper.loadFileFromXml(file, ObjectsList.class);
			getOptions().setStreetLightObjects((ObjectsList) result);
		} catch (Osm2xpBusinessException e) {
			Osm2xpLogger.error("Error importing street lights file", e);
		}

	}

}
