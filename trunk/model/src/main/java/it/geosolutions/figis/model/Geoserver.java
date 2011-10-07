package it.geosolutions.figis.model;



import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("geoserver")
public class Geoserver {
	

	@XStreamAlias("geoserverUrl")
	String geoserverUrl;
	

	@XStreamAlias("geoserverUsername")
	String geoserverUsername;
	

	@XStreamAlias("geoserverPassword")
	String geoserverPassword;
	

	public Geoserver() {
		super();
	}
	public String getGeoserverUrl() {
		return geoserverUrl;
	}
	public void setGeoserverUrl(String geoserverUrl) {
		this.geoserverUrl = geoserverUrl;
	}
	public String getGeoserverUsername() {
		return geoserverUsername;
	}
	public void setGeoserverUsername(String geoserverUsername) {
		this.geoserverUsername = geoserverUsername;
	}
	public String getGeoserverPassword() {
		return geoserverPassword;
	}
	public void setGeoserverPassword(String geoserverPassword) {
		this.geoserverPassword = geoserverPassword;
	}

}
