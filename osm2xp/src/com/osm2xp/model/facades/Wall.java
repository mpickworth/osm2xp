package com.osm2xp.model.facades;

import java.util.ArrayList;
import java.util.List;

public class Wall {
	
	private double minLength;
	private double maxLength;
	private double minHdg;
	private double maxHdg;
	
	private double xScale;
	private double yScale;
	
	private double roofSlope = 0;
	private boolean slant = false;
	
	private List<Double> xCoords = new ArrayList<>();
	private List<Double> yCoords = new ArrayList<>();
	
	public Wall(double minLength, double maxLength) {
		super();
		this.minLength = minLength;
		this.maxLength = maxLength;
	}
	
	public double getMinLength() {
		return minLength;
	}
	public void setMinLength(double minLength) {
		this.minLength = minLength;
	}
	public double getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(double maxLength) {
		this.maxLength = maxLength;
	}
	public List<Double> getxCoords() {
		return xCoords;
	}
	public void setxCoords(List<Double> xCoords) {
		this.xCoords = xCoords;
	}
	public List<Double> getyCoords() {
		return yCoords;
	}
	public void setyCoords(List<Double> yCoords) {
		this.yCoords = yCoords;
	}

	public double getxScale() {
		return xScale;
	}

	public void setxScale(double xScale) {
		this.xScale = xScale;
	}

	public double getyScale() {
		return yScale;
	}

	public void setyScale(double yScale) {
		this.yScale = yScale;
	}

	public double getRoofSlope() {
		return roofSlope;
	}

	public void setRoofSlope(double roofSlope) {
		this.roofSlope = roofSlope;
	}

	public boolean isSlant() {
		return slant;
	}

	public void setSlant(boolean slant) {
		this.slant = slant;
	}

	public double getMinHdg() {
		return minHdg;
	}

	public void setMinHdg(double minHdg) {
		this.minHdg = minHdg;
	}

	public double getMaxHdg() {
		return maxHdg;
	}

	public void setMaxHdg(double maxHdg) {
		this.maxHdg = maxHdg;
	}
	
}
