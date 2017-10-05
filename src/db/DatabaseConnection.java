package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import util.PropertyLoader;

/**
 * Helper class for connecting to the database.
 * 
 * @author Francisco Frias
 * @author Abel Guzman
 * @author Amin Guzman
 *
 */
public class DatabaseConnection {
	/*
	 * Standard format in which the connection string must be generated
	 * protocol:subprotocol://host:port/schema?user=username&password=password
	 */
	private static final String CONNECTION_STRING_FORMAT = "%s:%s://%s:%s/%s?user=%s&password=%s";
	private static final String PROPERTIES_NAMESPACE = "/config/database.properties";
	private static PropertyLoader connectionProperties = new PropertyLoader(PROPERTIES_NAMESPACE);

	private Connection connection;

	public DatabaseConnection() throws SQLException {
		try {
			Class.forName(connectionProperties.getProperty("driver"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		String protocol = connectionProperties.getProperty("protocol");
		String subProtocol = connectionProperties.getProperty("subprotocol");
		String host = connectionProperties.getProperty("host");
		String port = connectionProperties.getProperty("port");
		String schema = connectionProperties.getProperty("schema");
		String username = connectionProperties.getProperty("username");
		String password = connectionProperties.getProperty("password");
		String connectionString = generateConnectionString(protocol, subProtocol, host, port, schema, username,
				password);

		connection = DriverManager.getConnection(connectionString);
	}

	public Connection getConnection() {
		return connection;
	}

	public void close() throws SQLException {
		connection.close();
	}

	private String generateConnectionString(String protocol, String subProtocol, String host, String port,
			String schema, String username, String password) {
		return String.format(CONNECTION_STRING_FORMAT, protocol, subProtocol, host, port, schema, username, password);
	}
}
