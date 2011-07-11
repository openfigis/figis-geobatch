/**
 * 
 */
package it.geosolutions.ie.config;

/**
 * @author Fabiani
 *
 */
public class DataStoreParams {

	private final String hostname;
	private final Integer port;
	private final String database;
	private final String schema;
	private final String user;
	private final String password;
	
	/**
	 * @param hostname
	 * @param port
	 * @param database
	 * @param schema
	 * @param user
	 * @param password
	 */
	public DataStoreParams(String hostname, Integer port, String database,
			String schema, String user, String password) {
		super();
		this.hostname = hostname;
		this.port = port;
		this.database = database;
		this.schema = schema;
		this.user = user;
		this.password = password;
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * @return the schema
	 */
	public String getSchema() {
		return schema;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	
}
