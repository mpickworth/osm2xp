package com.osm2xp.model.facades;

import javax.xml.bind.annotation.XmlEnumValue;

public enum SpecialFacadeType {

	@XmlEnumValue("tank")
	TANK,
	@XmlEnumValue("garage")
	GARAGE,
	@XmlEnumValue("wall")
	WALL,
	@XmlEnumValue("fence")
	FENCE;
	
}
