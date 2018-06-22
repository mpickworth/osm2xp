package com.osm2xp.translators.xplane;

import java.util.HashMap;
import java.util.Map;

/**
 * Id renumberer to match new int ids starting from 1 with OSM node long ids
 * <b>IMPORTANT</b> this class is for single thread use only for now. Otherwise results can be unpredictable.
 * @author 32kda
 */
public class IDRenumbererService {
	
	private static Map<Long, Integer> crossingRenumberMap = new HashMap<Long, Integer>();
	private static int renumberCounter = 1; 
	
	public static void reinit() {
		crossingRenumberMap.clear();
		renumberCounter = 1;
	}
	
	public static int getNewId(long id) {
		Integer newId = crossingRenumberMap.get(id);
		if (newId == null) {
			newId = renumberCounter;
			renumberCounter++;
			crossingRenumberMap.put(id,newId);
		}
		return newId;
	}
	
	public static int getIncrementId() {
		renumberCounter++;
		return renumberCounter;
	}
	
}
