package com.osm2xp.model.options;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * XplaneOptions.
 * 
 * @author Benjamin Blanchet
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "excludeObj", "excludeFac", "excludeFor",
		"smartExclusions", "smartExclusionSize", "smartExclusionDistance",
		"excludeBch", "excludeNet", "excludeLin", "excludePol", "excludeStr",
		"residentialMin", "residentialMax", "buildingMin", "buildingMax",
		"minHouseSegment", "maxHouseSegment", "minHouseArea", "generateObj",
		"generateFor", "generateBuildings","generatePowerlines","generateRailways","generateRoads","generateFence",
		"generateTanks","generateChimneys","generateBridges","generateSlopedRoofs", "generateStreetLights", 
		"lightsDensity", "packageFacades","hardBuildings", "lightObject", "facadeLod", 
		"generateXmlStats", "generatePdfStats", "generateDebugImg", "buildingsExclusions",
		"forestsRules", "objectsRules", "lightsRules", "facadesRules",
		"streetLightObjects" })
@XmlRootElement(name = "XplaneOptions")
public class XplaneOptions {

	protected boolean excludeObj;
	protected boolean excludeFac;
	protected boolean excludeFor;
	protected boolean excludeBch;
	protected boolean excludeNet;
	protected boolean excludeLin;
	protected boolean excludePol;
	protected boolean excludeStr;
	protected boolean smartExclusions;
	protected int residentialMin;
	protected int residentialMax;
	protected int buildingMin;
	protected int buildingMax;
	protected int minHouseSegment;
	protected int maxHouseSegment;
	protected int minHouseArea;
	protected int smartExclusionSize;
	protected int smartExclusionDistance;
	protected boolean generateObj;
	protected boolean generateFor = true;
	protected boolean generateBuildings = true;
	protected boolean generatePowerlines = true;
	protected boolean generateRailways = true;
	protected boolean generateRoads = true;
	protected boolean generateFence = true;
	protected boolean generateTanks = true;
	protected boolean generateChimneys = true;
	protected boolean generateBridges = true;
	protected boolean generateStreetLights;
	protected boolean generateSlopedRoofs;
	protected int lightsDensity;
	protected boolean packageFacades;
	protected boolean hardBuildings;
	@XmlElement(required = true)
	protected String lightObject;
	protected int facadeLod;
	@XmlElement(required = true)
//	protected String facadeSet;
	protected boolean generateXmlStats;
	protected boolean generatePdfStats;
	protected boolean generateDebugImg = false;
	@XmlElement(name = "BuildingsExclusions", required = true)
	protected BuildingsExclusionsList buildingsExclusions;
	@XmlElement(name = "ForestsRules", required = true)
	protected ForestsRulesList forestsRules;
	@XmlElement(name = "ObjectsRules", required = true)
	protected XplaneObjectsRulesList objectsRules;
	@XmlElement(name = "LightsRules", required = true)
	protected LightsRulesList lightsRules;
	@XmlElement(name = "FacadesRules", required = true)
	protected FacadesRulesList facadesRules;
	@XmlElement(name = "StreetLightObjects", required = true)
	protected ObjectsList streetLightObjects;

	/**
	 * Default no-arg constructor
	 * 
	 */
	public XplaneOptions() {
		super();
	}

