package it.geosolutions.figis.model;


import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("global")
public class Global {
	

	@XStreamAlias("geoserver")
	Geoserver geoserver;
	

	@XStreamAlias("db")
	DB db;

        @XStreamAlias("clean")
        private boolean clean;

	public Global() {
		super();
		this.geoserver = new Geoserver();
		this.db = new DB();
	}

        public boolean isClean() {
            return clean;
        }

        public void setClean(boolean clean) {
                this.clean = clean;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Global other = (Global) obj;
        if (this.geoserver != other.geoserver && (this.geoserver == null || !this.geoserver.equals(other.geoserver))) {
            return false;
        }
        if (this.db != other.db && (this.db == null || !this.db.equals(other.db))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
	

}
