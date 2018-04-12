package com.osm2xp.translators.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.eclipse.core.runtime.preferences.InstanceScope;

import math.geom2d.Point2D;

import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.gui.Activator;
import com.osm2xp.model.osm.Node;
import com.osm2xp.model.osm.OsmPolygon;
import com.osm2xp.model.osm.Relation;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.model.osm.Way;
import com.osm2xp.model.stats.GenerationStats;
import com.osm2xp.model.xplane.XplaneDsf3DObject;
import com.osm2xp.model.xplane.XplaneDsfObject;
import com.osm2xp.translators.ITranslationListener;
import com.osm2xp.translators.ITranslator;
import com.osm2xp.utils.GeomUtils;
import com.osm2xp.utils.MiscUtils;
import com.osm2xp.utils.OsmUtils;
import com.osm2xp.utils.DsfObjectsProvider;
import com.osm2xp.utils.helpers.GuiOptionsHelper;
import com.osm2xp.utils.helpers.StatsHelper;
import com.osm2xp.utils.helpers.XplaneExclusionsHelper;
import com.osm2xp.utils.helpers.XplaneOptionsHelper;
import com.osm2xp.utils.logging.Osm2xpLogger;
import com.osm2xp.writers.IWriter;

/**
 * Xplane 10 translator implementation. Generates Xplane scenery from osm data.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class Xplane10TranslatorImpl implements ITranslator {

	/**
	 * Residential buildings maximum area.
	 */
	private final static double ASSERTION_RESIDENTIAL_MAX_AREA = 0.5;
	/**
	 * Buildings minimum vectors.
	 */
	private final static int BUILDING_MIN_VECTORS = 3;
	/**
	 * Buildings maximum vectors.
	 */
	private final static int BUILDING_MAX_VECTORS = 512;
	/**
	 * current lat/long tile.
	 */
	private Point2D currentTile;
	
	/**
	 * Line separator
	 */
	private static final String LINE_SEP = System.getProperty("line.separator");
	
	/**
	 * stats object.
	 */
	private GenerationStats stats;
	/**
	 * file writer.
	 */
	private IWriter writer;
	/**
	 * start time.
	 */
	private Date startTime;
	/**
	 * generated scenery folder path.
	 */
	private String folderPath;
	/**
	 * dsf object provider.
	 */
	private DsfObjectsProvider dsfObjectsProvider;
	/**
	 * Smart exclusions helper.
	 */
	private XplaneExclusionsHelper exclusionsHelper = new XplaneExclusionsHelper();
	
	private ITranslationListener translationListener;
	
	/**
	 * Building level height, 3 m by default 
	 */
	private double levelHeight = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).getDouble("levelHeight", 3);

	/**
	 * Constructor.
	 * 
	 * @param stats
	 *            stats object.
	 * @param writer
	 *            file writer.
	 * @param currentTile
	 *            current lat/long tile.
	 * @param folderPath
	 *            generated scenery folder path.
	 * @param dsfObjectsProvider
	 *            dsf object provider.
	 */
	public Xplane10TranslatorImpl(GenerationStats stats, IWriter writer,
			Point2D currentTile, String folderPath,
			DsfObjectsProvider dsfObjectsProvider) {
		this.currentTile = currentTile;
		this.stats = stats;
		this.writer = writer;
		this.folderPath = folderPath;
		this.dsfObjectsProvider = dsfObjectsProvider;
		this.startTime = new Date();
	}

	@Override
	public void complete() {

		// if smart exclusions enabled and tile is not empty, send them to
		// writer
		if (!StatsHelper.isTileEmpty(stats)
				&& XplaneOptionsHelper.getOptions().isSmartExclusions()) {
			String exclusions = exclusionsHelper.exportExclusions();
			writer.complete(exclusions);

		} else {
			writer.complete(null);
		}

		if (currentTile != null && !StatsHelper.isTileEmpty(stats)) {
			Osm2xpLogger.info("Tile " + (int) currentTile.x + "/"
					+ (int) currentTile.y + " stats : "
					+ stats.getBuildingsNumber() + " buildings, "
					+ stats.getForestsNumber() + " forests, "
					+ stats.getStreetlightsNumber() + " street lights, "
					+ stats.getObjectsNumber() + " objects. (generation took "
					+ MiscUtils.getTimeDiff(startTime, new Date()) + ")");

			// stats
			try {
				if (XplaneOptionsHelper.getOptions().isGenerateXmlStats()
						|| XplaneOptionsHelper.getOptions()
								.isGeneratePdfStats()) {
					StatsHelper.getStatsList().add(stats);

				}
				if (XplaneOptionsHelper.getOptions().isGenerateXmlStats()) {
					StatsHelper.saveStats(folderPath, currentTile, stats);
				}
				if (XplaneOptionsHelper.getOptions().isGeneratePdfStats()) {
					StatsHelper.generatePdfReport(folderPath, stats);
				}
			} catch (Osm2xpBusinessException e) {
				Osm2xpLogger.error("Error saving stats file for tile "
						+ currentTile, e);
			}
		} else if (!GuiOptionsHelper.getOptions().isSinglePass()) {
			Osm2xpLogger.info("Tile " + (int) currentTile.x + "/"
					+ (int) currentTile.y + " is empty, no dsf generated");
		}
		translationListener.complete();
	}

	@Override
	public void init() {
		// writer initialization
		writer.init(currentTile);
		// exclusionHelper
		if (XplaneOptionsHelper.getOptions().isSmartExclusions()) {
			exclusionsHelper.run();
		}

	}

	/**
	 * Write streetlight objects in dsf file.
	 * 
	 * @param osmPolygon
	 *            osm road polygon
	 */
	public void writeStreetLightToDsf(OsmPolygon osmPolygon) {
		// init d'un entier pour modulo densité street lights
		Integer densityIndex = 0;
		if (XplaneOptionsHelper.getOptions().getLightsDensity() == 0) {
			densityIndex = 10;
		} else {
			if (XplaneOptionsHelper.getOptions().getLightsDensity() == 1) {
				densityIndex = 5;
			} else {
				if (XplaneOptionsHelper.getOptions().getLightsDensity() == 2)
					densityIndex = 3;
			}
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < osmPolygon.getPolygon().getVertices().size(); i++) {
			if ((i % densityIndex) == 0) {
				Point2D lightLoc = osmPolygon.getPolygon().getVertex(i);
				lightLoc.x = lightLoc.x + 0.0001;
				lightLoc.y = lightLoc.y + 0.0001;
				if (GeomUtils.compareCoordinates(lightLoc, currentTile)) {
					Random randomGenerator = new Random();
					int orientation = randomGenerator.nextInt(360);
					sb.append("OBJECT "
							+ dsfObjectsProvider.getRandomStreetLightObject()
							+ " " + (lightLoc.y) + " " + (lightLoc.x) + " "
							+ orientation);
					sb.append(LINE_SEP);
					// stats
					StatsHelper.addStreetLight(stats);
				}
			}
		}

		writer.write(sb.toString());
	}

	/**
	 * write a building in the dsf file.
	 * 
	 * @param polygon
	 * @param facade
	 * @param size
	 */
	private void writeBuildingToDsf(OsmPolygon osmPolygon, Integer facade) {
		if (facade != null && osmPolygon.getHeight() != null) {
			StringBuffer sb = new StringBuffer();
			osmPolygon.setPolygon(GeomUtils.setClockwise(osmPolygon
					.getPolygon()));
			if (osmPolygon.getPolygon().getArea() * 100000000 > 0.1
					&& osmPolygon.getPolygon().getVertexNumber() > 3) {

				sb.append("BEGIN_POLYGON " + facade + " "
						+ osmPolygon.getHeight() + " 2");
				sb.append(LINE_SEP);
				sb.append("BEGIN_WINDING");
				sb.append(LINE_SEP);

				// on supprime le dernier point pour ne pas boucler
				osmPolygon.getPolygon().removePoint(
						osmPolygon.getPolygon().getLastPoint());
				for (Point2D loc : osmPolygon.getPolygon().getVertices()) {
					sb.append("POLYGON_POINT " + loc.y + " " + loc.x);
					sb.append(LINE_SEP);
				}
				sb.append("END_WINDING");
				sb.append(LINE_SEP);
				sb.append("END_POLYGON");
				sb.append(LINE_SEP);

				// stats TODO not working anymore since v2 facades new features.
				if (dsfObjectsProvider.getPolygonsList().get(facade)
						.toLowerCase().contains("building")
						|| dsfObjectsProvider.getPolygonsList().get(facade)
								.toLowerCase().contains("shape")) {
					StatsHelper.addBuildingType("Building", stats);
				} else {
					if (dsfObjectsProvider.getPolygonsList().get(facade)
							.toLowerCase().contains("house")
							|| dsfObjectsProvider.getPolygonsList().get(facade)
									.toLowerCase().contains("common")) {
						StatsHelper.addBuildingType("Residential", stats);
					} else {
						StatsHelper.addBuildingType("Facade rule", stats);
					}
				}
				writer.write(sb.toString(), GeomUtils
						.cleanCoordinatePoint(osmPolygon.getPolygon()
								.getFirstPoint()));
			}
		}
	}

	/**
	 * Compute the height for this polygon and osm tags.
	 * 
	 * 
	 * @return Integer the height.
	 */
	private Integer computeBuildingHeight(OsmPolygon polygon) {
		Integer result = null;
		Integer osmHeight = polygon.getHeight();
		if (osmHeight != null) {
			result = osmHeight;
		} else {
			String value = polygon.getTagValue("building:levels");
			if (value != null) {
				try {
					int levels = Integer.parseInt(value);
					return (int) Math.round(levels * levelHeight);
				} catch (NumberFormatException e) {
					//Ignore. Should we log it?
				}
				
			}
			
			if (polygon.getArea() * 10000000 < 0.2) {
				result = XplaneOptionsHelper.getOptions().getResidentialMin();
			} else {
				if (polygon.getArea() * 10000000 > ASSERTION_RESIDENTIAL_MAX_AREA)
					result = MiscUtils.getRandomSize(XplaneOptionsHelper
							.getOptions().getBuildingMin(), XplaneOptionsHelper
							.getOptions().getBuildingMax());
				else if (polygon.getArea() * 10000000 < ASSERTION_RESIDENTIAL_MAX_AREA)
					result = MiscUtils.getRandomSize(XplaneOptionsHelper
							.getOptions().getResidentialMin(),
							XplaneOptionsHelper.getOptions()
									.getResidentialMax());
			}
		}
		return result;
	}

	/**
	 * compute the dsf index of the residential facade object used for a given
	 * polygon
	 * 
	 * @param polygon
	 * @param height
	 * @return Integer the facade index
	 */
	private Integer computeResidentialFacadeIndex(OsmPolygon osmPolygon) {
		Integer result = null;
		// we check if we can use a sloped roof if the user wants them
		if (XplaneOptionsHelper.getOptions().isGenerateSlopedRoofs()
				&& osmPolygon.isSimplePolygon()) {
			result = dsfObjectsProvider.computeFacadeDsfIndex(true, true, true,
					osmPolygon);
		}
		// no sloped roof, so we'll use a standard house facade
		else {

			// if the polygon is a simple rectangle, we'll use a facade made for
			// simple shaped buildings
			if (osmPolygon.getPolygon().getEdges().size() == 4) {
				result = dsfObjectsProvider.computeFacadeDsfIndex(true, true,
						false, osmPolygon);
			}
			// the building has a complex footprint, so we'll use a facade made
			// for this case
			else {
				result = dsfObjectsProvider.computeFacadeDsfIndex(false, true,
						false, osmPolygon);
			}
		}
		return result;
	}

	/**
	 * compute the dsf index of the building facade object used for a given
	 * polygon
	 * 
	 * @param polygon
	 * @param height
	 * @param roofColor
	 * @return
	 */
	private Integer computeBuildingFacadeIndex(OsmPolygon osmPolygon) {
		Integer result = null;
		// if the polygon is a simple rectangle, we'll use a facade made for
		// simple shaped buildings
		if (osmPolygon.getPolygon().getEdges().size() == 4) {
			result = dsfObjectsProvider.computeFacadeDsfIndex(true, false,
					false, osmPolygon);
		}
		// the building has a complex footprint, so we'll use a facade made
		// for this case
		else {
			result = dsfObjectsProvider.computeFacadeDsfIndex(false, false,
					false, osmPolygon);
		}
		return result;
	}

	/**
	 * Compute the facade index for given osm tags, polygon and height.
	 * 
	 * @param tags
	 * @param polygon
	 * @param height
	 * @return Integer the facade index.
	 */
	public Integer computeFacadeIndex(OsmPolygon polygon) {
		Integer result = null;
		// first, do we are on a residential object?
		// yes if there is residential or house tag
		// or if surface of the polygon is under the max surface for a
		// residential house
		// and height is under max residential height
		if ((OsmUtils.isValueinTags("residential", polygon.getTags())
				|| OsmUtils.isValueinTags("house", polygon.getTags()) || polygon
				.getArea() * 10000000 < ASSERTION_RESIDENTIAL_MAX_AREA)
				&& polygon.getHeight() < XplaneOptionsHelper.getOptions()
						.getResidentialMax()) {

			result = computeResidentialFacadeIndex(polygon);
		}

		// do we are on a building object?
		// yes if there is industrial or Commercial tag
		// or if surface of the polygon is above the max surface for a
		// residential house
		// and height is above max residential height
		else {
			if (OsmUtils.isValueinTags("industrial", polygon.getTags())
					|| OsmUtils.isValueinTags("Commercial", polygon.getTags())
					|| polygon.getArea() * 10000000 > ASSERTION_RESIDENTIAL_MAX_AREA
					|| polygon.getHeight() > XplaneOptionsHelper.getOptions()
							.getResidentialMax()) {

				result = computeBuildingFacadeIndex(polygon);
			}
		}
		return result;
	}

	/**
	 * write a 3D object in the dsf file
	 * 
	 * @param object
	 *            a xplane dsf object
	 * @throws Osm2xpBusinessException
	 */
	private void writeObjectToDsf(XplaneDsfObject object)
			throws Osm2xpBusinessException {

		String objectDsfText = object.asObjDsfText();
		writer.write(objectDsfText, GeomUtils.cleanCoordinatePoint(object
				.getOsmPolygon().getCenter()));
		// stats
		StatsHelper.addObjectType(
				dsfObjectsProvider.getObjectsList().get(object.getDsfIndex()),
				stats);

	}

	@Override
	public void processNode(Node node) throws Osm2xpBusinessException {
		// process the node if we're on a single pass mode.
		// if not on single pass, only process if the node is on the current
		// lat/long tile
		if (XplaneOptionsHelper.getOptions().isGenerateObj()) {
			if ((!GuiOptionsHelper.getOptions().isSinglePass() && GeomUtils
					.compareCoordinates(currentTile, node))
					|| GuiOptionsHelper.getOptions().isSinglePass()) {
				// write a 3D object in the dsf file if this node is in an
				// object
				// rule
				XplaneDsf3DObject object = dsfObjectsProvider
						.getRandomDsfObjectIndexAndAngle(node.getTag(),
								node.getId());
				if (object != null) {
					List<Node> nodes = new ArrayList<Node>();
					nodes.add(node);
					object.setPolygon(new OsmPolygon(node.getId(), node
							.getTag(), nodes));
					writeObjectToDsf(object);
				}
			}
		}
	}

	@Override
	public void processPolygon(OsmPolygon osmPolygon)
			throws Osm2xpBusinessException {

		// polygon is null or empty don't process it
		if (osmPolygon.getNodes() != null && !osmPolygon.getNodes().isEmpty()) {
			// polygon MUST be in clockwise order
			osmPolygon.setPolygon(GeomUtils.forceClockwise(osmPolygon
					.getPolygon()));
			// if we're on a single pass mode
			// here we must check if the polygon is on more than one tile
			// if that's the case , we must split it into several polys
			List<OsmPolygon> polygons = new ArrayList<OsmPolygon>();
			if (GuiOptionsHelper.getOptions().isSinglePass()) {
				polygons.addAll(osmPolygon.splitPolygonAroundTiles());
			}
			// if not on a single pass mode, add this single polygon to the poly
			// list
			else {
				polygons.add(osmPolygon);
			}
			// try to transform those polygons into dsf objects.
			for (OsmPolygon poly : polygons) {
				// look for light rules
				processLightObject(poly);

				// try to generate a 3D object
				if (!process3dObject(poly)) {
					// nothing generated? try to generate a facade building.
					if (!processBuilding(poly)) {
						// nothing generated? try to generate a forest.
						if (!processForest(poly)) {
							processRoad(poly);
						}
					}
				}
			}
		}
	}

	private void processRoad(OsmPolygon poly) {
		if (poly.getTagValue("highway") != null &&
				"asphalt".equals(poly.getTagValue("surface"))) {
			translationListener.processRoad(poly);
		}
	}

	private void processLightObject(OsmPolygon poly) {
		if (XplaneOptionsHelper.getOptions().isGenerateLights()) {
			XplaneDsfObject object = dsfObjectsProvider
					.getRandomDsfLightObject(poly);
			if (object != null) {
				object.setPolygon(poly);
				try {
					writeObjectToDsf(object);

				} catch (Osm2xpBusinessException e) {
				}
			}
		}
	}

	/**
	 * Construct and write a facade building in the dsf file.
	 * 
	 * @param osmPolygon
	 *            osm polygon
	 * @return true if a building has been gennerated in the dsf file.
	 */
	private boolean processBuilding(OsmPolygon osmPolygon) {
		Boolean result = false;
		if (XplaneOptionsHelper.getOptions().isGenerateBuildings()
				&& OsmUtils.isBuilding(osmPolygon.getTags())
				&& !OsmUtils.isExcluded(osmPolygon.getTags(),
						osmPolygon.getId())
				&& osmPolygon.getPolygon().getVertexNumber() > BUILDING_MIN_VECTORS
				&& osmPolygon.getPolygon().getVertexNumber() < BUILDING_MAX_VECTORS) {

			// check that the largest vector of the building
			// and that the area of the osmPolygon.getPolygon() are over the
			// minimum values set by the user
			Double maxVector = osmPolygon.getMaxVectorSize();
			if (maxVector > XplaneOptionsHelper.getOptions()
					.getMinHouseSegment()
					&& maxVector < XplaneOptionsHelper.getOptions()
							.getMaxHouseSegment()
					&& ((osmPolygon.getPolygon().getArea() * 100000) * 100000) > XplaneOptionsHelper
							.getOptions().getMinHouseArea()) {

				// simplify shape if checked and if necessary
				if (GuiOptionsHelper.getOptions().isSimplifyShapes()
						&& !osmPolygon.isSimplePolygon()) {
					osmPolygon.simplifyPolygon();
				}

				// compute height and facade dsf index
				osmPolygon.setHeight(computeBuildingHeight(osmPolygon));
				Integer facade = computeFacadeIndex(osmPolygon);
				if (translationListener != null) {
					translationListener.processBuilding(osmPolygon, facade);
				}
				
				// write building in dsf file
				writeBuildingToDsf(osmPolygon, facade);
				// Smart exclusions
				if (XplaneOptionsHelper.getOptions().isSmartExclusions()) {
					exclusionsHelper.addTodoPolygon(osmPolygon);
					exclusionsHelper.run();
				}
				result = true;
			}
		}
		return result;
	}

	/**
	 * choose and write a 3D object in the dsf file.
	 * 
	 * @param polygon
	 *            osm polygon.
	 * @return true if a 3D object has been written in the dsf file.
	 */
	private boolean process3dObject(OsmPolygon osmPolygon) {
		Boolean result = false;

		if (XplaneOptionsHelper.getOptions().isGenerateObj()) {
			// simplify shape if checked and if necessary
			if (GuiOptionsHelper.getOptions().isSimplifyShapes()
					&& !osmPolygon.isSimplePolygon()) {
				osmPolygon.simplifyPolygon();
			}
			XplaneDsfObject object = dsfObjectsProvider
					.getRandomDsfObject(osmPolygon);
			if (object != null) {
				object.setPolygon(osmPolygon);
				try {
					writeObjectToDsf(object);
					result = true;
				} catch (Osm2xpBusinessException e) {
					result = false;
				}

			}
		}
		return result;
	}

	/**
	 * send a streetLight in the dsf file.
	 * 
	 * @param osmPolygon
	 *            osm polygon
	 * @return true if a streetlight has been written in the dsf file.
	 */
	private boolean processStreetLights(OsmPolygon osmPolygon) {
		Boolean result = false;
		if (XplaneOptionsHelper.getOptions().isGenerateStreetLights()
				&& OsmUtils.isTagInTagsList("highway", "residential",
						osmPolygon.getTags())) {
			writeStreetLightToDsf(osmPolygon);
			result = true;
		}
		return result;
	}

	/**
	 * @param way
	 * @param polygon
	 * @return
	 */
	private boolean processForest(OsmPolygon osmPolygon) {
		Boolean result = false;
		if (XplaneOptionsHelper.getOptions().isGenerateFor()) {
			Integer[] forestIndexAndDensity = dsfObjectsProvider
					.getRandomForestIndexAndDensity(osmPolygon.getTags());
			if (forestIndexAndDensity != null) {
				if (translationListener != null) {
					translationListener.processForest(osmPolygon);
				}
				writeForestToDsf(osmPolygon, forestIndexAndDensity);
				result = true;
			}
		}
		return result;
	}

	/**
	 * @param polygon
	 *            the forest polygon
	 * @param forestIndexAndDensity
	 *            index and density of the forest rule
	 */
	private void writeForestToDsf(OsmPolygon osmPolygon,
			Integer[] forestIndexAndDensity) {
		StringBuffer sb = new StringBuffer();
		sb.append("BEGIN_POLYGON " + forestIndexAndDensity[0] + " "
				+ forestIndexAndDensity[1] + " 2");
		sb.append(LINE_SEP);
		sb.append("BEGIN_WINDING");
		sb.append(LINE_SEP);
		for (Point2D loc : osmPolygon.getPolygon().getVertices()) {
			sb.append("POLYGON_POINT " + loc.y + " " + loc.x);
			sb.append(LINE_SEP);
		}
		sb.append("END_WINDING");
		sb.append(LINE_SEP);
		sb.append("END_POLYGON");
		sb.append(LINE_SEP);

		// stats
		StatsHelper.addForestType(
				dsfObjectsProvider.getPolygonsList().get(
						forestIndexAndDensity[0]), stats);

		writer.write(sb.toString(), GeomUtils.cleanCoordinatePoint(osmPolygon
				.getPolygon().getFirstPoint()));
	}

	@Override
	public void processRelation(Relation relation)
			throws Osm2xpBusinessException {
	}

	@Override
	public Boolean mustStoreNode(Node node) {
		Boolean result = true;
		if (!GuiOptionsHelper.getOptions().isSinglePass()) {
			result = GeomUtils.compareCoordinates(currentTile, node);
		}
		return result;
	}

	@Override
	public Boolean mustStoreWay(Way way) {
		List<Tag> tags = way.getTag();
		return (OsmUtils.isBuilding(tags) || OsmUtils.isForest(tags) || OsmUtils
				.isObject(tags));
	}

	public void setTranslationListener(ITranslationListener translationListener) {
		this.translationListener = translationListener;
	}
}
