package com.osm2xp.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import math.geom2d.polygon.LinearRing2D;

import org.apache.commons.lang.StringUtils;

import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.model.facades.Facade;
import com.osm2xp.model.facades.FacadeSet;
import com.osm2xp.model.options.FacadeTagRule;
import com.osm2xp.model.options.ForestTagRule;
import com.osm2xp.model.options.TagsRule;
import com.osm2xp.model.options.XplaneLightTagRule;
import com.osm2xp.model.options.ObjectFile;
import com.osm2xp.model.options.XplaneObjectTagRule;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.model.xplane.XplaneDsf3DObject;
import com.osm2xp.model.xplane.XplaneDsfLightObject;
import com.osm2xp.model.xplane.XplaneDsfObject;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * DsfObjectsProvider.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class DsfObjectsProvider {

	private List<String> objectsList = new ArrayList<String>();
	private List<String> singlesFacadesList = new ArrayList<String>();
	private List<String> facadesList = new ArrayList<String>();
	private List<String> forestsList = new ArrayList<String>();
	private List<String> polygonsList = new ArrayList<String>();
	private List<String> lightsObjectsList = new ArrayList<String>();

	private FacadeSet facadeSet;
	private List<Facade> slopedHousesList;
	private List<Facade> facadesWithRoofColorList;

	/**
	 * @param facadeSet
	 */
	public DsfObjectsProvider(FacadeSet facadeSet) {
		this.facadeSet = facadeSet;
		computePolygonsList();
		computeObjectsList();
	}

	/**
	 * @param facadeSet
	 */
	public DsfObjectsProvider() {
		computePolygonsList();
		computeObjectsList();
	}

	/**
	 * Return the dsf index of the computed facade file
	 * 
	 * @param simpleBuilding
	 * @param residential
	 * @param slopedRoof
	 * @param xVectorLength
	 * @return Integer the facade file index
	 */
	public Integer computeFacadeDsfIndex(Boolean simpleBuilding,
			Boolean residential, Boolean slopedRoof, OsmPolygon osmPolygon) {
		Color roofColor = osmPolygon.getRoofColor();
		Double minVector = osmPolygon.getMinVectorSize();
		Integer height = osmPolygon.getHeight();
		Integer result = null;
		// residential object
		if (residential) {
			// simple 4 points footprint residential house
			if (simpleBuilding) {
				// residential sloped roof
				if (slopedRoof) {
					if (roofColor != null) {

						result = getRandomHouseSlopedFacade(minVector, height,
								roofColor);
						if (result == null) {
							result = getRandomHouseSlopedFacade(minVector,
									height);
						}
					} else {

						result = getRandomHouseSlopedFacade(minVector, height);
						if (result == null) {
							// System.out.println("min vector=" + minVector
							// + "                " + (slope++));
						}
					}
					// if no sloped house has been found, get a simple house
					if (result == null) {
						result = getRandomSimpleFootprintHouseFacade(height);

					}
					// if still null get a ComplexFootprintHouseFacade
					if (result == null) {
						return getRandomComplexFootprintHouseFacade(height);
					}

				}
				// residential simple flat roofs
				else {
					result = getRandomSimpleFootprintHouseFacade(height);

					if (result == null) {
						return getRandomComplexFootprintHouseFacade(height);
					}
				}
			}
			// complex footprint residential house
			else {
				if (roofColor != null) {
					result = getRandomComplexFootprintHouseFacade(height,
							roofColor);
				} else {
					result = getRandomComplexFootprintHouseFacade(height);
				}

			}
		}

		// building object
		else {
			// simple 4 points footprint building
			if (simpleBuilding) {

				// building sloped roof
				if (slopedRoof) {
					result = getRandomBuildingSlopedFacade(minVector, height);
				}
				// building flat roof
				else {
					result = getRandomSimpleFootprintBuildingFacade(height);
				}
			}
			// complex footprint building
			else {
				result = getRandomComplexFootprintBuildingFacade(height);
			}

		}
		
		if (result == null) { //
			int idx = Math.min(facadeSet.getFacades().size() - 1, (int) (Math.random() * facadeSet.getFacades().size()));
			result = polygonsList.indexOf(facadeSet.getFacades().get(idx).getFile());
		}

		return result;
	}

	/**
	 * return the dsf index of a random residential sloped facade file
	 * 
	 * @param minVector
	 * @return Integer the facade file
	 */
	private Integer getRandomHouseSlopedFacade(double minVector, double height) {
		if (this.facadeSet != null) {
			Collections.shuffle(facadeSet.getFacades());
			for (Facade facade : this.getSlopedHousesList()) {
				if ((facade.getMinVectorLength() <= minVector && facade
						.getMaxVectorLength() >= minVector)) {
					return polygonsList.indexOf(facade.getFile());
				}
			}
		}

		return null;
	}

	/**
	 * return the dsf index of a random residential sloped facade file
	 * 
	 * @param minVector
	 * @return Integer the facade file
	 */
	private Integer getRandomHouseSlopedFacade(double minVector, double height,
			Color buildingColor) {

		// find facades which are good in terms of vector size for the sloped
		// roof
		List<Facade> goodFacades = new ArrayList<Facade>();
		if (this.facadeSet != null) {
			List<Facade> slopedFacades = this.getSlopedHousesList();
			Collections.shuffle(slopedFacades);
			for (Facade facade : slopedFacades) {
				if ((facade.getMinVectorLength() <= minVector && facade
						.getMaxVectorLength() >= minVector)) {
					goodFacades.add(facade);
				}
			}
		}

		// now pick the one with the roof color closest the building one

		Facade facadeResult = null;
		Integer result = null;
		Double colorDiff = null;

		for (Facade facade : goodFacades) {
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

		if (facadeResult != null) {
			result = polygonsList.indexOf(facadeResult.getFile());
		}

		return result;
	}

	private Integer getRandomComplexFootprintBuildingFacade(double height) {
		if (this.facadeSet != null) {
			Collections.shuffle(facadeSet.getFacades());
			for (Facade facade : this.facadeSet.getFacades()) {
				if ((facade.isCommercial() || facade.isIndustrial())
						&& !facade.isSimpleBuildingOnly()) {
					return polygonsList.indexOf(facade.getFile());
				}
			}
		}
		return null;
	}

	/**
	 * @param height
	 * @return
	 */
	private Integer getRandomSimpleFootprintBuildingFacade(double height) {
		if (this.facadeSet != null) {
			Collections.shuffle(facadeSet.getFacades());
			for (Facade facade : this.facadeSet.getFacades()) {
				if ((facade.isCommercial() || facade.isIndustrial())
						&& facade.isSimpleBuildingOnly()) {
					// check height
					if (facade.getMinHeight() <= height
							&& (facade.getMaxHeight() == 0 || (facade
									.getMaxHeight() != 0 && facade
									.getMaxHeight() >= height))) {
						return polygonsList.indexOf(facade.getFile());
					}
				}
			}
		}
		Osm2xpLogger
				.warning("No facade found for simple footprint building, with height="
						+ height);
		return null;
	}

	/**
	 * @param minVector
	 * @param height
	 * @return
	 */
	private Integer getRandomBuildingSlopedFacade(double minVector,
			double height) {
		if (this.facadeSet != null) {
			Collections.shuffle(facadeSet.getFacades());
			for (Facade facade : this.facadeSet.getFacades()) {
				if ((facade.isCommercial() || facade.isIndustrial())
						&& facade.isSloped()
						&& (facade.getMinVectorLength() <= minVector && facade
								.getMaxVectorLength() >= minVector)) {
					return polygonsList.indexOf(facade.getFile());
				}
			}
		}
		return null;
	}

	/**
	 * @param height
	 * @return
	 */
	private Integer getRandomComplexFootprintHouseFacade(double height) {
		if (this.facadeSet != null) {
			Collections.shuffle(facadeSet.getFacades());
			for (Facade facade : this.facadeSet.getFacades()) {
				if (facade.isResidential() && !facade.isSimpleBuildingOnly()) {
					return polygonsList.indexOf(facade.getFile());
				}
			}
		}
		return null;
	}

	/**
	 * @param height
	 * @return
	 */
	private Integer getRandomSimpleFootprintHouseFacade(double height) {

		if (this.facadeSet != null) {
			Collections.shuffle(facadeSet.getFacades());
			for (Facade facade : this.facadeSet.getFacades()) {
				if (facade.isResidential() && facade.isSimpleBuildingOnly()
						&& !facade.isSloped()) {
					return polygonsList.indexOf(facade.getFile());
				}
			}
		}
		return null;
	}

	/**
	 * @param height
	 * @return
	 */
	private Integer getRandomComplexFootprintHouseFacade(double height,
			Color buildingColor) {

		Facade facadeResult = null;
		Integer result = null;
		Double colorDiff = null;
		if (this.facadeSet != null) {
			Collections.shuffle(facadeSet.getFacades());

			for (Facade facade : this.facadeSet.getFacades()) {
				if (StringUtils.isNotBlank(facade.getRoofColor())
						&& facade.isResidential()
						&& !facade.isSimpleBuildingOnly()) {
					String rgbValues[] = facade.getRoofColor().split(",");
					Color currentFacadeRoofColor = new Color(
							Integer.parseInt(rgbValues[0]),
							Integer.parseInt(rgbValues[1]),
							Integer.parseInt(rgbValues[2]));

					Double colorDifference = MiscUtils.colorDistance(
							buildingColor, currentFacadeRoofColor);

					if (colorDiff == null || (colorDiff > colorDifference)) {
						colorDiff = colorDifference;
						facadeResult = facade;
					}

				}
			}
			if (facadeResult != null) {
				result = polygonsList.indexOf(facadeResult.getFile());
			}
		}
		return result;

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
			for (Facade facade : facadeSet.getFacades()) {
				facadesList.add(facade.getFile());
			}
			polygonsList.addAll(facadesList);
		}

	}

	/**
	 * 
	 */
	public void computeObjectsList() {
		objectsList.clear();
		// add 3D objects
		for (XplaneObjectTagRule object : XplaneOptionsHelper.getOptions()
				.getObjectsRules().getRules()) {
			for (ObjectFile file : object.getObjectsFiles()) {
				if (!objectsList.contains(file.getPath())) {
					objectsList.add(file.getPath());
				}

			}
		}

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

	/**
	 * 
	 */
	private void computeSlopedHousesFacadesList() {
		this.slopedHousesList = new ArrayList<Facade>();
		for (Facade facade : this.facadeSet.getFacades()) {
			if (facade.isResidential() && facade.isSloped()) {
				slopedHousesList.add(facade);

			}
		}
	}

	/**
	 * 
	 */
	private void computeFacadesWithRoofColorList() {
		this.facadesWithRoofColorList = new ArrayList<Facade>();
		for (Facade facade : this.facadeSet.getFacades()) {
			if (facade.getRoofColor() != null) {
				facadesWithRoofColorList.add(facade);

			}
		}
	}

	public List<Facade> getFacadesWithRoofColorList() {
		if (facadesWithRoofColorList == null) {
			computeFacadesWithRoofColorList();
		}
		return facadesWithRoofColorList;
	}

	public List<Facade> getSlopedHousesList() {
		if (slopedHousesList == null) {
			computeSlopedHousesFacadesList();
		}
		return slopedHousesList;
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

}
