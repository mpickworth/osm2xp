package com.osm2xp.model.facades;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Facade.
 * 
 * @author Benjamin Blanchet
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Facade", propOrder = { "file", "roofColor", "wallColor", "specialType", "barrierType", "industrial", "commercial",
		"residential", "sloped", "minVectorLength", "maxVectorLength", "simpleBuildingOnly", "minHeight", "maxHeight" })
public class Facade {

	@XmlElement(required = true)
	protected String file;
	@XmlElement(required = true)
	protected String roofColor;
	@XmlElement(required = true)
	protected String wallColor;
	@XmlElement(name = "barrier")
	protected BarrierType barrierType;
	@XmlElement(name = "specialType")
	protected SpecialFacadeType specialType;
	protected boolean industrial;
	protected boolean commercial;
	protected boolean residential;
	protected boolean sloped;
	protected double minVectorLength;
	protected double maxVectorLength;
	protected boolean simpleBuildingOnly;
	protected int minHeight;
	protected int maxHeight;

	/**
	 * Gets the value of the file property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Sets the value of the file property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setFile(String value) {
		this.file = value;
	}

	/**
	 * Gets the value of the roofColor property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getRoofColor() {
		return roofColor;
	}

	/**
	 * Sets the value of the roofColor property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setRoofColor(String value) {
		this.roofColor = value;
	}

	/**
	 * Gets the value of the wallColor property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getWallColor() {
		return wallColor;
	}

	/**
	 * Sets the value of the wallColor property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setWallColor(String value) {
		this.wallColor = value;
	}

	/**
	 * Gets the value of the industrial property.
	 * 
	 */
	public boolean isIndustrial() {
		return industrial;
	}

	/**
	 * Sets the value of the industrial property.
	 * 
	 */
	public void setIndustrial(boolean value) {
		this.industrial = value;
	}

	/**
	 * Gets the value of the commercial property.
	 * 
	 */
	public boolean isCommercial() {
		return commercial;
	}

	/**
	 * Sets the value of the commercial property.
	 * 
	 */
	public void setCommercial(boolean value) {
		this.commercial = value;
	}

	/**
	 * Gets the value of the residential property.
	 * 
	 */
	public boolean isResidential() {
		return residential;
	}

	/**
	 * Sets the value of the residential property.
	 * 
	 */
	public void setResidential(boolean value) {
		this.residential = value;
	}

	/**
	 * Gets the value of the sloped property.
	 * 
	 */
	public boolean isSloped() {
		return sloped;
	}

	/**
	 * Sets the value of the sloped property.
	 * 
	 */
	public void setSloped(boolean value) {
		this.sloped = value;
	}

	/**
	 * Gets the value of the minVectorLength property.
	 * 
	 */
	public double getMinVectorLength() {
		return minVectorLength;
	}

	/**
	 * Sets the value of the minVectorLength property.
	 * 
	 */
	public void setMinVectorLength(double value) {
		this.minVectorLength = value;
	}

	/**
	 * Gets the value of the maxVectorLength property.
	 * 
	 */
	public double getMaxVectorLength() {
		return maxVectorLength;
	}

	/**
	 * Sets the value of the maxVectorLength property.
	 * 
	 */
	public void setMaxVectorLength(double value) {
		this.maxVectorLength = value;
	}

	/**
	 * Gets the value of the simpleBuildingOnly property.
	 * 
	 */
	public boolean isSimpleBuildingOnly() {
		return simpleBuildingOnly;
	}

	/**
	 * Sets the value of the simpleBuildingOnly property.
	 * 
	 */
	public void setSimpleBuildingOnly(boolean value) {
		this.simpleBuildingOnly = value;
	}

	/**
	 * Gets the value of the minHeight property.
	 * 
	 */
	public int getMinHeight() {
		return minHeight;
	}

	/**
	 * Sets the value of the minHeight property.
	 * 
	 */
	public void setMinHeight(int value) {
		this.minHeight = value;
	}

	/**
	 * Gets the value of the maxHeight property.
	 * 
	 */
	public int getMaxHeight() {
		return maxHeight;
	}

	/**
	 * Sets the value of the maxHeight property.
	 * 
	 */
	public void setMaxHeight(int value) {
		this.maxHeight = value;
	}

	public BarrierType getBarrierType() {
		return barrierType;
	}

	public void setBarrierType(BarrierType barrierType) {
		this.barrierType = barrierType;
	}

	public SpecialFacadeType getSpecialType() {
		if (specialType == null) {
			switch (barrierType) {
			case FENCE:
				return SpecialFacadeType.FENCE;
			case WALL:
				return SpecialFacadeType.WALL;
			}
		}
		return specialType;
	}

	public void setSpecialType(SpecialFacadeType specialType) {
		this.specialType = specialType;
	}

}
