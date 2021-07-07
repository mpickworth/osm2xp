package com.osm2xp.model.facades;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.osm2xp.gui.Activator;

public class FacadeDefinitionParser {
	
	private static final String[] WALL_TAGS = {"WALL_RULE","SCALE","ROOF_SLOPE",
			"BOTTOM",
			"MIDDLE",
			"TOP",			        
			"LEFT",
			"CENTER",
			"RIGHT"};
	
	public static FacadeDefinition parse(File facadeFile) {
		FacadeDefinition definition = new FacadeDefinition();
		Multimap<String, String> props = HashMultimap.create();
		try {
			Wall currentWall = null;
			List<String> lines = Files.readLines(facadeFile, Charset.forName("utf-8"));
			for (String string : lines) {
				string = string.trim();
				int idx = string.indexOf(' ');
				if (idx > 0) {
					String key = string.substring(0, idx).trim();
					if (key.startsWith("WALL")) {
						if (currentWall != null) {
							definition.addWall(currentWall);
							currentWall = null;
						}
						currentWall = createWallDef(string);
					} else if (isWallTag(key) && currentWall != null) {
						parseWallTag(currentWall, string);
					} else if (!isWallTag(string)) {						
						String value = string.substring(idx, string.length()).trim();
						if (!key.isEmpty() && !value.isEmpty()) {
							props.put(key, value);
						}
					}
				}
			}
			if (currentWall != null) {
				definition.addWall(currentWall);
			}
			definition.setProperties(props);
		} catch (Exception e) {
			Activator.log(e);
			return null;
		}
		return definition;
	}

	private static void parseWallTag(Wall currentWall, String string) {
		String[] parts = string.trim().replaceAll("\\s+"," ").split(" ");
		if ("SCALE".equals(parts[0])) {
			currentWall.setxScale(Double.parseDouble(parts[1]));
			currentWall.setyScale(Double.parseDouble(parts[2]));
		} else if ("ROOF_SLOPE".equals(parts[0])) {
			currentWall.setRoofSlope(Double.parseDouble(parts[1]));
			if (parts.length > 2) {
				currentWall.setSlant("SLANT".equals(parts[2]));	
			}
		} else if ("BOTTOM".equals(parts[0]) || "MIDDLE".equals(parts[0]) || "TOP".equals(parts[0])) {
			if (currentWall.getyCoords().isEmpty()) {
				currentWall.getyCoords().add(Double.parseDouble(parts[1].trim()));
			}
			currentWall.getyCoords().add(Double.parseDouble(parts[2].trim()));
		} else if ("LEFT".equals(parts[0]) || "CENTER".equals(parts[0]) || "RIGHT".equals(parts[0])) {
			if (currentWall.getxCoords().isEmpty()) {
				currentWall.getxCoords().add(Double.parseDouble(parts[1].trim()));
			}
			currentWall.getxCoords().add(Double.parseDouble(parts[2].trim()));
		}
	}

	private static boolean isWallTag(String key) {
		return ArrayUtils.contains(WALL_TAGS, key);
	}

	private static Wall createWallDef(String string) {
		String[] props = string.trim().split(" ");
		if (props.length > 2) {
			Wall result = new Wall(Double.parseDouble(props[1].trim()), Double.parseDouble(props[2].trim()));
			if (props.length > 4) {
				result.setMinHdg(Double.parseDouble(props[3].trim()));
				result.setMaxHdg(Double.parseDouble(props[4].trim()));
			}
			return result;
		}
				
		return null;
	}

}
