package it.geosolutions.figis.persistence.dao.daoImpl;
/*****************
 * The implementation of the IntersectionDao interface
 */

import org.springframework.transaction.annotation.Transactional;
import it.geosolutions.figis.persistence.dao.IntersectionDao;
import it.geosolutions.figis.model.Intersection;

@Transactional
public class IntersectionDaoImpl extends BaseDAO<Intersection, Long> implements IntersectionDao{

     public IntersectionDaoImpl() {
		
     }
	
    /***********
     * Save a new Intersection instance into the DB
     * @param entity the new Intersection instance
     * @return the saved Intersection instance with the assigned identifier
     */
    @Override
    public Intersection save(Intersection entity) {
            // TODO Auto-generated method stub
            return super.save(entity);
    }

    /************************
     * Delete an Intersection instance from the DB
     * @param entity the instance to delete
     * @return true if deletion has success, false otheriwse
     */
    @Override
    public boolean remove(Intersection entity) {
        return super.remove(entity);
    }


}
