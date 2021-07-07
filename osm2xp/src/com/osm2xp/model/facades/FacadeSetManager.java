package com.osm2xp.model.facades;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.osm2xp.exceptions.Osm2xpTechnicalException;
import com.osm2xp.gui.Activator;
import com.osm2xp.translators.BuildingType;
import com.osm2xp.utils.DsfUtils;
import com.osm2xp.utils.FilesUtils;
import com.osm2xp.utils.MiscUtils;
import com.osm2xp.utils.StatusInfo;
import com.osm2xp.utils.helpers.FacadeSetHelper;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;

public class FacadeSetManager {
	
	private static final String FACADES_TARGET_FOLDER_NAME = "facades";

	public static final String FACADE_SETS_PROP = "facadeSets";
	
	protected Multimap<BuildingType, Facade> buildingFacades = HashMultimap.create();
	
	protected Multimap<SpecialFacadeType, Facade> specialFacades = HashMultimap.create();

	private String[] setPaths;

	private static FacadeSetManager facadeSetManager;
	
	private static String lastId = null;
	
	/**
	 * Get {@link FacadeSetManager} for given options
	 * @param facadeSetsStr Facade set paths separated with ';'
	 * @param targetFolder Generation target folder, or <code>null</code> if you need to just check facade set availability
	 * @return {@link FacadeSetManager} instance
	 */
	public static FacadeSetManager getManager(String facadeSetsStr, File targetFolder) {
		String id = facadeSetsStr + (targetFolder != null ? targetFolder.getAbsolutePath() : "");
		if (!id.equals(lastId)) {
			facadeSetManager = new FacadeSetManager(facadeSetsStr, targetFolder);
			lastId = id;
		}
		return facadeSetManager;
	}
	
	public static void clearCache() {
		lastId = null;
	}

