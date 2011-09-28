package it.geosolutions.geobatch.figis.intersection.dao.daoImpl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import com.trg.search.ISearch;

import it.geosolutions.geobatch.figis.intersection.dao.ConfigDao;
import it.geosolutions.geobatch.figis.intersection.model.Config;
import it.geosolutions.geobatch.figis.intersection.model.DB;
import it.geosolutions.geobatch.figis.intersection.model.Geoserver;

public class ConfigDaoImpl extends BaseDAO<Config, Long> implements ConfigDao{

	SessionFactory sessionFactory;

	 

	    @Override
	    public void persist(Config... entities) {
	        super.persist(entities);
	    }

	    @Override
	    public List<Config> findAll() {
	        return super.findAll();
	    }

	    @Override
	    public List<Config> search(ISearch search) {
	        return super.search(search);
	    }


	    @Override
	    public boolean remove(Config entity) {
	        return super.remove(entity);
	    }

	    @Override
	    public boolean removeById(Long id) {
	        return super.removeById(id);
	    }	
	
	
	public ConfigDaoImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		
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
		
	}
	
	

}
