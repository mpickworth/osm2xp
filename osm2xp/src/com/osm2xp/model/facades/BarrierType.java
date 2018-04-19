package com.osm2xp.model.facades;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "barrierType")
@XmlEnum
public enum BarrierType {
	
	@XmlEnumValue("wall")
	WALL("wall"),
	@XmlEnumValue("fence")
	FENCE("fence");
	
	private final String value;
	
	BarrierType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BarrierType fromValue(String v) {
        for (BarrierType c: BarrierType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
