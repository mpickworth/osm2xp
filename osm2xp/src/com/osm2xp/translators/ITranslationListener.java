package com.osm2xp.translators;

import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.model.osm.OsmPolyline;

public interface ITranslationListener {
	public void processBuilding(OsmPolygon polygon, Integer facade);
	public void process3dObject(OsmPolygon polygon);
	public void processStreetLights(OsmPolygon polygon);
	public void processForest(OsmPolygon polygon);
	public void polyProcessed(OsmPolyline poly, IPolyHandler handler);
	public void complete();
}
