package com.osm2xp.model.fsx;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import math.geom3d.Vector3D;

import com.osm2xp.constants.FsxConstants;
import com.osm2xp.model.geom.Lod13Location;
import com.osm2xp.model.osm.Node;
import com.osm2xp.utils.geometry.GeomUtils;

/**
 * FsxObject.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class FsxObject {

	private String guid;
	private String type;
	private ArrayList<Lod13Location> lod13Locations;

	/**
	 * @param guid
	 * @param type
	 * @param nodes
	 */
	public FsxObject(String guid, String type, List<Node> nodes) {
		this.guid = guid;
		this.type = type;
		lod13Locations = new ArrayList<Lod13Location>();
		for (Node node : nodes) {
			Lod13Location location = GeomUtils.getLod13Location(node.getLat(),
					node.getLon());
			lod13Locations.add(location);
		}
	}

	/**
	 * @return
	 */
	public boolean isOnASingleLod13Square() {
		if (!lod13Locations.isEmpty()) {
			Lod13Location base = lod13Locations.get(0);
			for (Lod13Location location : lod13Locations) {
				if (!location.getLod13String().equalsIgnoreCase(
						base.getLod13String())) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		if (this.type.equals(FsxConstants.AGN_TYPE_GENERICBUILDING)) {
			return getSquareBuildingText();
		}

		if (this.type.equals(FsxConstants.AGN_TYPE_FOREST)) {
			return getForestText();
		}

		if (this.type.equals(FsxConstants.AGN_TYPE_POLYBUILDING)) {
			return getPolylineBuildingText();
		}
		return null;
	}

	/**
	 * @return
	 */
	public String getPolylineBuildingText() {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(this.type + " ");
		stringBuilder.append("{" + this.guid + "} ");
		stringBuilder.append("-0.1 ");
		for (Lod13Location location : lod13Locations) {
			NumberFormat formatter = new DecimalFormat("#.################");
			stringBuilder.append((formatter.format(location.getxOffset()) + " "
					+ formatter.format(location.getyOffset()) + " ")
					.replaceAll(",", "."));
		}
		stringBuilder.append("\n");
		return stringBuilder.toString();
	}

	/**
	 * @return
	 */
	public String getForestText() {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(this.type + " ");
		stringBuilder.append("{" + this.guid + "} ");

		for (Lod13Location location : lod13Locations) {
			new DecimalFormat("#.################");
			NumberFormat formatter = NumberFormat.getInstance(Locale
					.getDefault());
			String xOffset = formatter.format((Double) location.getxOffset());
			String yOffset = formatter.format((Double) location.getyOffset());
			stringBuilder.append(xOffset + " " + yOffset + " ");

		}
		stringBuilder.append("\n");
		return stringBuilder.toString();
	}

	/**
	 * @return
	 */
	public String getSquareBuildingText() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(this.type + " ");
		stringBuilder.append("{" + this.guid + "} ");
		NumberFormat formatter = new DecimalFormat("#.################");
		Lod13Location location = lod13Locations.get(0);
		// first point
		stringBuilder.append((formatter.format(location.getxOffset()) + " "
				+ formatter.format(location.getyOffset()) + " ").replaceAll(
				",", "."));
		// direction to second point
		stringBuilder.append(formatter.format(location.getxOffset()
				- lod13Locations.get(1).getxOffset())
				+ " "
				+ formatter.format(location.getyOffset()
						- lod13Locations.get(1).getyOffset()) + " ");
		// third point
		stringBuilder.append((formatter.format(lod13Locations.get(2)
				.getxOffset())
				+ " "
				+ formatter.format(lod13Locations.get(2).getyOffset()) + " ")
				.replaceAll(",", "."));
		stringBuilder.append("\n");
		return stringBuilder.toString().replaceAll(",", ".");
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public ArrayList<Lod13Location> getLod13Locations() {
		return lod13Locations;
	}

	public void setLod13Locations(ArrayList<Lod13Location> lod13Locations) {
		this.lod13Locations = lod13Locations;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public class AGNGenericBuilding {
		public Vector3D direction;

		public Vector3D offset;

		public Vector3D position;

		public AGNGenericBuilding(ArrayList<Lod13Location> lod13Locations) {

			this.position = new Vector3D(lod13Locations.get(1).getxOffset(),
					lod13Locations.get(1).getyOffset(), 0.0);
			this.direction = new Vector3D(lod13Locations.get(2).getxOffset(),
					lod13Locations.get(2).getyOffset(), 0.0);
			this.offset = new Vector3D(lod13Locations.get(3).getxOffset(),
					lod13Locations.get(3).getyOffset(), 0.0);
			// return new AGNGenericBuilding { GUID = new Guid(elem[1]),
			// Position = new Vector3d(Convert.ToDouble(elem[2]),
			// Convert.ToDouble(elem[3]), 0.0), Direction = new
			// Vector3d(Convert.ToDouble(elem[4]), Convert.ToDouble(elem[5]),
			// 0.0), Offset = new Vector3d(Convert.ToDouble(elem[6]),
			// Convert.ToDouble(elem[7]), 0.0) };
		}

		@Override
		public String toString() {
			return null;
			//
			// object[] arg = new object[] { base.GUID, this.Position.x,
			// this.Position.y, this.Direction.x, this.Direction.y,
			// this.Offset.x, this.Offset.y };
			// writer.Write("AGNGenericBuilding {{{0}}} {1} {2} {3} {4} {5} {6}",
			// arg);
			// return writer.ToString();
		}
	}
}
// Properties

