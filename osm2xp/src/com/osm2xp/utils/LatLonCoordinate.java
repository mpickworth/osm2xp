package com.osm2xp.utils;

import com.vividsolutions.jts.geom.Coordinate;

public class LatLonCoordinate extends Coordinate {
	
	private static final int    EARTH_RADIUS    = 6371 * 1000; //meters
	private static final double RAD_COEF = Math.PI / 180;

	private static final long serialVersionUID = -3019536026094811552L;
	
	public LatLonCoordinate(double x, double y, double z) {
		super(x,y,z);
	}

	@Override
	public double distance(Coordinate p) {
		if (p instanceof LatLonCoordinate) {
		    double latitude1 = y * RAD_COEF;
		    double longitude1 = x * RAD_COEF;

		    double lat_sin = Math.sin(latitude1);
			double x1 = EARTH_RADIUS * lat_sin * Math.cos(longitude1);
		    double y1 = EARTH_RADIUS * lat_sin * Math.sin(longitude1);

		    double latitude2 = p.y * RAD_COEF;
		    double longitude2 = p.x * RAD_COEF;

		    double lat_sin2 = Math.sin(latitude2);
			double x2 = EARTH_RADIUS * lat_sin2 * Math.cos(longitude2);
		    double y2 = EARTH_RADIUS * lat_sin2 * Math.sin(longitude2);
		    double dx = x1 - x2;
			double dy = y1 - y2;
			return Math.sqrt(dx * dx + dy * dy);
		}
		return super.distance(p);
	}

}
