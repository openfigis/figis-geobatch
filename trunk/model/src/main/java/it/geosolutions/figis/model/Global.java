package it.geosolutions.figis.model;


import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("global")
public class Global {
	

	@XStreamAlias("geoserver")
	Geoserver geoserver;
	

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
