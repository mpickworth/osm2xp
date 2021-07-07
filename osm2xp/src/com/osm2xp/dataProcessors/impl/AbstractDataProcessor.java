package com.osm2xp.dataProcessors.impl;

import java.util.ArrayList;
import java.util.List;

import com.osm2xp.dataProcessors.IDataSink;
import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.model.osm.Node;

public abstract class AbstractDataProcessor implements IDataSink {

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
	
}
