package com.osm2xp.dataProcessors;

import java.util.List;

import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.model.osm.Node;
import com.osm2xp.model.osm.Way;

/**
 * Data sink interface. Store open street map nodes.
 * 
 * @author Benjamin Blanchet
 * 
 */
public interface IDataSink {

	/**
	 * Store a node in the storage implementation.
	 * 
	 * @param node
	 * @throws DataSinkException
	 */
	void storeNode(Node node) throws DataSinkException;

	/**
	 * find a node in the storage implementation.
	 * 
	 * @param nodeId
	 * @return a osm node
	 * @throws DataSinkException
	 */
	Node getNode(Long id) throws DataSinkException;

	/**
	 * find a list of nodes in the storage implementation.
	 * 
	 * @param nodeIds
	 * @return a list of osm nodes
	 */
	List<Node> getNodes(List<Long> nodeIds) throws DataSinkException;

	/**
	 * called on completion of generation job.
	 */
	public void complete() throws DataSinkException;

	/**
	 * return the number of stored nodes
	 * 
	 * @return
	 */
	public Long getNodesNumber();


	/**
	 * Store a way in the storage implementation.
	 * 
	 * @param way way to store
	 */
	void storeWay(Way way);
	
	/**
	 * Find a way in the storage implementation.
	 * 
	 * @param wayId
	 * @return corresponding {@link Way} or <code>null</code>
	 */
	Way getWay(long wayId);
}
