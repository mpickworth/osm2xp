package com.osm2xp.model.options;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * GuiOptions.
 * 
 * @author Benjamin Blanchet
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "databaseMode", "appendHour", "appendTile",
		"simplifyShapes", "currentFilePath", "outputFormat", "singlePass",
		"lastFiles" })
@XmlRootElement(name = "GuiOptions")
public class GuiOptions {

	protected boolean databaseMode;
	protected boolean appendHour;
	protected boolean appendTile;
	protected boolean simplifyShapes;
	@XmlElement(required = true)
	protected String currentFilePath;
	@XmlElement(required = true)
	protected String outputFormat;
	protected boolean singlePass;
	@XmlElement(required = true)
	protected List<String> lastFiles;

	/**
	 * Default no-arg constructor
	 * 
	 */
	public GuiOptions() {
		super();
	}

	/**
	 * Fully-initialising value constructor
	 * 
	 */
	public GuiOptions(final boolean databaseMode, final boolean appendHour,
			final boolean appendTile, final boolean simplifyShapes,
			final String currentFilePath, final String outputFormat,
			final boolean singlePass, final List<String> lastFiles) {
		this.databaseMode = databaseMode;
		this.appendHour = appendHour;
		this.appendTile = appendTile;
		this.simplifyShapes = simplifyShapes;
		this.currentFilePath = currentFilePath;
		this.outputFormat = outputFormat;
		this.singlePass = singlePass;
		this.lastFiles = lastFiles;
	}

	/**
	 * Gets the value of the databaseMode property.
	 * 
	 */
	public boolean isDatabaseMode() {
		return databaseMode;
	}

	/**
	 * Sets the value of the databaseMode property.
	 * 
	 */
	public void setDatabaseMode(boolean value) {
		this.databaseMode = value;
	}

	/**
	 * Gets the value of the appendHour property.
	 * 
	 */
	public boolean isAppendHour() {
		return appendHour;
	}

	/**
	 * Sets the value of the appendHour property.
	 * 
	 */
	public void setAppendHour(boolean value) {
		this.appendHour = value;
	}

	/**
	 * Gets the value of the appendTile property.
	 * 
	 */
	public boolean isAppendTile() {
		return appendTile;
	}

	/**
	 * Sets the value of the appendTile property.
	 * 
	 */
	public void setAppendTile(boolean value) {
		this.appendTile = value;
	}

	/**
	 * Gets the value of the simplifyShapes property.
	 * 
	 */
	public boolean isSimplifyShapes() {
		return simplifyShapes;
	}

	/**
	 * Sets the value of the simplifyShapes property.
	 * 
	 */
	public void setSimplifyShapes(boolean value) {
		this.simplifyShapes = value;
	}

	/**
	 * Gets the value of the currentFilePath property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCurrentFilePath() {
		return currentFilePath;
	}

	/**
	 * Sets the value of the currentFilePath property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCurrentFilePath(String value) {
		this.currentFilePath = value;
	}

	/**
	 * Gets the value of the outputFormat property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getOutputFormat() {
		return outputFormat;
	}

	/**
	 * Sets the value of the outputFormat property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setOutputFormat(String value) {
		this.outputFormat = value;
	}

	/**
	 * Gets the value of the singlePass property.
	 * 
	 */
	public boolean isSinglePass() {
		return singlePass;
	}

	/**
	 * Sets the value of the singlePass property.
	 * 
	 */
	public void setSinglePass(boolean value) {
		this.singlePass = value;
	}

	/**
	 * Gets the value of the lastFiles property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the lastFiles property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getLastFiles().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link String }
	 * 
	 * 
	 */
	public List<String> getLastFiles() {
		if (lastFiles == null) {
			lastFiles = new ArrayList<String>();
		}
		return this.lastFiles;
	}

}
