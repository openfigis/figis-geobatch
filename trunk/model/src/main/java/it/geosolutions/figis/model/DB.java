package it.geosolutions.figis.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;




@XStreamAlias("DB")
public class DB {
	

	@XStreamAlias("database")	
	String database;
	

	@XStreamAlias("schema")	
	String schema;
	

	@XStreamAlias("user")
	String user;
	

	@XStreamAlias("password")	
	String password;
	

	@XStreamAlias("port")	
	String port;
	

	@XStreamAlias("host")	
	String host;
		
	public DB() {
		super();
	}
	
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}

	
	
}
