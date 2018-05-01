package com.osm2xp.utils;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.osm2xp.constants.Osm2xpConstants;
import com.osm2xp.gui.Activator;
import com.osm2xp.model.options.ForestTagRule;
import com.osm2xp.model.options.TagsRule;
import com.osm2xp.model.options.XplaneObjectTagRule;
import com.osm2xp.model.osm.Node;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;

/**
 * OsmUtils.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class OsmUtils {

	/**
	 * @param value
	 * @param tags
	 * @return
	 */
	public static boolean isStringInTags(String value, List<Tag> tags) {
		for (Tag tag : tags) {
			if (tag.getKey().toLowerCase().contains(value)
					|| tag.getValue().toLowerCase().contains(value)) {
				return true;

			}
		}
		return false;
	}

	/**
	 * extract roof color information from a list of tags
	 * 
	 * @param tagsList
	 * @return
	 */
	public static Color getRoofColorFromTags(List<Tag> tagsList) {
		Color result = null;
		for (Tag tag : tagsList) {
			if (tag.getKey().contains("roof:color")) {
				try {
					result = Color.decode(tag.getValue());
				} catch (Exception e) {
					// Osm2xpLogger
					// .warning("Failed to extract color information for value "
					// + tag.getValue());
				}
			}
		}
		return result;
	}

	/**
	 * return the first tag of first list that is also in list2
	 * 
	 * @param tagsList1
	 * @param tagsList2
	 * @return
	 */
	public static Tag getMatchingTag(List<Tag> tagsList1, List<Tag> tagsList2) {

		for (Tag tag : tagsList1) {
			if (isTagInTagsList(tag.getKey(), tag.getValue(), tagsList2)) {
				return tag;
			}
		}
		return null;

	}

	/**
	 * return the first tag of first list that is also in list2
	 * 
	 * @param tagsList1
	 * @param tagsList2
	 * @return
	 */
	public static List<Tag> getMatchingTags(List<Tag> tagsList,
			OsmPolygon osmPolygon) {

		List<Tag> result = new ArrayList<Tag>();
		for (Tag userTag : tagsList) {
			for (Tag polygonTag : osmPolygon.getTags()) {
				// first check if the current tag is looking for a osm polygon
				// id
				// check
				if (userTag.getKey().equalsIgnoreCase("id")
						&& userTag.getValue().equalsIgnoreCase(
								String.valueOf(osmPolygon.getId()))) {
					result.add(polygonTag);
				}
				// if tags are matching
				if ((userTag.getKey().equalsIgnoreCase(polygonTag.getKey()) && userTag
						.getValue().equalsIgnoreCase(polygonTag.getValue()))
						|| (userTag.getKey().equalsIgnoreCase("*") && userTag
								.getValue().equalsIgnoreCase(
										polygonTag.getValue()))
						|| (userTag.getKey().equalsIgnoreCase(
								polygonTag.getKey()) && userTag.getValue()
								.equalsIgnoreCase("*"))

				) {
					// if tag is building="yes"
					if (polygonTag.getKey().equalsIgnoreCase("building")
							&& polygonTag.getValue().equalsIgnoreCase("yes")) {
						// only add the tag if there isn't a "wall=no" tag
						if (isBuildingWithWalls(osmPolygon.getTags())) {
							result.add(polygonTag);
						}
					} else
					// if isn't a building=yes tag, add this tag whatever
					{
						result.add(polygonTag);
					}
				}
			}
		}
		if (result.isEmpty()) {
			result = null;
		}
		return result;

	}

	/**
	 * return the first tag of first list that is also in list2
	 * 
	 * @param tagsList1
	 * @param tagsList2
	 * @return
	 */
	public static List<TagsRule> getMatchingRules(
			List<? extends TagsRule> tagsRulesList, OsmPolygon osmPolygon) {

		List<TagsRule> result = new ArrayList<TagsRule>();
		for (TagsRule tagsRules : tagsRulesList) {
			for (Tag polygonTag : osmPolygon.getTags()) {
				// first check if the current tag is looking for a osm polygon
				// id
				// check
				if (tagsRules.getTag().getKey().equalsIgnoreCase("id")
						&& tagsRules
								.getTag()
								.getValue()
								.equalsIgnoreCase(
										String.valueOf(osmPolygon.getId()))) {
					result.add(tagsRules);
					break;
				}
				// if tags are matching
				if (tagsRules.getTag().getKey()
						.equalsIgnoreCase(polygonTag.getKey())
						&& tagsRules.getTag().getValue()
								.equalsIgnoreCase(polygonTag.getValue())) {
					// if tag is building="yes"
					if (tagsRules.getTag().getKey()
							.equalsIgnoreCase("building")
							&& tagsRules.getTag().getValue()
									.equalsIgnoreCase("yes")) {
						// only add the tag if there isn't a "wall=no" tag
						if (isBuildingWithWalls(osmPolygon.getTags())) {
							result.add(tagsRules);
						}
					} else
					// if isn't a building=yes tag, add this tag whatever
					{
						result.add(tagsRules);
					}
				}
			}
		}
		if (result.isEmpty()) {
			result = null;
		}
		return result;

	}

	private static boolean isBuildingWithWalls(List<Tag> tagsList) {
		Boolean result = true;
		for (Tag tag : tagsList) {
			if (tag.getKey().equalsIgnoreCase("wall")
					&& tag.getValue().equalsIgnoreCase("no")) {
				result = false;
				break;
			}
		}
		return result;
	}

	public static boolean isTagInTagsList(String key, String value,
			List<Tag> tags) {
		for (Tag tag : tags) {
			if ((tag.getKey().equalsIgnoreCase(key) && tag.getValue()
					.equalsIgnoreCase(value))
					|| (value.equalsIgnoreCase("*") && tag.getValue()
							.equalsIgnoreCase(value))
					|| (tag.getKey().equalsIgnoreCase(key) && value
							.equalsIgnoreCase("*"))) {
				return true;

			}
		}
		return false;
	}

	public static boolean isExcluded(List<Tag> tags, Long id) {
		for (Tag tag : tags) {
			for (Tag userTag : XplaneOptionsHelper.getOptions()
					.getBuildingsExclusions().getExclusions()) {
				if ((userTag.getKey().equalsIgnoreCase("id") && userTag
						.getValue().equalsIgnoreCase(String.valueOf(id)))
						|| (OsmUtils.compareTags(userTag, tag))) {
					return true;
				}
			}
		}
		return false;

	}

	public static boolean isValueinTags(String value, List<Tag> tags) {
		for (Tag tag : tags) {
			if (tag.getValue().equalsIgnoreCase(value)) {
				return true;

			}
		}
		return false;
	}

	public static boolean isObject(List<Tag> tags) {
		for (Tag tag : tags) {
			for (XplaneObjectTagRule objectTagRule : XplaneOptionsHelper
					.getOptions().getObjectsRules().getRules()) {
				if (compareTags(objectTagRule.getTag(), tag)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isForest(List<Tag> tags) {
		for (Tag tag : tags) {
			for (ForestTagRule forestTagsRule : XplaneOptionsHelper
					.getOptions().getForestsRules().getRules()) {
				if (compareTags(forestTagsRule.getTag(), tag)) {
					return true;
				}

			}
		}
		return false;
	}

	public static boolean isOsmForest(List<Tag> tags) {
		Boolean result = false;
		for (Tag tag : tags) {
			if (tag.getKey().toLowerCase().contains("forest")
					|| tag.getValue().toLowerCase().contains("forest")
					|| tag.getKey().toLowerCase().contains("wood")
					|| tag.getValue().toLowerCase().contains("wood"))
				result = true;
			break;

		}

		return result;
	}

	/**
	 * Remove common tags such as source, name, etc...
	 * 
	 * @param tags
	 *            source tags
	 * @return List of tags
	 */
	public static List<Tag> removeCommonTags(List<Tag> tags) {
		List<Tag> result = new ArrayList<Tag>();
		for (Tag tag : tags) {
			if (!tag.getKey().toLowerCase().contains("name")
					&& !tag.getKey().toLowerCase().contains("source")
					&& !tag.getKey().toLowerCase().contains("note")
					&& !tag.getKey().toLowerCase().contains("url")
					&& !tag.getKey().toLowerCase().contains("ref")
					&& !tag.getKey().toLowerCase().contains("created")) {
				result.add(tag);
			}
		}
		return result;

	}

	public static boolean compareTags(Tag userTag, Tag tag) {
		return userTag.getKey().equalsIgnoreCase(tag.getKey())
				&& userTag.getValue().equalsIgnoreCase(tag.getValue());
	}
	
	public static boolean isRailway(List<Tag> tags) {
		return isTagInTagsList("railway","rail", tags);
	}
	
	public static boolean isRoad(List<Tag> tags) {
		return isStringInTags("highway", tags);
	}
	
	public static boolean isPowerline(List<Tag> tags) {
		return isTagInTagsList("power","line", tags);
	}
	
	public static boolean isFence(List<Tag> tags) {
		return isStringInTags("barrier", tags);
	}

	public static boolean isBuilding(List<Tag> tags) {
		return ((isStringInTags("building", tags) && !isTagInTagsList("wall",
				"no", tags)));
	}

	public static String getNormalizedTagText(Tag tag) {
		if (!tag.getKey().toLowerCase().contains("source")
				&& !tag.getKey().toLowerCase().contains("name")
				&& !tag.getKey().toLowerCase().contains("addr")
				&& !tag.getKey().toLowerCase().contains("description")
				&& !tag.getKey().toLowerCase().contains("fixme")
				&& !tag.getKey().toLowerCase().contains("todo")) {
			String value = tag.getValue().replace("&", "&amp;");
			value = value.replace("\"", "&quot;");
			value = value.replace("\'", "&apos;");
			value = value.replace("<", "&gt;");
			value = value.replace(">", "&apos;");
			String temp = Normalizer.normalize(value, Normalizer.Form.NFD);
			Pattern pattern = Pattern
					.compile("\\p{InCombiningDiacriticalMarks}+");
			value = pattern.matcher(temp).replaceAll("");
			tag.setValue(Normalizer.normalize(tag.getValue(),
					Normalizer.Form.NFKD));
			return "<tag k=\"" + tag.getKey() + "\" v=\"" + value + "\"/>\n";
		}
		return null;

	}

	/**
	 * get height information from osm tags.
	 * 
	 * @param tags
	 * @return the height in meter.
	 */
	public static Integer getHeightFromTags(List<Tag> tags) {
		for (Tag tag : tags) {
			if (!tag.getKey().toLowerCase().contains("max")
					&& !tag.getKey().toLowerCase().contains("min")) {
				if (tag.getKey().toLowerCase().contains("height")
						&& tag.getValue().length() < 11) {
					Integer height = MiscUtils.extractNumbers(tag.getValue());
					if (height != null && height < 800 && height > 4) {
						return height;
					}
				}
				if (tag.getKey().toLowerCase().contains("level")
						&& !tag.getValue().contains("-1")
						&& tag.getValue().length() < 5) {
					Integer levels = MiscUtils.extractNumbers(tag.getValue());
					if (levels != null) {
						int height = (levels * 3) + 1;
						if (height < 800 && height > 4) {
							return height;
						}
					}
				}
			}
		}
		return null;
	}

	public static String CreateTempFile(String folderPath,
			List<OsmPolygon> wayList, String fileName) throws IOException {

		String filePath = folderPath + File.separator + fileName + ".osm";

		try (BufferedWriter output = new BufferedWriter(new FileWriter(filePath, false))){
			output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			output.write("<osm version=\"0.6\" generator=\"osm2xp "
					+ Osm2xpConstants.OSM2XP_VERSION + "\">\n");
	
			// write all nodes
			for (OsmPolygon osmPolygon : wayList) {
				for (Node node : osmPolygon.getNodes()) {
					output.write("<node id=\"" + node.getId() + "\" lat=\""
							+ node.getLat() + "\" lon=\"" + node.getLon()
							+ "\" version=\"1\" />\n");
				}
	
			}
	
			for (OsmPolygon osmPolygon : wayList) {
				output.write("<way id=\"" + osmPolygon.getId()
						+ "\" visible=\"true\" version=\"2\" >\n");
				for (Node node : osmPolygon.getNodes()) {
					output.write("<nd ref=\"" + node.getId() + "\"/>\n");
				}
				for (Tag tag : osmPolygon.getTags()) {
					String normalizedTag = getNormalizedTagText(tag);
					if (normalizedTag != null) {
						output.write(normalizedTag);
					}
	
				}
				output.write("</way>\n");
			}
			output.write("</osm>");
			output.flush();
		} catch (IOException e) {
			Activator.log(e);
		}
		new File(filePath).deleteOnExit();
		return filePath;
	}

	public static String CreateTempFile(String folderPath, OsmPolygon osmPolygon)
			throws IOException {

		String filePath = folderPath + File.separator + osmPolygon.getId()
				+ ".osm";
		try (BufferedWriter output = new BufferedWriter(new FileWriter(filePath, false))){
			output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			output.write("<osm version=\"0.6\" generator=\"osm2xp "
					+ Osm2xpConstants.OSM2XP_VERSION + "\">\n");
	
			for (Node node : osmPolygon.getNodes()) {
				output.write("<node id=\"" + node.getId() + "\" lat=\""
						+ node.getLat() + "\" lon=\"" + node.getLon()
						+ "\" version=\"1\" />\n");
			}
	
			output.write("<way id=\"" + osmPolygon.getId()
					+ "\" visible=\"true\" version=\"2\" >\n");
			for (Node nd : osmPolygon.getNodes()) {
				output.write("<nd ref=\"" + nd.getId() + "\"/>\n");
			}
			for (Tag tag : osmPolygon.getTags()) {
				String normalizedTag = getNormalizedTagText(tag);
				if (normalizedTag != null) {
					output.write(normalizedTag);
				}
	
			}
			output.write("</way>\n");
	
			output.write("</osm>");
			output.flush();
		} catch (IOException e) {
			Activator.log(e);
		}
		new File(filePath).deleteOnExit();
		return filePath;
	}
}
