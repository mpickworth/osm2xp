package com.osm2xp.parsers;

import com.osm2xp.exceptions.OsmParsingException;

public interface IBasicParser {
	
	public void process() throws OsmParsingException;

	public void complete();

}
