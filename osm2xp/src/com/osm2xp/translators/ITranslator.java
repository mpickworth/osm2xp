package com.osm2xp.translators;

import java.util.List;

import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBBox;

import com.osm2xp.exceptions.Osm2xpBusinessException;
import com.osm2xp.model.osm.Node;
import com.osm2xp.model.osm.OsmPolyline;
import com.osm2xp.model.osm.Relation;
import com.osm2xp.model.osm.Tag;
import com.osm2xp.model.osm.Way;

/**
 * ITranslator.
 * 
 * @author Benjamin Blanchet
 * 
 */
public interface ITranslator {

	/**
	 * process an open street map node.
	 * 
	 * @param node
	 *            osm node
	 * @throws Osm2xpBusinessException
	 */
	public void processNode(Node node) throws Osm2xpBusinessException;

	/**
	 * process an open street map polygon.
	 * 
	 * @param OsmPolygon
	 *            osm polygon
	 * @throws Osm2xpBusinessException
	 */
	public void processPolyline(OsmPolyline polyline)
			throws Osm2xpBusinessException;

	/**
	 * process an open street map relation.
	 * 
	 * @param relation
	 *            osm relation
	 * @throws Osm2xpBusinessException
	 */
	public void processRelation(Relation relation)
			throws Osm2xpBusinessException;

	/**
	 * translation of the file is complete.
	 */
	public void complete();

	/**
	 * initialization of the translator.
	 * 
	 * @throws Osm2xpBusinessException
	 */
	public void init();

	/**
	 * Tells if the given node must be stored.
	 * 
	 * @param node
	 *            osm node
	 * @return true if this node is of interest for this translator.
	 */
	public Boolean mustStoreNode(Node node);

	/**
	 * Tells if the given way must be processed.
	 * 
	 * @param way
	 *            osm way
	 * @return true if this way is of interest for this translator.
	 */
	public Boolean mustProcessWay(Way way);
	
	/**
	 * Tells whether given polyline or polygon should be processed.
	 * 
	 * @param tags list of tags for selected object
	 * @return true if this poly is of interest for this translator.
	 */
	public Boolean mustProcessPolyline(List<Tag> tags);
	
	/**
	 * Returns maximum hole count for polyline with given tags. 0 would mean no holes allowed
	 * @param tags Tags for polyline being analyzed
	 * @return max hole count for polygon with given tags. 0 - no holes allowed, you can use Integer.MAX_VALUE to indicate no restriction
	 */
	public int getMaxHoleCount(List<Tag> tags);

	public void processBoundingBox(HeaderBBox bbox);

}