	private FacadeSetManager(String facadeSetsStr, File targetFolder) {
		List<FacadeSet> list = new ArrayList<FacadeSet>();
		setPaths = facadeSetsStr.split(File.pathSeparator);
		for (String pathStr : setPaths) {
			if (pathStr.trim().isEmpty()) {
				continue;
			}
			FacadeSet set = loadFacadeSet(pathStr);
			if (set != null) {
				list.add(set);
			}
		}
		String problems = getSpecialFacadeProblems();
		if (!StringUtils.isBlank(problems)) {
			Activator.log(IStatus.WARNING, problems);
			File specFacadesFolder = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation() + "/resources/specfacades");
			FacadeSet set = loadFacadeSet(specFacadesFolder);
			if (set != null) {
				list.add(set);
			}
		}
		list.stream().flatMap(set -> set.getFacades().stream()).forEach(facade -> addFacade(facade));
		checkCopyFacades(targetFolder, !StringUtils.isBlank(problems));
	}
	private FacadeSet loadFacadeSet(String pathStr) {
		File folder = new File(pathStr);
		return loadFacadeSet(folder);
	}
	
	private FacadeSet loadFacadeSet(File folder) {
		if (folder.getName().endsWith(FacadeSetHelper.FACADE_SET_DESCRIPTOR_FILE_NAME)) {
			folder = folder.getParentFile();
		}
		if (folder.isDirectory()) {
			return FacadeSetHelper.getFacadeSet(folder.getAbsolutePath());
		}
		return null;
	}

	private String getSpecialFacadeProblems() {
		StringBuilder builder = new StringBuilder();
		if (XplaneOptionsHelper.getOptions().isGenerateFence() && (!specialFacades.containsKey(SpecialFacadeType.FENCE) || !specialFacades.containsKey(SpecialFacadeType.WALL))) {
			builder.append(" - 'Generate barriers option choosen, but no fence and/or wall facades are present in specified facade sets");
			builder.append('\n');
		}
		if (XplaneOptionsHelper.getOptions().isGenerateTanks() && !specialFacades.containsKey(SpecialFacadeType.TANK)) {
			builder.append(" - 'Generate tanks/gasometers option choosen, but no 'tank' facades are present in specified facade sets");
			builder.append('\n');
		}
		if (XplaneOptionsHelper.getOptions().isGenerateBuildings() && !specialFacades.containsKey(SpecialFacadeType.GARAGE)) {
			builder.append(" - 'Generate buildings option choosen, but no special facades for garages are present in specified facade sets");
			builder.append('\n');
		}
		if (builder.length() > 0) {
			builder.append("Will use built-in default facades");
		}
		return builder.toString();
	}
	
	protected void checkCopyFacades(File targetFolder, boolean copySpecFacades) {
		//Check for non-empty folder was added to avoid copying facades second time for second tile being generated
		if (targetFolder != null) { // && XplaneOptionsHelper.getOptions().isPackageFacades() TODO this option is ignored for now, we always copy facades
			File targetFacadesFolder = new File(targetFolder, FACADES_TARGET_FOLDER_NAME);
			if (targetFacadesFolder.isDirectory() && targetFacadesFolder.list().length > 0) {
				return; //Don't copy facades second time
			}
			if (copySpecFacades) {
				File specFacadesFolder = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation() + "/resources/specfacades");
				if (specFacadesFolder.isDirectory()) {
					try {
						FilesUtils.copyDirectory(specFacadesFolder, targetFacadesFolder,
								false);
					} catch (IOException e) {
						Activator.log(e);
					}
				} else {
					Activator.log(IStatus.ERROR, "Special facades folder not present in resources dir");
				}
			}
			if (XplaneOptionsHelper.getOptions().isGenerateBuildings() 
					|| XplaneOptionsHelper.getOptions().isGenerateFence()
					|| XplaneOptionsHelper.getOptions().isGenerateTanks()) {
				for (String pathStr : setPaths) {
					File folder = new File(pathStr);
					if (pathStr.endsWith(FacadeSetHelper.FACADE_SET_DESCRIPTOR_FILE_NAME)) {
						folder = folder.getParentFile();
					}
					copyFacades(folder, targetFolder);
				}
			}
		}
		
	}

	protected void copyFacades(File srcFolder, File targetFolder) {
		if (srcFolder.isFile()) {
			srcFolder = srcFolder.getParentFile();
		}
		File facadesFolder = new File(targetFolder, FACADES_TARGET_FOLDER_NAME);
		facadesFolder.mkdirs();

		try {
			FilesUtils.copyDirectory(srcFolder, facadesFolder, true);
			DsfUtils.applyFacadeLod(targetFolder);
			if (!XplaneOptionsHelper.getOptions().isHardBuildings()) {
				DsfUtils.removeConcreteRoofsAndWalls(targetFolder);
			}
		} catch (FileNotFoundException e) {
			throw new Osm2xpTechnicalException(e);
		} catch (IOException e) {
			throw new Osm2xpTechnicalException(e);
		}
	}

	protected void addFacade(Facade facade) {
		SpecialFacadeType specialFacadeType = facade.getSpecialType(); //If barrier/tank/garage
		if (specialFacadeType != null) {
			specialFacades.put(specialFacadeType, facade);
			return;
		}
		if (facade.isResidential()) {
			buildingFacades.put(BuildingType.RESIDENTIAL, facade);
		}
		if (facade.isCommercial()) {
			buildingFacades.put(BuildingType.COMMERCIAL, facade);
		}
		if (facade.isIndustrial()) {
			buildingFacades.put(BuildingType.INDUSTRIAL, facade);
		}
		if (!facade.isCommercial() && !facade.isIndustrial() && !facade.isResidential()) { //Residential by default
			buildingFacades.put(BuildingType.RESIDENTIAL, facade);
		}
	}
	
	public Facade getRandomFacade(BuildingType type, int height, boolean simple) {
		Collection<Facade> facades = buildingFacades.get(type);
		if (facades != null && !facades.isEmpty()) {
			List<Facade> matching = facades.stream()
					.filter(facade -> 
					facade.getMaxHeight() == 0 || (facade.getMinHeight() <= height && facade.getMaxHeight() >= height))
					.filter(facade -> !simple ? !facade.isSimpleBuildingOnly() : true)
					.collect(Collectors.toList());
			if (matching.size() > 0) {
				Collections.shuffle(matching);
				return matching.get(0);
			}
		}
		Activator.log(IStatus.WARNING, "Unable to find proper facade for building type " + type + " height= " + height + " simple= " + simple);
		if (facades.size() > 0) {
			Facade[] array = facades.toArray(new Facade[0]);
			return array[new Random().nextInt(array.length)];
		}
		Collection<Facade> values = buildingFacades.values();
		return values.iterator().next();
	}
	
	public List<String> getAllFacadeStrings() {
		List<String> facadeStrings = specialFacades.values().stream().distinct().map(facade -> facade.getFile()).sorted().collect(Collectors.toList());
		facadeStrings.addAll(buildingFacades.values().stream().distinct().map(facade -> facade.getFile()).sorted().collect(Collectors.toList()));
		return facadeStrings;
	}
	
	public Facade getRandomSpecialFacade(SpecialFacadeType specialType) {
		if (specialFacades.isEmpty()) {
			return null;
		}
		Collection<Facade> facades = specialFacades.get(specialType);
		if (facades.isEmpty()) {
			LogFactory.getLog(getClass()).error("No facade registered for type " + specialType);
			return null;
		}
		Facade[] facadeArray = facades.toArray(new Facade[0]);
		return (facadeArray[new Random().nextInt(facades.size())]);
	}
	
	public Facade getRandomHouseSlopedFacade(BuildingType buildingType, double minVector, double height,
			Color buildingColor) {

		
		// find facades which are good in terms of vector size for the sloped
		// roof
		Collection<Facade> goodFacades = buildingFacades.get(buildingType);
		if (goodFacades == null || goodFacades.isEmpty()) { //Use all facades if no facades for this type are registered
			goodFacades = buildingFacades.values();
		}
		List<Facade> resList = goodFacades.stream().filter(facade -> facade.isSloped() && (facade.getMaxVectorLength() == 0 ||
				(facade.getMinVectorLength() <= minVector && facade
						.getMaxVectorLength() >= minVector))).collect(Collectors.toList());
		Collections.shuffle(resList);
		if (buildingColor == null && resList.size() > 0) {
			return resList.get(0);
		}
		// now pick the one with the roof color closest the building one

		Facade facadeResult = null;
		Double colorDiff = null;

		for (Facade facade : resList) {
			// only look at facades with roof color information
			if (StringUtils.isNotBlank(facade.getRoofColor())) {
				// create a color object
				String rgbValues[] = facade.getRoofColor().split(",");
				Color currentFacadeRoofColor = new Color(
						Integer.parseInt(rgbValues[0]),
						Integer.parseInt(rgbValues[1]),
						Integer.parseInt(rgbValues[2]));
				// compute the difference beetween building roof color and
				// facade roof color
				Double colorDifference = MiscUtils.colorDistance(buildingColor,
						currentFacadeRoofColor);

				// store current Facade if good
				if (colorDiff == null || (colorDiff > colorDifference)) {
					colorDiff = colorDifference;
					facadeResult = facade;
				}
			}
		}

		return facadeResult;
	}
	
	public IStatus getFacadeSetStatus() {
		if (!XplaneOptionsHelper.getOptions().isGenerateBuildings() && !XplaneOptionsHelper.getOptions().isGenerateFence()) {
			return Status.OK_STATUS;	
		}
		if (setPaths == null || setPaths.length == 0 || (setPaths.length == 1 && setPaths[0].trim().isEmpty())) {
			return new StatusInfo(IStatus.ERROR, "No facade sets configured. Please add at least one facade set.");
		}
		if (buildingFacades.isEmpty() && specialFacades.isEmpty()) {
			return new StatusInfo(IStatus.ERROR, "All specified facade sets are invalid.");
		}
		if (XplaneOptionsHelper.getOptions().isGenerateBuildings() && buildingFacades.isEmpty()) {
			return new StatusInfo(IStatus.WARNING, "'Generate buildings' option chosen, but no building facades loaded.");
		}
		StringBuilder badPaths = new StringBuilder();
		for (String path : setPaths) {
			if (!new File(path).exists()) {
				if (badPaths.length() > 0) {
					badPaths.append(", ");
				}
				badPaths.append(path);
			}
		}
		if (badPaths.length() > 0) {
			return new StatusInfo(IStatus.WARNING, "Invalid facade path(s): " + badPaths);
		}
		return Status.OK_STATUS;
	}

}
