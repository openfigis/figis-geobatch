package it.geosolutions.ie.dao;
import it.geosolutions.ie.model.Computation;

import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.geotools.data.DataStoreFinder;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.impl.SessionFactoryImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for Computations administration.
 *
 * @author Ariel Nu–ez <ariel.nunez at geo-solutions.it>
 */
@Transactional
@Repository
public class ComputationDao {
	private final static Logger LOGGER = Logger.getLogger(ComputationDao.class);

    private SessionFactoryImpl sessionFactory;
    
    @SuppressWarnings("unchecked")
	public List<Computation> getComputations() {
    	LOGGER.info("Getting all Computations");
    	Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery("FROM Computation");
        return q.list();
    }

    public void persist(Computation c) {
    	LOGGER.info("Persisting Computation");
    	Session session = sessionFactory.getCurrentSession();
        session.persist(c);
    }

    public void update(Computation c) {
    	LOGGER.info("Updating Computation");
    	Session session = sessionFactory.getCurrentSession();
        session.merge(c);
    }

    public void remove(Computation c) {
    	LOGGER.info("Removing Computation");
    	Session session = sessionFactory.getCurrentSession();
        session.delete(c);
    }

	public void setSessionFactory(SessionFactoryImpl sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SessionFactoryImpl getSessionFactory() {
		return sessionFactory;
	}
	
    public void purgeStatus(){
    	LOGGER.info("Purging status");
    	Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery("FROM Computation");
        for (Iterator<Computation> i = q.iterate(); i.hasNext();){
        	Computation c = i.next();
        	if(c.getFinished()==null){
        		LOGGER.warn("Computation "+ c.getId() +" was not finished" );
        		c.setStatus("CRASHED");
        		update(c);
        		LOGGER.info("Computation "+ c.getId() + "marked as crashed");
        	}
        }
    }
    
}
