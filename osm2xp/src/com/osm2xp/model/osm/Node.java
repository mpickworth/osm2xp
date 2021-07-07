package com.osm2xp.model.osm;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Node.
 * 
 * @author Benjamin Blanchet
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "tag" })
@XmlRootElement(name = "node")
public class Node {

	protected List<Tag> tag;
	@XmlAttribute(name = "lat", required = true)
	protected double lat;
	@XmlAttribute(name = "lon", required = true)
	protected double lon;
	@XmlAttribute(name = "id", required = true)
	protected long id;

	/**
	 * Default no-arg constructor
	 * 
	 */
	public Node() {
		super();
	}

	/**
	 * Fully-initialising value constructor
	 * 
	 */
	public Node(final List<Tag> tag, final double lat, final double lon,
			final long id) {
		this.tag = tag;
		this.lat = lat;
		this.lon = lon;
		this.id = id;
	}

	/**
	 * Gets the value of the tag property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the tag property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getTag().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Tag }
	 * 
	 * 
	 */
	public List<Tag> getTag() {
		if (tag == null) {
			tag = new ArrayList<Tag>();
		}
		return this.tag;
	}

	/**
	 * Gets the value of the lat property.
	 * 
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * Sets the value of the lat property.
	 * 
	 */
	public void setLat(double value) {
		this.lat = value;
	}

	/**
	 * Gets the value of the lon property.
	 * 
	 */
	public double getLon() {
		return lon;
	}

	/**
	 * Sets the value of the lon property.
	 * 
	 */
	public void setLon(double value) {
		this.lon = value;
	}

	/**
	 * Gets the value of the id property.
	 * 
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the value of the id property.
	 * 
	 */
	public void setId(long value) {
		this.id = value;
	}

	@Override
	public String toString() {
		return "Node " + id + "[lat=" + lat + ", lon=" + lon + "]";
	}

}
