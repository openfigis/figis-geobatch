package it.geosolutions.geobatch.figis.intersection.dao;

import com.trg.dao.jpa.GenericDAO;

import it.geosolutions.geobatch.figis.intersection.model.Config;
import it.geosolutions.geobatch.figis.intersection.model.DB;
import it.geosolutions.geobatch.figis.intersection.model.Geoserver;

public interface ConfigDao extends GenericDAO<Config, Long>{
	public void setGeoserver(Geoserver geo);
	public void setDB(DB db);
	public void setQuartz();
	public boolean configExist();
	public void updateConfig(Config config);
	public void insertConfig(Config config);
	public Config getConfig();
	// added a comment
}
