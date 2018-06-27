package com.osm2xp.dataProcessors.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.osm2xp.constants.Osm2xpConstants;
import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.model.osm.Node;
import com.osm2xp.model.osm.Way;
import com.osm2xp.utils.FilesUtils;

import jdbm.PrimaryTreeMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

/**
 * Jdbm2 data sink implementation.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class Jdbm2ProcessorImpl extends AbstractDataProcessor {
	private RecordManager recman;
	private PrimaryTreeMap<Long, double[]> nodesMap;
	// private RecordManager recMan;
	private Map<Long, double[]> sink = new HashMap<Long, double[]>();

	public Jdbm2ProcessorImpl() throws DataSinkException {
		initDB();
	}

	/**
	 * initialization of the h2 database engine
	 * 
	 * @throws DataSinkException
	 */
	private void initDB() throws DataSinkException {
		File jdbmDirectory = new File(Osm2xpConstants.JDBM_PATH);
		// delete jdbm objects
		if (jdbmDirectory.exists()) {
			FilesUtils.deleteDirectory(jdbmDirectory);
		}
		// create directory
		jdbmDirectory.mkdirs();
		try {
			recman = RecordManagerFactory
					.createRecordManager(Osm2xpConstants.JDBM_PATH
							+ File.separator + "nodes");
		} catch (IOException e) {
			throw new DataSinkException("Jdbm2 initialization failed", e);
		}
		nodesMap = recman.treeMap("nodes");

	}

	@Override
	public void storeNode(Node node) throws DataSinkException {
		double[] coords = new double[] { node.getLat(), node.getLon() };
		sink.put(node.getId(), coords);

		if (sink.size() == 100000) {
			try {
				injectNodesIntoStorage();
			} catch (IOException e) {
				throw new DataSinkException("Jdbm2 node injection failed", e);
			}

		}
	}

	private void injectNodesIntoStorage() throws IOException {
		nodesMap.putAll(sink);
		recman.commit();
		sink.clear();
		System.out.println("taille= " + nodesMap.size());

	}

	@Override
	public Node getNode(Long id) throws DataSinkException {
		if (!sink.isEmpty()) {
			try {
				injectNodesIntoStorage();
			} catch (IOException e) {
				throw new DataSinkException("Jdbm2 node injection failed", e);
			}
		}
		Node result = null;
		double[] node = nodesMap.get(id);
		if (node != null) {
			result = new Node(null, node[0], node[1], id);
		}
		return result;

	}

	@Override
	public void complete() throws DataSinkException {
		try {
			recman.close();
		} catch (IOException e) {
			throw new DataSinkException("Jdbm2 close failed", e);
		}

	}

	@Override
	public Long getNodesNumber() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void storeWay(Way way) {
		// TODO Not supported yet
	}

	@Override
	public Way getWay(long wayId) {
		//TODO not supported yet
		return null;
	}

}
