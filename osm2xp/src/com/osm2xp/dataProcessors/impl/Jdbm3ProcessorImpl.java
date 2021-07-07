package com.osm2xp.dataProcessors.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.osm2xp.constants.Osm2xpConstants;
import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.model.osm.Node;
import com.osm2xp.model.osm.Way;
import com.osm2xp.utils.FilesUtils;

import net.kotek.jdbm.DB;
import net.kotek.jdbm.DBMaker;

/**
 * Jdbm3 data sink implementation.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class Jdbm3ProcessorImpl extends AbstractDataProcessor {
	private DB db;
	private ConcurrentMap<Long, double[]> nodesMap;
	// private RecordManager recMan;
	private Map<Long, double[]> sink = new HashMap<Long, double[]>();

	public Jdbm3ProcessorImpl() throws DataSinkException {
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

		db = DBMaker.openFile(
				Osm2xpConstants.JDBM_PATH + File.separator + "nodes").make();
		// dbMaker.disableTransactions();
		// dbMaker.disableCache();
		// dbMaker=dbMaker.closeOnExit();
		// dbMaker=dbMaker.deleteFilesAfterClose();
		// dbMaker=dbMaker.disableTransactions();
		// dbMaker=dbMaker.enableHardCache();
		// db = dbMaker.make();

		nodesMap = db.createHashMap("nodes");

	}

	@Override
	public void storeNode(Node node) throws DataSinkException {
		double[] coords = new double[] { node.getLat(), node.getLon() };
		sink.put(node.getId(), coords);

		if (sink.size() == 100000) {
			injectNodesIntoStorage();

		}
	}

	private void injectNodesIntoStorage() {
		nodesMap.putAll(sink);
		db.commit();
		sink.clear();
		db.clearCache();
		System.out.println("taille= " + nodesMap.size());

	}

	@Override
	public Node getNode(Long id) throws DataSinkException {
		if (!sink.isEmpty()) {
			injectNodesIntoStorage();
		}
		Node result = null;
		double[] node = nodesMap.get(id);
		if (node != null) {
			result = new Node(null, node[0], node[1], id);
		}
		return result;

	}

	@Override
	public void complete() {
		db.close();

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
