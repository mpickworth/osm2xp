package com.osm2xp.dataProcessors.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.h2.tools.Server;

import com.osm2xp.exceptions.DataSinkException;
import com.osm2xp.model.osm.Node;
import com.osm2xp.model.osm.Way;
import com.osm2xp.utils.logging.Osm2xpLogger;

/**
 * H2DBD Data sink implementation.
 * 
 * @author Benjamin Blanchet
 * 
 */
public class H2DBDProcessorImpl extends AbstractDataProcessor {

	private static final String QUERY_SELECT_NODE = "SELECT * FROM nodes where id=";
	private static final String QUERY_INSERT_NODE = "INSERT INTO NODES (id,latitude,longitude) VALUES ";
	private static final String QUERY_CREATE_NODES_TABLE = "CREATE TABLE NODES(id LONG PRIMARY KEY, latitude DOUBLE,longitude DOUBLE)";
	private static final String QUERY_DROP_NODES_TABLE = "DROP TABLE IF EXISTS NODES";
	private Server server;
	private Connection conn;

	public H2DBDProcessorImpl() throws DataSinkException {
		initDB();
	}

	/**
	 * initialization of the h2 database engine
	 * 
	 * @throws DataSinkException
	 */
	private void initDB() throws DataSinkException {
		try {
			server = Server.createTcpServer().start();
			Class.forName("org.h2.Driver");

			conn = DriverManager.getConnection("jdbc:h2:~/db", "sa", "");

			conn.prepareStatement(QUERY_DROP_NODES_TABLE).execute();
			conn.prepareStatement(QUERY_CREATE_NODES_TABLE).execute();
		} catch (SQLException e) {
			throw new DataSinkException("H2DB initialisation failed", e);
		} catch (ClassNotFoundException e) {
			throw new DataSinkException("H2DB initialisation failed", e);
		}

	}

	@Override
	public void storeNode(Node node) throws DataSinkException {
		String query = QUERY_INSERT_NODE + "(" + node.getId() + ","
				+ node.getLat() + "," + node.getLon() + ")";
		try {
			conn.prepareStatement(query).execute();
		} catch (SQLException e) {
			throw new DataSinkException("H2DB node record error", e);
		}

	}

	@Override
	public Node getNode(Long id) throws DataSinkException {
		String query = QUERY_SELECT_NODE + id;
		Node node = null;
		try {
			Statement stmt = conn.createStatement();
			long dateStart = new Date().getTime();
			ResultSet result = stmt.executeQuery(query);
			if (result.next()) {
				node = new Node();
				node.setId(id);
				node.setLat(result.getDouble("LATITUDE"));
				node.setLon(result.getDouble("LONGITUDE"));
				long dateEnd = new Date().getTime();
				Osm2xpLogger.info("node found in " + (dateEnd - dateStart)
						+ " ms");
			}
		} catch (SQLException e) {
			throw new DataSinkException("H2DB get node error", e);
		}
		return node;
	}

	@Override
	public Long getNodesNumber() {
		return null;
	}

	@Override
	public void complete() throws DataSinkException {
		server.stop();
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
