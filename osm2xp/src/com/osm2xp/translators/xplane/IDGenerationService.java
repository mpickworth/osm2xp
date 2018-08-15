package com.osm2xp.translators.xplane;

/**
 * Id generator service to crete nodes while OSM polygons cutting, fixing etc.
 * Generates negative node ids, since it's OSM policy for "temporary" nodes
 * <b>IMPORTANT</b> this class is for single thread use only for now. Otherwise results can be unpredictable.
 * @author 32kda
 */
public class IDGenerationService {
	
	private static int renumberCounter = -1; //We use negative values for "fake" nodes here because of https://wiki.openstreetmap.org/wiki/Node, "Editors may temporarily save node ids as negative to denote ids that haven't yet been saved to the server."
	
	/**
	 * Reinit counter and make it -1
	 */
	public static void reinit() {
		renumberCounter = -1;
	}	
	
	/**
	 * Return new id, actually not increment, but decrement starting at -1, see reason above 
	 * @return
	 */
	public static int getIncrementId() {
		int newId = renumberCounter;
		renumberCounter--;
		return newId;
	}
	
}