	/**
	 * Fully-initialising value constructor
	 * 
	 */
	public XplaneOptions(final boolean excludeObj, final boolean excludeFac,
			final boolean excludeFor, final boolean excludeBch,
			final boolean excludeNet, final boolean excludeLin,
			final boolean excludePol, final boolean excludeStr,
			final int residentialMin, final int residentialMax,
			final int buildingMin, final int buildingMax,
			final int minHouseSegment, final int maxHouseSegment,
			final int minHouseArea, final boolean generateObj,
			final boolean generateFor, final boolean generateStreetLights,
			final boolean generateBuildings, final boolean generateSlopedRoofs,
			final int lightsDensity, final boolean packageFacades,
			final boolean hardBuildings, final String lightObject,
			final int facadeLod, final boolean generateXmlStats, final boolean generatePdfStats,
			final BuildingsExclusionsList buildingsExclusions,
			final ForestsRulesList forestsRules,
			final LightsRulesList lightsRules,
			final XplaneObjectsRulesList objectsRules,
			final FacadesRulesList facadesRules,
			final ObjectsList streetLightObjects,
			final int smartExclusionDistance, final int smartExclusionSize) {
		this.excludeObj = excludeObj;
		this.excludeFac = excludeFac;
		this.excludeFor = excludeFor;
		this.excludeBch = excludeBch;
		this.excludeNet = excludeNet;
		this.excludeLin = excludeLin;
		this.excludePol = excludePol;
		this.excludeStr = excludeStr;
		this.residentialMin = residentialMin;
		this.residentialMax = residentialMax;
		this.buildingMin = buildingMin;
		this.buildingMax = buildingMax;
		this.minHouseSegment = minHouseSegment;
		this.maxHouseSegment = maxHouseSegment;
		this.minHouseArea = minHouseArea;
		this.generateObj = generateObj;
		this.generateFor = generateFor;
		this.generateStreetLights = generateStreetLights;
		this.generateBuildings = generateBuildings;
		this.generateSlopedRoofs = generateSlopedRoofs;
		this.lightsDensity = lightsDensity;
		this.packageFacades = packageFacades;
		this.hardBuildings = hardBuildings;
		this.lightObject = lightObject;
		this.facadeLod = facadeLod;
//		this.facadeSet = facadeSet;
		this.generateXmlStats = generateXmlStats;
		this.generatePdfStats = generatePdfStats;
		this.buildingsExclusions = buildingsExclusions;
		this.forestsRules = forestsRules;
		this.objectsRules = objectsRules;
		this.facadesRules = facadesRules;
		this.streetLightObjects = streetLightObjects;
		this.smartExclusionDistance = smartExclusionDistance;
		this.smartExclusionSize = smartExclusionSize;
		this.lightsRules = lightsRules;
	}

	/**
	 * Gets the value of the excludeObj property.
	 * 
	 */
	public boolean isExcludeObj() {
		return excludeObj;
	}

	/**
	 * Sets the value of the excludeObj property.
	 * 
	 */
	public void setExcludeObj(boolean value) {
		this.excludeObj = value;
	}

	/**
	 * Gets the value of the excludeFac property.
	 * 
	 */
	public boolean isExcludeFac() {
		return excludeFac;
	}

	/**
	 * Sets the value of the excludeFac property.
	 * 
	 */
	public void setExcludeFac(boolean value) {
		this.excludeFac = value;
	}

	/**
	 * Gets the value of the excludeFor property.
	 * 
	 */
	public boolean isExcludeFor() {
		return excludeFor;
	}

	/**
	 * Sets the value of the excludeFor property.
	 * 
	 */
	public void setExcludeFor(boolean value) {
		this.excludeFor = value;
	}

	/**
	 * Gets the value of the excludeBch property.
	 * 
	 */
	public boolean isExcludeBch() {
		return excludeBch;
	}

	/**
	 * Sets the value of the excludeBch property.
	 * 
	 */
	public void setExcludeBch(boolean value) {
		this.excludeBch = value;
	}

	/**
	 * Gets the value of the excludeNet property.
	 * 
	 */
	public boolean isExcludeNet() {
		return excludeNet;
	}

	/**
	 * Sets the value of the excludeNet property.
	 * 
	 */
	public void setExcludeNet(boolean value) {
		this.excludeNet = value;
	}

	/**
	 * Gets the value of the excludeLin property.
	 * 
	 */
	public boolean isExcludeLin() {
		return excludeLin;
	}

	/**
	 * Sets the value of the excludeLin property.
	 * 
	 */
	public void setExcludeLin(boolean value) {
		this.excludeLin = value;
	}

