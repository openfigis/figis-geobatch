package it.geosolutions.figis.persistence.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@Embeddable
@XStreamAlias("geoserver")
public class Geoserver {
	
	@Column(name = "GEOSERVER_URL", nullable = false, length = 100)
	@XStreamAlias("geoserverUrl")
	String geoserverUrl;
	
	@Column(name = "GEOSERVER_USERNAME", nullable = false, length = 100)
	@XStreamAlias("geoserverUsername")
	String geoserverUsername;
	
	@Column(name = "GEOSERVER_PASSWORD", nullable = false, length = 100)
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
