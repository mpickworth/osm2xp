package com.osm2xp.translators;

import com.osm2xp.model.osm.OsmPolygon;

public interface IPolyHandler {
	public boolean handlePoly(OsmPolygon osmPolygon);
	
	public void translationComplete();
}