	/**
	 * Gets the value of the excludePol property.
	 * 
	 */
	public boolean isExcludePol() {
		return excludePol;
	}

	/**
	 * Sets the value of the excludePol property.
	 * 
	 */
	public void setExcludePol(boolean value) {
		this.excludePol = value;
	}

	/**
	 * Gets the value of the excludeStr property.
	 * 
	 */
	public boolean isExcludeStr() {
		return excludeStr;
	}

	/**
	 * Sets the value of the excludeStr property.
	 * 
	 */
	public void setExcludeStr(boolean value) {
		this.excludeStr = value;
	}

	/**
	 * Gets the value of the residentialMin property.
	 * 
	 */
	public int getResidentialMin() {
		return residentialMin;
	}

	/**
	 * Sets the value of the residentialMin property.
	 * 
	 */
	public void setResidentialMin(int value) {
		this.residentialMin = value;
	}

	/**
	 * Gets the value of the residentialMax property.
	 * 
	 */
	public int getResidentialMax() {
		return residentialMax;
	}

	/**
	 * Sets the value of the residentialMax property.
	 * 
	 */
	public void setResidentialMax(int value) {
		this.residentialMax = value;
	}

	/**
	 * Gets the value of the buildingMin property.
	 * 
	 */
	public int getBuildingMin() {
		return buildingMin;
	}

	/**
	 * Sets the value of the buildingMin property.
	 * 
	 */
	public void setBuildingMin(int value) {
		this.buildingMin = value;
	}

	/**
	 * Gets the value of the buildingMax property.
	 * 
	 */
	public int getBuildingMax() {
		return buildingMax;
	}

	/**
	 * Sets the value of the buildingMax property.
	 * 
	 */
	public void setBuildingMax(int value) {
		this.buildingMax = value;
	}

	/**
	 * Gets the value of the minHouseSegment property.
	 * 
	 */
	public int getMinHouseSegment() {
		return minHouseSegment;
	}

	/**
	 * Sets the value of the minHouseSegment property.
	 * 
	 */
	public void setMinHouseSegment(int value) {
		this.minHouseSegment = value;
	}

	/**
	 * Gets the value of the maxHouseSegment property.
	 * 
	 */
	public int getMaxHouseSegment() {
		return maxHouseSegment;
	}

	/**
	 * Sets the value of the maxHouseSegment property.
	 * 
	 */
	public void setMaxHouseSegment(int value) {
		this.maxHouseSegment = value;
	}

	/**
	 * Gets the value of the minHouseArea property.
	 * 
	 */
	public int getMinHouseArea() {
		return minHouseArea;
	}

	/**
	 * Sets the value of the minHouseArea property.
	 * 
	 */
	public void setMinHouseArea(int value) {
		this.minHouseArea = value;
	}

	/**
	 * Gets the value of the generateObj property.
	 * 
	 */
	public boolean isGenerateObj() {
		return generateObj;
	}

	/**
	 * Sets the value of the generateObj property.
	 * 
	 */
	public void setGenerateObj(boolean value) {
		this.generateObj = value;
	}

	/**
	 * Gets the value of the generateFor property.
	 * 
	 */
	public boolean isGenerateFor() {
		return generateFor;
	}

	/**
	 * Sets the value of the generateFor property.
	 * 
	 */
	public void setGenerateFor(boolean value) {
		this.generateFor = value;
	}

	/**
	 * Gets the value of the generateStreetLights property.
	 * 
	 */
	public boolean isGenerateStreetLights() {
		return generateStreetLights;
	}

	/**
	 * Sets the value of the generateStreetLights property.
	 * 
	 */
	public void setGenerateStreetLights(boolean value) {
		this.generateStreetLights = value;
	}

	/**
	 * Gets the value of the generateBuildings property.
	 * 
	 */
	public boolean isGenerateBuildings() {
		return generateBuildings;
	}

