package com.osm2xp.dataProcessors.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.osm2xp.dataProcessors.IDataSink;
import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.model.osm.Node;

/**
 * Memory data sink implementation
 * 
 * @author Benjamin Blanchet
 * 
 */
public class MemoryProcessorImpl implements IDataSink {

	private Map<Long, double[]> nodeMap = new HashMap<Long, double[]>();

	@Override
	public void storeNode(final Node node) throws DataSinkException {
		double[] pt = new double[] { node.getLat(), node.getLon() };
		this.nodeMap.put(node.getId(), pt);
	}

	@Override
	public Node getNode(final Long id) throws DataSinkException {
		double[] loc = nodeMap.get(id);
		if (loc != null) {
			Node node = new Node();
			node.setId(id);
			node.setLat(loc[0]);
			node.setLon(loc[1]);
			return node;
		}
		return null;
	}

	@Override
	public List<Node> getNodes(final List<Long> ids) throws DataSinkException {
		final List<Node> nodes = new ArrayList<Node>();
		for (Long nd : ids) {
			final Node node = getNode(nd);
			if (node != null) {
				nodes.add(node);
			} 
		}
		return nodes.size() > 0 ? nodes : null;
	}
	
	@Override
	public void complete() {
		nodeMap = null;

	}

	@Override
	public Long getNodesNumber() {
		return (long) nodeMap.size();
	}

}
