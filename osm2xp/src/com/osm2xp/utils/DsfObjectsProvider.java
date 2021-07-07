package com.osm2xp.utils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;

import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.gui.Activator;
import com.osm2xp.model.facades.Facade;
import com.osm2xp.model.facades.FacadeSetManager;
import com.osm2xp.model.facades.SpecialFacadeType;
import com.osm2xp.model.options.FacadeTagRule;
import com.osm2xp.model.options.ForestTagRule;
import com.osm2xp.model.options.ObjectFile;
import com.osm2xp.model.options.TagsRule;
import com.osm2xp.model.options.XplaneLightTagRule;
import com.osm2xp.model.options.XplaneObjectTagRule;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.model.osm.OsmPolyline;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.model.xplane.XplaneDsf3DObject;
import com.osm2xp.model.xplane.XplaneDsfLightObject;
import com.osm2xp.model.xplane.XplaneDsfObject;
import com.osm2xp.translators.BuildingType;
import com.osm2xp.utils.geometry.GeomUtils;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;

import math.geom2d.Box2D;
import math.geom2d.polygon.LinearRing2D;

/**
 * DsfObjectsProvider.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class DsfObjectsProvider {

	public static final String OBJECTS_TARGET_FOLDER_NAME = "objects";
	public static final String SPECIAL_OBJECTS_TARGET_FOLDER_NAME = "specobjects";
	public static final String FORESTS_TARGET_FOLDER_NAME = "forests";
	private List<String> objectsList = new ArrayList<String>();
	private List<String> singlesFacadesList = new ArrayList<String>();
	private List<String> facadesList = new ArrayList<String>();
	private List<String> forestsList = new ArrayList<String>();
	private List<String> polygonsList = new ArrayList<String>();
	private List<String> lightsObjectsList = new ArrayList<String>();

	private FacadeSetManager facadeSetManager;
	private Box2D exclusionBox;
	private String targetFolderPath;

	/**
	 * @param facadeSet
	 */
	public DsfObjectsProvider(String folderPath, FacadeSetManager facadeSetManager) {
		this.facadeSetManager = facadeSetManager;
		this.targetFolderPath = folderPath;
		computePolygonsList();
		computeObjectsList();
	}

	/**
	 * @param folderPath target folder path
	 */
	public DsfObjectsProvider(String folderPath) {
		this.targetFolderPath = folderPath;
		computePolygonsList();
		computeObjectsList();
	}

	/**
	 * Return the dsf index of the computed facade file
	 * 
	 * @param simpleBuilding
	 * @param buildingType
	 * @param slopedRoof
	 * @param xVectorLength
	 * @return Integer the facade file index
	 */
	public Integer computeFacadeDsfIndex(Boolean simpleBuilding,
			BuildingType buildingType, Boolean slopedRoof, OsmPolygon osmPolygon) {
		Color roofColor = osmPolygon.getRoofColor();
		Double minVector = osmPolygon.getMinVectorSize();
		Integer height = osmPolygon.getHeight();
		Facade resFacade = null;
		if (slopedRoof) {
			resFacade = facadeSetManager.getRandomHouseSlopedFacade(buildingType, minVector, height, roofColor); 
		}
		if (resFacade == null) {
			resFacade = facadeSetManager.getRandomFacade(buildingType,height,simpleBuilding);
		}
		
		return polygonsList.indexOf(resFacade.getFile());
	}
	
	public Integer computeSpecialFacadeDsfIndex(SpecialFacadeType specialFacadeType, OsmPolyline polygon) {
		Facade randomBarrierFacade = facadeSetManager.getRandomSpecialFacade(specialFacadeType);
		if (randomBarrierFacade != null) {
			return polygonsList.indexOf(randomBarrierFacade.getFile());
		}
		return -1;
	}


	/**
	 * @param facadeSet
	 * @throws Osm2xpBusinessException
	 */
	public void computePolygonsList() {

		facadesList.clear();
		forestsList.clear();
		polygonsList.clear();
		singlesFacadesList.clear();

		// FORESTS RULES
		if (XplaneOptionsHelper.getOptions().isGenerateFor()) {

			for (ForestTagRule forest : XplaneOptionsHelper.getOptions()
					.getForestsRules().getRules()) {
				for (ObjectFile file : forest.getObjectsFiles()) {
					if (!forestsList.contains(file.getPath())) {
						forestsList.add(file.getPath());
					}

				}
			}
			copyForestFiles();
			polygonsList.addAll(forestsList);
			
		}
		// FACADES RULES
		if (!XplaneOptionsHelper.getOptions().getFacadesRules().getRules()
				.isEmpty()) {
			for (FacadeTagRule facadeTagRule : XplaneOptionsHelper.getOptions()
					.getFacadesRules().getRules()) {
				for (ObjectFile file : facadeTagRule.getObjectsFiles()) {
					if (!singlesFacadesList.contains(file.getPath())) {
						singlesFacadesList.add(file.getPath());
					}
				}
			}
			polygonsList.addAll(singlesFacadesList);
		}

		// BASIC BUILDINGS FACADES
		if (XplaneOptionsHelper.getOptions().isGenerateBuildings()) {
			facadesList.addAll(facadeSetManager.getAllFacadeStrings());
			polygonsList.addAll(facadesList);
		}

	}

	/**
	 * 
	 */
	public void computeObjectsList() {
		objectsList.clear();
		// add 3D objects
//		for (XplaneObjectTagRule object : XplaneOptionsHelper.getOptions() TODO Not needed, since we register object during copy operation
//				.getObjectsRules().getRules()) {
//			for (ObjectFile file : object.getObjectsFiles()) {
//				if (!objectsList.contains(file.getPath())) {
//					objectsList.add(file.getPath());
//				}
//
//			}
//		}
		
		//add special 3d objects (e.g. chimneys)
		add3DObjects();

		// add lights objects
		for (XplaneLightTagRule object : XplaneOptionsHelper.getOptions()
				.getLightsRules().getRules()) {
			for (ObjectFile file : object.getObjectsFiles()) {
				if (!objectsList.contains(file.getPath())) {
					objectsList.add(file.getPath());
				}

			}
		}
	}
	
	private void copyForestFiles() {
		File forestsFolder = new File(
				ResourcesPlugin.getWorkspace().getRoot().getLocation() + "/resources/forests");
		if (forestsFolder.isDirectory()) {
			try {
				FilesUtils.copyDirectory(forestsFolder, new File(targetFolderPath, FORESTS_TARGET_FOLDER_NAME),
						false);
			} catch (IOException e) {
				Activator.log(e);
			}
		}
	}

	private void add3DObjects() {
		if (XplaneOptionsHelper.getOptions().isGenerateChimneys() || XplaneOptionsHelper.getOptions().isGenerateObj() ) {
				File objectsFolder = new File(
						ResourcesPlugin.getWorkspace().getRoot().getLocation() + "/resources/objects");
				if (objectsFolder.isDirectory()) {
					registerAndCopyObjectsFolder(objectsFolder, OBJECTS_TARGET_FOLDER_NAME);
				} else {
					Activator.log(IStatus.ERROR, "Special facades folder not present in resources dir");
				} 
				File specObjectsFolder = new File(
						ResourcesPlugin.getWorkspace().getRoot().getLocation() + "/resources/specobjects");
				if (specObjectsFolder.isDirectory()) {
					registerAndCopyObjectsFolder(specObjectsFolder, SPECIAL_OBJECTS_TARGET_FOLDER_NAME);
				} else {
					Activator.log(IStatus.ERROR, "Special facades folder not present in resources dir");
				} 
			}
	}

	protected void registerAndCopyObjectsFolder(File objectsFolder, String targetSubfolder) {
		try {
			FilesUtils.copyDirectory(objectsFolder, new File(targetFolderPath, targetSubfolder),
					false);
		} catch (IOException e) {
			Activator.log(e);
		}
		File[] objFiles = objectsFolder.listFiles((parent, name) -> name.toLowerCase().endsWith(".obj"));
		for (File file : objFiles) {
			objectsList.add(targetSubfolder + "/" + file.getName());
		}
	}

	/**
	 * @param facadeTagRule
	 * @return
	 */
	public Integer getRandomSingleFacade(FacadeTagRule facadeTagRule) {
		Collections.shuffle(singlesFacadesList);
		String randomSingleFacade = singlesFacadesList.get(0);
		return polygonsList.indexOf(randomSingleFacade);
	}

	/**
	 * @param tagRule
	 * @return
	 */
	public Integer getRandomObject(TagsRule tagRule) {
		Collections.shuffle(tagRule.getObjectsFiles());
		String objectFile = tagRule.getObjectsFiles().get(0).getPath();
		return objectsList.indexOf(objectFile);
	}
	
	/**
	 * @return
	 */
	@Deprecated
	public Integer getRandomStreetLightObject() {
		return null;
	}

	/**
	 * @param forestTagRule
	 * @return
	 */
	public Integer getRandomForest(ForestTagRule forestTagRule) {
		Collections.shuffle(forestTagRule.getObjectsFiles());
		String forestFile = forestTagRule.getObjectsFiles().get(0).getPath();
		return polygonsList.indexOf(forestFile);
	}

	/**
	 * return a random object index and the angle for the first matching rule
	 * 
	 * @param tags
	 * @return
	 */
	public XplaneDsf3DObject getRandomDsfObjectIndexAndAngle(List<Tag> tags,
			Long id) {
		XplaneDsf3DObject result = null;
		for (Tag tag : tags) {
			for (XplaneObjectTagRule objectTagRule : XplaneOptionsHelper
					.getOptions().getObjectsRules().getRules()) {
				if ((objectTagRule.getTag().getKey().equalsIgnoreCase("id") && objectTagRule
						.getTag().getValue()
						.equalsIgnoreCase(String.valueOf(id)))
						|| (OsmUtils.compareTags(objectTagRule.getTag(), tag))) {
					result = new XplaneDsf3DObject();
					result.setRule(objectTagRule);
					result.setDsfIndex(getRandomObject(objectTagRule));
					if (objectTagRule.isRandomAngle()) {
						Random randomGenerator = new Random();
						result.setAngle(randomGenerator.nextInt(360));
					} else {
						result.setAngle(objectTagRule.getAngle());
					}
					break;
				}
			}
		}
		return result;
	}

	/**
	 * return a random object index and the angle for the first matching rule
	 * 
	 * @param tags
	 * @return
	 */
	public XplaneDsfObject getRandomDsfObject(OsmPolygon osmPolygon) {
		LinearRing2D polygon = osmPolygon.getPolygon();
		XplaneDsfObject result = null;
		// shuffle rules
		List<XplaneObjectTagRule> tagsRules = new ArrayList<XplaneObjectTagRule>();
		tagsRules.addAll(XplaneOptionsHelper.getOptions().getObjectsRules()
				.getRules());
		Collections.shuffle(tagsRules);
		for (Tag tag : osmPolygon.getTags()) {
			for (XplaneObjectTagRule rule : tagsRules) {
				// check Tag matching
				if ((rule.getTag().getKey().equalsIgnoreCase("id") && rule
						.getTag().getValue()
						.equalsIgnoreCase(String.valueOf(osmPolygon.getId())))
						|| (OsmUtils.compareTags(rule.getTag(), tag))) {
					// check rule options

					Boolean checkArea = !rule.isAreaCheck()
							|| (rule.isAreaCheck() && (osmPolygon.getArea() > rule
									.getMinArea() && osmPolygon.getArea() < rule
									.getMaxArea()));

					Boolean checkSize = !rule.isSizeCheck()
							|| GeomUtils.isRectangleBigEnoughForObject(
									rule.getxVectorMaxLength(),
									rule.getyVectorMaxLength(),
									rule.getxVectorMinLength(),
									rule.getyVectorMinLength(), polygon);

					Boolean checkSimplePoly = !rule.isSimplePolygonOnly()
							|| (rule.isSimplePolygonOnly() && osmPolygon
									.isSimplePolygon());

					if (checkArea && checkSize && checkSimplePoly) {
						result = new XplaneDsf3DObject(osmPolygon, rule);
						// compute object index
						result.setDsfIndex(getRandomObject(rule));

					}
				}
			}
		}
		return result;
	}

	/**
	 * @param tags
	 * @param dsfObjectsProvider
	 * @return
	 */
	public Integer[] getRandomForestIndexAndDensity(List<Tag> tags) {
		for (Tag tag : tags) {
			for (ForestTagRule forestTagRule : XplaneOptionsHelper.getOptions()
					.getForestsRules().getRules()) {
				if (OsmUtils.compareTags(forestTagRule.getTag(), tag)) {
					Integer[] result = new Integer[2];
					result[0] = getRandomForest(forestTagRule);
					result[1] = forestTagRule.getForestDensity();
					return result;

				}
			}
		}
		return null;
	}

	/**
	 * @return the objectsList
	 */
	public List<String> getObjectsList() {
		return objectsList;
	}

	/**
	 * @param objectsList
	 *            the objectsList to set
	 */
	public void setObjectsList(List<String> objectsList) {
		this.objectsList = objectsList;
	}

	/**
	 * @return the singlesFacadesList
	 */
	public List<String> getSinglesFacadesList() {
		return singlesFacadesList;
	}

	/**
	 * @param singlesFacadesList
	 *            the singlesFacadesList to set
	 */
	public void setSinglesFacadesList(List<String> singlesFacadesList) {
		this.singlesFacadesList = singlesFacadesList;
	}

	/**
	 * @return the facadesList
	 */
	public List<String> getFacadesList() {
		return facadesList;
	}

	/**
	 * @param facadesList
	 *            the facadesList to set
	 */
	public void setFacadesList(List<String> facadesList) {
		this.facadesList = facadesList;
	}

	/**
	 * @return the forestsList
	 */
	public List<String> getForestsList() {
		return forestsList;
	}

	/**
	 * @param forestsList
	 *            the forestsList to set
	 */
	public void setForestsList(List<String> forestsList) {
		this.forestsList = forestsList;
	}

	/**
	 * @return the polygonsList
	 */
	public List<String> getPolygonsList() {
		return polygonsList;
	}

	/**
	 * @param polygonsList
	 *            the polygonsList to set
	 */
	public void setPolygonsList(List<String> polygonsList) {
		this.polygonsList = polygonsList;
	}

	public XplaneDsfObject getRandomDsfLightObject(OsmPolygon osmPolygon) {
		XplaneDsfObject result = null;
		// shuffle rules
		List<XplaneLightTagRule> tagsRules = new ArrayList<XplaneLightTagRule>();
		tagsRules.addAll(XplaneOptionsHelper.getOptions().getLightsRules()
				.getRules());
		Collections.shuffle(tagsRules);
		for (Tag tag : osmPolygon.getTags()) {
			for (XplaneLightTagRule rule : tagsRules) {
				// check Tag matching
				if ((rule.getTag().getKey().equalsIgnoreCase("id") && rule
						.getTag().getValue()
						.equalsIgnoreCase(String.valueOf(osmPolygon.getId())))
						|| (OsmUtils.compareTags(rule.getTag(), tag))) {
					// percentage check
					Random rand = new Random();
					int min = 0;
					int max = 100;
					int percentage = rand.nextInt(max - min + 1) + min;
					if (percentage < rule.getPercentage()) {
						result = new XplaneDsfLightObject(osmPolygon, rule);
						// compute object index
						result.setDsfIndex(getRandomObject(rule));
					}
				}
			}
		}
		return result;
	}

	public void setExclusionBox(Box2D boundingBox) {
		this.exclusionBox = boundingBox;
	}

	public Box2D getExclusionBox() {
		return exclusionBox;
	}

	public Integer getSpecialObject(String specialObjectFile) {
		return objectsList.indexOf(SPECIAL_OBJECTS_TARGET_FOLDER_NAME + "/" + specialObjectFile);
	}

}
