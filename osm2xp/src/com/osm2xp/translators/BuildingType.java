package com.osm2xp.translators;

public enum BuildingType {
	INDUSTRIAL("industrial"),
	COMMERCIAL("commercial"),
	RESIDENTIAL("residential");
	
	private String id;
	
	BuildingType(String id) {
		this.id = id;
	}
	
	public static BuildingType fromId(String id) {
		for (BuildingType type : values()) {
			if (type.id.equals(id)) {
				return type;
			}
		}
		return null;
	}
}
