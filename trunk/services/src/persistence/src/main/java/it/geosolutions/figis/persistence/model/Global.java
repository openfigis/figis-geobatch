package it.geosolutions.figis.persistence.model;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@Embeddable
@XStreamAlias("global")
public class Global {
	
	@Embedded
	@XStreamAlias("geoserver")
	Geoserver geoserver;
	
	@Embedded
	@XStreamAlias("db")
	DB db;


	public Global() {
		super();
		this.geoserver = new Geoserver();
		this.db = new DB();
	}

	public Geoserver getGeoserver() {
		return geoserver;
	}

	public void setGeoserver(Geoserver geoserver) {
		this.geoserver = geoserver;
	}

	public DB getDb() {
		return db;
	}

	public void setDb(DB db) {
		this.db = db;
	}
	

}
