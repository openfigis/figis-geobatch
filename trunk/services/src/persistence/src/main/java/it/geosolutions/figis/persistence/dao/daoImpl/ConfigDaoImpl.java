package it.geosolutions.figis.persistence.dao.daoImpl;

import it.geosolutions.figis.persistence.dao.ConfigDao;
import it.geosolutions.figis.persistence.model.Config;
import it.geosolutions.figis.persistence.model.DB;
import it.geosolutions.figis.persistence.model.Geoserver;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import com.trg.search.ISearch;


@Transactional
public class ConfigDaoImpl extends BaseDAO<Config, Long> implements ConfigDao{

	
	 
	public ConfigDaoImpl() {
		super();
	}
	


	@Override
	public Config save(Config entity) {
		// TODO Auto-generated method stub
		return super.save(entity);
	}

    @Override
    public boolean remove(Config entity) {
        return super.remove(entity);
    }



	@Transactional
	public void setGeoserver(Geoserver geo) {
		// TODO Auto-generated method stub
		
	}
	
	@Transactional
	public void setDB(DB db) {
		// TODO Auto-generated method stub
		
	}

	@Transactional
	public void setQuartz() {
		// TODO Auto-generated method stub
		
	}

	@Transactional
	public boolean configExist() {
		// TODO Auto-generated method stub
		return false;
	}



	public void updateConfig(Config config) {
		// TODO Auto-generated method stub
		
	}



	public void insertConfig(Config config) {
		// TODO Auto-generated method stub
		
	}



	public Config getConfig() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
/*
	@Transactional
	public Config getConfig() {
		Session session = sessionFactory.openSession();
		Criteria firstCriteria = session.createCriteria(Config.class);
		List list = firstCriteria.list();
		if (list!= null && list.size() > 0) {
			Config configDB = (Config)firstCriteria.list().get(0);
			return configDB;
		}
		return null;
	}

	@Transactional
	public void updateConfig(Config config) {
		Session session = sessionFactory.openSession();
		Criteria firstCriteria = session.createCriteria(Config.class);
		List list = firstCriteria.list();
		if (list!= null && list.size() > 0) {
			Config configDB = (Config)firstCriteria.list().get(0);
			session.delete(configDB);
		}
		session.save(config);
		
	}

	@Transactional
	public void insertConfig(Config config) {
		Session session = sessionFactory.openSession();
		session.save(config);
		
	}*/
	
	

}
