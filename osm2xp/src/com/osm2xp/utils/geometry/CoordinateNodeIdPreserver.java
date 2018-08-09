package com.osm2xp.utils.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class CoordinateNodeIdPreserver {

	public static List<Geometry> preserveNodeIds(List<Geometry> original, List<Geometry> fixed) {
		if (needPreserveCoordinates(original)) {
			Map<Coordinate, Long> coordsMap = original.stream().flatMap(geometry -> Arrays.asList(geometry.getCoordinates()).stream())
					.filter(coord -> coord instanceof NodeCoordinate).distinct()
					.collect(Collectors.toMap(p -> p, p -> ((NodeCoordinate) p).getNodeId()));
			
			return fixed.stream().map(geom -> geom instanceof LineString?preserveCoordinates(coordsMap, (LineString) geom) : geom).collect(Collectors.toList());
		}
		return fixed;
	}

	protected static boolean needPreserveCoordinates(List<Geometry> geoms) {
		return geoms.stream().anyMatch(geom -> geom instanceof LineString);
	}

	protected static LineString preserveCoordinates(Map<Coordinate, Long> nodeIdsMap, LineString fixed) {
		List<Coordinate> newCoords = new ArrayList<>();
		Coordinate[] fixedCoords = fixed.getCoordinates();
		for (Coordinate coordinate : fixedCoords) {
			Long id = nodeIdsMap.get(coordinate);
			if (id != null) {
				newCoords.add(new NodeCoordinate(coordinate.x, coordinate.y, id));
			} else {
				newCoords.add(coordinate);
			}
		}
		return fixed.getFactory().createLineString(newCoords.toArray(new Coordinate[0]));

	}

}