	/**
	 * Sets the value of the generateBuildings property.
	 * 
	 */
	public void setGenerateBuildings(boolean value) {
		this.generateBuildings = value;
	}

	/**
	 * Gets the value of the generateSlopedRoofs property.
	 * 
	 */
	public boolean isGenerateSlopedRoofs() {
		return generateSlopedRoofs;
	}

	/**
	 * Sets the value of the generateSlopedRoofs property.
	 * 
	 */
	public void setGenerateSlopedRoofs(boolean value) {
		this.generateSlopedRoofs = value;
	}

	/**
	 * Gets the value of the lightsDensity property.
	 * 
	 */
	public int getLightsDensity() {
		return lightsDensity;
	}

	/**
	 * Sets the value of the lightsDensity property.
	 * 
	 */
	public void setLightsDensity(int value) {
		this.lightsDensity = value;
	}

	/**
	 * Gets the value of the packageFacades property.
	 * 
	 */
	public boolean isPackageFacades() {
		return packageFacades;
	}

	/**
	 * Sets the value of the packageFacades property.
	 * 
	 */
	public void setPackageFacades(boolean value) {
		this.packageFacades = value;
	}

	/**
	 * Gets the value of the hardBuildings property.
	 * 
	 */
	public boolean isHardBuildings() {
		return hardBuildings;
	}

	/**
	 * Sets the value of the hardBuildings property.
	 * 
	 */
	public void setHardBuildings(boolean value) {
		this.hardBuildings = value;
	}

	/**
	 * Gets the value of the lightObject property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLightObject() {
		return lightObject;
	}

	/**
	 * Sets the value of the lightObject property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLightObject(String value) {
		this.lightObject = value;
	}

	/**
	 * Gets the value of the facadeLod property.
	 * 
	 */
	public int getFacadeLod() {
		return facadeLod;
	}

	/**
	 * Sets the value of the facadeLod property.
	 * 
	 */
	public void setFacadeLod(int value) {
		this.facadeLod = value;
	}

	/**
	 * Gets the value of the facadeSet property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
//	public String getFacadeSet() {
//		return facadeSet;
//	}

	/**
	 * Sets the value of the facadeSet property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
//	public void setFacadeSet(String value) {
//		this.facadeSet = value;
//	}

	/**
	 * Gets the value of the generateXmlStats property.
	 * 
	 */
	public boolean isGenerateXmlStats() {
		return generateXmlStats;
	}

	/**
	 * Sets the value of the generateXmlStats property.
	 * 
	 */
	public void setGenerateXmlStats(boolean value) {
		this.generateXmlStats = value;
	}

	/**
	 * Gets the value of the generatePdfStats property.
	 * 
	 */
	public boolean isGeneratePdfStats() {
		return generatePdfStats;
	}

	/**
	 * Sets the value of the generatePdfStats property.
	 * 
	 */
	public void setGeneratePdfStats(boolean value) {
		this.generatePdfStats = value;
	}

	/**
	 * Gets the value of the buildingsExclusions property.
	 * 
	 * @return possible object is {@link BuildingsExclusionsList }
	 * 
	 */
	public BuildingsExclusionsList getBuildingsExclusions() {
		return buildingsExclusions;
	}

	/**
	 * Sets the value of the buildingsExclusions property.
	 * 
	 * @param value
	 *            allowed object is {@link BuildingsExclusionsList }
	 * 
	 */
	public void setBuildingsExclusions(BuildingsExclusionsList value) {
		this.buildingsExclusions = value;
	}

	/**
	 * Gets the value of the forestsRules property.
	 * 
	 * @return possible object is {@link ForestsRulesList }
	 * 
	 */
	public ForestsRulesList getForestsRules() {
		return forestsRules;
	}

	/**
	 * Sets the value of the forestsRules property.
	 * 
	 * @param value
	 *            allowed object is {@link ForestsRulesList }
	 * 
	 */
	public void setForestsRules(ForestsRulesList value) {
		this.forestsRules = value;
	}

