package com.osm2xp.model.facades;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "barrierType")
@XmlEnum
public enum BarrierType {
	
	@XmlEnumValue("wall")
	WALL,
	@XmlEnumValue("fence")
	FENCE;

}
