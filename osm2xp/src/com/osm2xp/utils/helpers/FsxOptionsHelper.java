package com.osm2xp.utils.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;

import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.model.options.FsxOptions;
import com.osm2xp.model.options.ObjectFile;
import com.osm2xp.model.options.ObjectTagRule;
import com.osm2xp.model.options.ObjectsRulesList;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * FsxOptionsHelper.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class FsxOptionsHelper extends OptionsHelper {

	private static FsxOptions options;
	private static final String FSX_OPTIONS_FILE_PATH = ResourcesPlugin
			.getWorkspace().getRoot().getLocation()
			+ File.separator + "fsxOptions.xml";

	static {
		if (new File(FSX_OPTIONS_FILE_PATH).exists()) {
			try {
				options = (FsxOptions) XmlHelper.loadFileFromXml(new File(
						FSX_OPTIONS_FILE_PATH), FsxOptions.class);
			} catch (Osm2xpBusinessException e) {
				Osm2xpLogger.error("Error initializing FSX options helper", e);
			}
		} 
		if (options == null) {
			options = createNewFsxOptionsBean();
		}
	}

	/**
	 * @return
	 */
	private static FsxOptions createNewFsxOptionsBean() {
		FsxOptions result = new FsxOptions(createNewObjectsRules(), null);
		return result;
	}

	/**
	 * @return
	 */
	public static FsxOptions getOptions() {
		return options;
	}

	/**
	 * @return
	 */
	@SuppressWarnings("serial")
	private static ObjectsRulesList createNewObjectsRules() {
		List<ObjectTagRule> objectsList = new ArrayList<ObjectTagRule>();

		// lighthouses
		ObjectTagRule objectLighthouse = new ObjectTagRule(new Tag("man_made",
				"lighthouse"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile("{BE7FA036-6133-48CD-B3A3-BDC339E6AB35}"));
				add(new ObjectFile("{8B63A054-B32E-415D-9884-258008444268}"));
				add(new ObjectFile("{DE00535C-5E8C-4CC0-932F-E82355A37412}"));
			}
		}, 0, true);
		objectsList.add(objectLighthouse);

		// silos
		ObjectTagRule objectSilo = new ObjectTagRule(
				new Tag("man_made", "silo"), new ArrayList<ObjectFile>() {
					{
						add(new ObjectFile(
								"{EF0366E8-AAFE-4DE8-A458-A61A5D7C84BB}"));
						add(new ObjectFile(
								"{A5B68A08-6F53-43F2-B0DD-C38B9EF79421}"));
						add(new ObjectFile(
								"{8D99C008-2B93-4A2B-A2D4-C2A75A1B2E41}"));
					}
				}, 0, true);
		objectsList.add(objectSilo);
		// wind turbines
		ObjectTagRule objectWindTurbine = new ObjectTagRule(new Tag(
				"power_source", "wind"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile("{F21DB452-6D9D-424D-B608-A9C34C194CC1}"));
			}
		}, 0, true);
		// water towers
		objectsList.add(objectWindTurbine);
		ObjectTagRule objectWaterTower = new ObjectTagRule(new Tag("man_made",
				"water_tower"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile("{F2EF2CD2-539C-49C3-812D-128167D6EBB0}"));
				add(new ObjectFile("{567C15BF-E002-4DE9-A38C-B68C55135A8A}"));
			}
		}, 0, true);
		// cranes
		objectsList.add(objectWaterTower);
		ObjectTagRule objectCrane = new ObjectTagRule(new Tag("man_made",
				"crane"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile("{C545A28E-E2EC-11D2-9C84-00105A0CE62A}"));
				add(new ObjectFile("{B2DFD078-603C-48B4-95FA-45EE25582157}"));

			}
		}, 0, true);
		objectsList.add(objectCrane);
		// radio towers
		ObjectTagRule objectRadioTower = new ObjectTagRule(new Tag(
				"tower:type", "communication"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile("{79D0AD2B-C7CF-4433-9C7C-25BA72B3327F}"));
				add(new ObjectFile("{F4CCAF40-00A9-45B9-9412-E4873DE9CA97}"));
				add(new ObjectFile("{C545A290-E2EC-11D2-9C84-00105A0CE62A}"));
			}
		}, 0, true);
		objectsList.add(objectRadioTower);

		// nuclear plants
		ObjectTagRule objectNuclearCentral = new ObjectTagRule(new Tag(
				"generator:source", "nuclear"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile("{DDA775E2-5E3E-435E-9A11-F5A8ECF75D37}"));
			}
		}, 0, true);
		objectsList.add(objectNuclearCentral);

		// power poles
		ObjectTagRule objectPowerPole = new ObjectTagRule(new Tag("power",
				"pole"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile("{54382EC5-B1BD-4A43-AA1A-4F5F529356C3}"));
			}
		}, 0, true);
		objectsList.add(objectPowerPole);
		// storage tanks
		ObjectTagRule objectStorageTank = new ObjectTagRule(new Tag("man_made",
				"storage_tank"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile("{7549DBA5-8710-4C0F-BF62-324244D86C31}"));
				add(new ObjectFile("{DA302E05-6454-4F7A-8C6F-C5FC32A77174}"));

			}
		}, 0, true);
		objectsList.add(objectStorageTank);
		// power stations
		ObjectTagRule objectPowerStations = new ObjectTagRule(new Tag("power",
				"station"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile("{0C0C2054-0ABB-4E86-91A1-B146C3350558}"));
				add(new ObjectFile("{38C55F68-D0EA-418D-8C3D-CC797B80D410}"));
			}
		}, 0, true);
		objectsList.add(objectPowerStations);

		// baseball stadium
		ObjectTagRule objectBaseballStadium = new ObjectTagRule(new Tag(
				"sport", "baseball"), new ArrayList<ObjectFile>() {
			{
				add(new ObjectFile("{0BB07FF0-7FB0-4C88-ACDF-106339FF59D3}"));
			}
		}, 0, true);
		objectsList.add(objectBaseballStadium);
		// churches
		ObjectTagRule objectChurches = new ObjectTagRule(new Tag(
				"place_of_worship:type", "church"),
				new ArrayList<ObjectFile>() {
					{
						add(new ObjectFile(
								"{E75256EE-38FC-49A9-9689-A5F81BC1D2C0}"));
					}
				}, 0, true);
		objectsList.add(objectChurches);

		// churches
		ObjectTagRule objectCathedrals = new ObjectTagRule(new Tag(
				"place_of_worship:type", "cathedral"),
				new ArrayList<ObjectFile>() {
					{
						add(new ObjectFile(
								"{999B34C1-09CC-4172-A314-4A9371A8A8FF}"));
					}
				}, 0, true);
		objectsList.add(objectCathedrals);

		// mosque
		ObjectTagRule objectMosques = new ObjectTagRule(new Tag(
				"place_of_worship:type", "mosque"),
				new ArrayList<ObjectFile>() {
					{
						add(new ObjectFile(
								"{034F8334-FEDE-427E-A025-59B140FADA56}"));
					}
				}, 0, true);
		objectsList.add(objectMosques);
		ObjectsRulesList result = new ObjectsRulesList(objectsList);
		return result;
	}

	public static void saveOptions() throws Osm2xpBusinessException {
		XmlHelper.saveToXml(getOptions(), new File(FSX_OPTIONS_FILE_PATH));

	}

	public static void importObjectsRules(File file) {
	}

	public static void exportObjectsRules(File file) {

	}

}