	/**
	 * Gets the value of the objectsRules property.
	 * 
	 * @return possible object is {@link ObjectsRulesList }
	 * 
	 */
	public XplaneObjectsRulesList getObjectsRules() {
		return objectsRules;
	}

	/**
	 * Sets the value of the objectsRules property.
	 * 
	 * @param value
	 *            allowed object is {@link ObjectsRulesList }
	 * 
	 */
	public void setObjectsRules(XplaneObjectsRulesList value) {
		this.objectsRules = value;
	}

	/**
	 * Gets the value of the facadesRules property.
	 * 
	 * @return possible object is {@link FacadesRulesList }
	 * 
	 */
	public FacadesRulesList getFacadesRules() {
		return facadesRules;
	}

	/**
	 * Sets the value of the facadesRules property.
	 * 
	 * @param value
	 *            allowed object is {@link FacadesRulesList }
	 * 
	 */
	public void setFacadesRules(FacadesRulesList value) {
		this.facadesRules = value;
	}

	/**
	 * Gets the value of the streetLightObjects property.
	 * 
	 * @return possible object is {@link ObjectsList }
	 * 
	 */
	public ObjectsList getStreetLightObjects() {
		return streetLightObjects;
	}

	/**
	 * Sets the value of the streetLightObjects property.
	 * 
	 * @param value
	 *            allowed object is {@link ObjectsList }
	 * 
	 */
	public void setStreetLightObjects(ObjectsList value) {
		this.streetLightObjects = value;
	}

	public boolean isSmartExclusions() {
		return smartExclusions;
	}

	public void setSmartExclusions(boolean smartExclusions) {
		this.smartExclusions = smartExclusions;
	}

	public int getSmartExclusionSize() {
		return smartExclusionSize;
	}

	public void setSmartExclusionSize(int smartExclusionSize) {
		this.smartExclusionSize = smartExclusionSize;
	}

	public int getSmartExclusionDistance() {
		return smartExclusionDistance;
	}

	public void setSmartExclusionDistance(int smartExclusionDistance) {
		this.smartExclusionDistance = smartExclusionDistance;
	}

	public LightsRulesList getLightsRules() {
		return lightsRules;
	}

	public void setLightsRules(LightsRulesList lightsRules) {
		this.lightsRules = lightsRules;
	}

	public boolean isGenerateLights() {
		return true;
	}

	public boolean isGeneratePowerlines() {
		return generatePowerlines;
	}

	public void setGeneratePowerlines(boolean generatePowerlines) {
		this.generatePowerlines = generatePowerlines;
	}

	public boolean isGenerateRailways() {
		return generateRailways;
	}

	public void setGenerateRailways(boolean generateRailways) {
		this.generateRailways = generateRailways;
	}

	public boolean isGenerateRoads() {
		return generateRoads;
	}

	public void setGenerateRoads(boolean generateRoads) {
		this.generateRoads = generateRoads;
	}

	public boolean isGenerateFence() {
		return generateFence;
	}

	public void setGenerateFence(boolean generateFence) {
		this.generateFence = generateFence;
	}

	public boolean isGenerateDebugImg() {
		return generateDebugImg;
	}

	public void setGenerateDebugImg(boolean generateDebugImg) {
		this.generateDebugImg = generateDebugImg;
	}

	public boolean isGenerateTanks() {
		return generateTanks;
	}

	public void setGenerateTanks(boolean generateTanks) {
		this.generateTanks = generateTanks;
	}

	public boolean isGenerateChimneys() {
		return generateChimneys;
	}

	public void setGenerateChimneys(boolean generateChimneys) {
		this.generateChimneys = generateChimneys;
	}
	
	public boolean isGenerateBridges() {
		return generateBridges;
	}
	
	public void setGenerateBridges(boolean generateBridges) {
		this.generateBridges= generateBridges;
	}

}
