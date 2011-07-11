package it.geosolutions.ie.dao;

import it.geosolutions.ie.model.Combination;
import it.geosolutions.ie.model.Download;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.impl.SessionFactoryImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for combinations administration.
 *
 * @author Ariel Nu–ez <ariel.nunez at geo-solutions.it>
 */
@Transactional
@Repository
public class CombinationDao {
	private final static Logger LOGGER = Logger.getLogger(CombinationDao.class);

    private SessionFactoryImpl sessionFactory;
    
    @SuppressWarnings("unchecked")
	public List<Combination> getCombinations() {
    	LOGGER.info("Getting all combinations");
    	Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery("FROM Combination");
        return q.list();
    }   
    @SuppressWarnings("unchecked")
	public List<Combination> getEnabledCombinations() {
    	LOGGER.info("Getting enabled combinations");
    	Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery("FROM Combination c WHERE c.status = :var");
        q.setString("var", "ENABLED");
        return q.list();
    }
    
    @SuppressWarnings("unchecked")
	public List<Download> getPopulatedCombinations() {
    	LOGGER.info("Getting populated combinations");
    	Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery("FROM Download c WHERE c.empty = :var2");
        q.setString("var2", "0");
        return q.list();
    }

    public void persist(Combination c) {
    	LOGGER.info("Persisting combination");
    	Session session = sessionFactory.getCurrentSession();
        session.persist(c);
    }

    public void persistDownload(Combination c){
        LOGGER.info("Persisting download");
        Session session = sessionFactory.getCurrentSession();
        Download d = new Download();
        d.populate(c);
        session.persist(d);
    }
    
    public void update(Combination c) {
    	LOGGER.info("Updating combination");
    	Session session = sessionFactory.getCurrentSession();
    	Download d = new Download();
    	d.populate(c);
        session.persist(d);
    }
    
    public void clearDownloads() {
        LOGGER.info("Clear Downloads");
        Session session = sessionFactory.getCurrentSession();
        List<Download> downloads =  getPopulatedCombinations();
        for(Download d: downloads){
            session.delete(d);
        }
    }

    public void remove(Combination c) {
    	LOGGER.info("Removing combination");
    	Session session = sessionFactory.getCurrentSession();
        session.delete(c);
    }
    public void enable(Long id, boolean enable_value){
    	Session session = sessionFactory.getCurrentSession();
    	Combination c = (Combination) session.load(Combination.class, id);
        if(enable_value) c.enable();
        else c.disable();
        session.update(c);
    }
    public void remove(Long id){
    	remove(getById(id));
    }
    
	public void setSessionFactory(SessionFactoryImpl sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
    public Combination getById(Long id){
     	LOGGER.info("Getting a combination");
    	Session session = sessionFactory.getCurrentSession();   
    	return (Combination) session.load(Combination.class, id);
    }
	public SessionFactoryImpl getSessionFactory() {
		return sessionFactory;
	}
    
}
