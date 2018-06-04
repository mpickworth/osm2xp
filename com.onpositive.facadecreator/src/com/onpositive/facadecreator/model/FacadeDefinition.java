package com.onpositive.facadecreator.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class FacadeDefinition {
	
	private Multimap<String, String> properties = HashMultimap.create();
	
	private List<Wall> walls = new ArrayList<Wall>();
	
	public void addWall(Wall currentWall) {
		walls.add(currentWall);
	}

	public List<Wall> getWalls() {
		return walls;
	}

	public void setWalls(List<Wall> walls) {
		this.walls = walls;
	}

	public Multimap<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Multimap<String, String> properties) {
		this.properties = properties;
	}

}
