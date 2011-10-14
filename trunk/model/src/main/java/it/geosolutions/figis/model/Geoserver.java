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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Geoserver other = (Geoserver) obj;
        if ((this.geoserverUrl == null) ? (other.geoserverUrl != null) : !this.geoserverUrl.equals(other.geoserverUrl)) {
            return false;
        }
        if ((this.geoserverUsername == null) ? (other.geoserverUsername != null) : !this.geoserverUsername.equals(other.geoserverUsername)) {
            return false;
        }
        if ((this.geoserverPassword == null) ? (other.geoserverPassword != null) : !this.geoserverPassword.equals(other.geoserverPassword)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

}
