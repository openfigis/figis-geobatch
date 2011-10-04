package it.geosolutions.figis.persistence.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@XStreamAlias("DB")
public class DB {
	
	@Column(name = "DATABASE", nullable = false, length = 100)
	@XStreamAlias("database")	
	String database;
	
	@Column(name = "SCHEMA", nullable = true, length = 100)
	@XStreamAlias("schema")	
	String schema;
	
	@Column(name = "USER", nullable = false, length = 100)
	@XStreamAlias("user")
	String user;
	
	@Column(name = "PASSWORD", nullable = false, length = 100)
	@XStreamAlias("password")	
	String password;
	
	@Column(name = "PORT", nullable = false, length = 100)
	@XStreamAlias("port")	
	String port;
	
	@Column(name = "HOST", nullable = false, length = 100)	
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
