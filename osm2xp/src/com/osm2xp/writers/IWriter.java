package com.osm2xp.writers;

import math.geom2d.Point2D;

/**
 * Writer interface.
 * 
 * @author Benjamin Blanchet
 * 
 */
public interface IWriter {

	public void init(Point2D coordinates);

	public void write(Object data);

	public void complete(Object data);
}
