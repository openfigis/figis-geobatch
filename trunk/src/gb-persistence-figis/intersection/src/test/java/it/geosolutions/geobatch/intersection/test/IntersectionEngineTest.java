package it.geosolutions.geobatch.intersection.test;

import it.geosolutions.geobatch.figis.intersection.model.Config;
import it.geosolutions.geobatch.figis.intersection.model.ConfigXStreamMapper;
import it.geosolutions.geobatch.figis.intersection.model.DB;
import it.geosolutions.geobatch.figis.intersection.model.Geoserver;
import it.geosolutions.geobatch.figis.intersection.model.Global;
import it.geosolutions.geobatch.figis.intersection.model.Intersection;



import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;



public class IntersectionEngineTest {
	SessionFactory sessionFactory = null;
	
	@Before
	public void setUp() throws Exception {
		  sessionFactory = HibernateUtil.getSessionFactory();
	}

	@Test
	public void testInsertConfig() {

		  Session sess = sessionFactory.getCurrentSession();
		  /** Starting the Transaction */
		  Transaction tx = sess.beginTransaction();
		  /** Creating Pojo */
		  Global global = new Global();
		  global.getGeoserver().setGeoserverUsername("admin");
		  global.getGeoserver().setGeoserverPassword("password");
		  global.getGeoserver().setGeoserverUrl("localhost");
		  global.getDb().setDatabase("trial");
		  global.getDb().setHost("localhost");
		  global.getDb().setPassword("password");
		  global.getDb().setPort("8080");
		  global.getDb().setSchema("empty");
		  global.getDb().setUser("dbuser");
		  Config config = new Config();
		  config.setUpdateVersion(1);
		  config.setGlobal(global);

		  /** Saving POJO */
		  sess.save(config);
		  /** Commiting the changes */
		  tx.commit();

	}
	
	@Test
	public void testInsertIntersection() {
		  Session sess = sessionFactory.getCurrentSession();
		  /** Starting the Transaction */
		  Transaction tx = sess.beginTransaction();	
		  Intersection int1 = new Intersection();
		  int1.setSrcLayer("SrcLayer");
		  int1.setTrgLayer("TrgLayer");
		  sess.save(int1);
		  /** Commiting the changes */
		  tx.commit();
	}

}
