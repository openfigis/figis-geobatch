package it.geosolutions.figis.persistence.dao.daoImpl;
/*****************
 * The implementation of the ConfigDao interface
 */
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.persistence.dao.ConfigDao;
import org.springframework.transaction.annotation.Transactional;



@Transactional
public class ConfigDaoImpl extends BaseDAO<Config, Long> implements ConfigDao{
	 
	public ConfigDaoImpl() {
		super();
	}
	

        /***********
         * Save a new Config instance into the DB
         * @param entity the new Config instance
         * @return the saved Config instance with the assigned identifier
         */
	@Override
	public Config save(Config entity) {
		// TODO Auto-generated method stub
		return super.save(entity);
	}
        /************************
         * Delete a Config instance from the DB
         * @param entity the instance to delete
         * @return true if deletion has success, false otheriwse
         */
        @Override
        public boolean remove(Config entity) {
            return super.remove(entity);
        }



}
