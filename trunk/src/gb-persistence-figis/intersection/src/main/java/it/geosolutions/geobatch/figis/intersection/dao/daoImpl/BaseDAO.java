package it.geosolutions.geobatch.figis.intersection.dao.daoImpl;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.trg.dao.jpa.GenericDAOImpl;
import com.trg.search.jpa.JPASearchProcessor;

@Repository
public class BaseDAO<T, ID extends Serializable> extends GenericDAOImpl<T, ID> {

    /**
     * EntityManager setting
     * 
     * @param entityManager
     *            the entity manager to set
     */
    @Override
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }

    /**
     * JPASearchProcessor setting
     * 
     * @param searchProcessor
     *            the search processor to set
     */
    @Override
    @Autowired
    public void setSearchProcessor(JPASearchProcessor searchProcessor) {
        super.setSearchProcessor(searchProcessor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.trg.dao.jpa.JPABaseDAO#em()
     */
    @Override
    public EntityManager em() {
        // TODO Auto-generated method stub
        return super.em();
    }
}

