package it.geosolutions.geobatch.intersection.test;

import static org.junit.Assert.assertTrue;

import java.util.List;

import it.geosolutions.geobatch.figis.intersection.dao.ConfigDao;
import it.geosolutions.geobatch.figis.intersection.dao.IntersectionDao;
import it.geosolutions.geobatch.figis.intersection.dao.daoImpl.ConfigDaoImpl;
import it.geosolutions.geobatch.figis.intersection.dao.daoImpl.IntersectionDaoImpl;
import it.geosolutions.geobatch.figis.intersection.model.Config;
import it.geosolutions.geobatch.figis.intersection.model.ConfigXStreamMapper;
import it.geosolutions.geobatch.figis.intersection.model.DB;
import it.geosolutions.geobatch.figis.intersection.model.Geoserver;
import it.geosolutions.geobatch.figis.intersection.model.Global;
import it.geosolutions.geobatch.figis.intersection.model.Intersection;
import it.geosolutions.geobatch.figis.intersection.model.Intersection.Status;



import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



public class IntersectionEngineTest {
	//SessionFactory sessionFactory = null;
	ConfigDao configDao = null;
	IntersectionDao intersectionDao = null;
	
	@Before
	public void setUp() throws Exception {
		try {
		System.out.println("start setup");
		  //sessionFactory = HibernateUtil.getSessionFactory();
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

        System.out.println("************************************XML READ"+ctx.containsBean("configDAO"));
        configDao = (ConfigDao) ctx.getBean("configDAO");
       intersectionDao = (IntersectionDao) ctx.getBean("intersectionDAO");
        System.out.println("************************************finish setup test");
		} catch(Throwable e) {
			System.out.println("************** ho finito");
			e.printStackTrace();
		}

	}
	@Test 
	public void testInsertConfigJPA() {
		System.out.println("start test");
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
		  
		  configDao.persist(config);
		  
/*		  List<Config> list = configDao.findAll();
		  System.out.println("size"+list.size());
		  for (Config configStep: list) {
			  System.out.println(""+configStep.getGlobal().getGeoserver().getGeoserverUsername());
		  }  */

		  System.out.println("end test");
	}
	
	@Test
	public void testListAll() {
		  List<Config> list = configDao.findAll();
		  System.out.println("size"+list.size());
		  for (Config configStep: list) {
			  System.out.println(""+configStep.getGlobal().getGeoserver().getGeoserverUsername());
		  }
	}
	
/*
	@Test
	public void testInsertConfig() {
		  
		  Session sess = sessionFactory.getCurrentSession();
		  
		  Transaction tx = sess.beginTransaction();
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

		  sess.save(config);

		  tx.commit();

	}
	
	@Test
	public void testInsertIntersection() {

		  
		  Session sess = sessionFactory.getCurrentSession();
		  Transaction tx = sess.beginTransaction();
		  Intersection int1 = new Intersection();
		  int1.setSrcLayer("SrcLayer");
		  int1.setTrgLayer("TrgLayer");

		  sess.save(int1);
		  tx.commit();
	}
		
	@Test
	public void testUpdateConfig() {

		  Session sess = sessionFactory.getCurrentSession();
		  Transaction tx = sess.beginTransaction();
		  Global global = new Global();
		  global.getGeoserver().setGeoserverUsername("user");
		  global.getGeoserver().setGeoserverPassword("oldPassword");
		  global.getGeoserver().setGeoserverUrl("oldLocalhost");
		  global.getDb().setDatabase("oldDB");
		  global.getDb().setHost("oldLocalhost");
		  global.getDb().setPassword("oldDBPassword");
		  global.getDb().setPort("8080");
		  global.getDb().setSchema("empty");
		  global.getDb().setUser("oldDBUser");
		  Config config = new Config();
		  config.setUpdateVersion(1);
		  config.setGlobal(global);


		  sess.save(config);

		  

		  List<Config> results = (List<Config>)sess.createQuery("from Config e where e.global.geoserver.geoserverUsername='user'").list();
		  Config configTemp = results.get(0);
		  configTemp.getGlobal().getGeoserver().setGeoserverPassword("newPassword");
		  configTemp.getGlobal().getGeoserver().setGeoserverUrl("newLocalHost");
		  configTemp.getGlobal().getDb().setDatabase("newDB");
		  configTemp.getGlobal().getDb().setUser("newDBUser");
		  configTemp.getGlobal().getDb().setPassword("newSBPassword");
		  configTemp.getGlobal().getDb().setHost("newLocalhost");
		  sess.saveOrUpdate(configTemp);
		  
		
		  List<Config> results2 = (List<Config>)sess.createQuery("from Config e where e.global.geoserver.geoserverUsername='user'").list();
		  Config configTemp2 = results2.get(0);
		  String geoserverPassword2 = configTemp2.getGlobal().getGeoserver().getGeoserverPassword();
		  String geoserverURL2 = configTemp2.getGlobal().getGeoserver().getGeoserverUrl();
		  String dbName2 = configTemp2.getGlobal().getDb().getDatabase();
		  String dbUserName2 = configTemp2.getGlobal().getDb().getUser();
		  String dbPassword2 = configTemp2.getGlobal().getDb().getPassword();
		  String dbHost2 = configTemp2.getGlobal().getDb().getHost();
		  

		  assertTrue(!geoserverPassword2.equals("oldPassword"));
		  assertTrue(!geoserverURL2.equals("oldLocalhost"));
		  assertTrue(!dbName2.equals("oldDB"));
		  assertTrue(!dbUserName2.equals("oldDBUser"));
		  assertTrue(!dbPassword2.equals("oldPassword"));
		  assertTrue(!dbHost2.equals("oldLocalhost"));		  

		  
		  tx.commit();
	}

	@Test
	public void testUpdateIntersection() {

		  Session sess = sessionFactory.getCurrentSession();

		  Transaction tx = sess.beginTransaction();
		  Intersection int1 = new Intersection(true, true, true,"srcLayer", "trgLayer", "srcCodeField",
			"trgCodeField", "maskLayer", "areaCRS", Status.TOCOMPUTE);
		  Intersection int2 = new Intersection(true, true, false,"srcLayer2", "trgLayer2", "srcCodeField2",
			"trgCodeField", "maskLayer2", "areaCRS2", Status.COMPUTING);	  


		  sess.save(int1);
		  sess.save(int2);

		  List<Intersection> results = (List<Intersection>)sess.createQuery("from Intersection e").list();
		  
		  if (results!=null) {
		  for (Intersection intStep: results) {
			  System.out.println("srcLayer : "+intStep.getSrcLayer()+", trgLayer : "+intStep.getTrgLayer());
		  	}
		  }
		  System.out.println("stop");
		  assertTrue(results.size()==3);
		  tx.commit();
		  System.out.println("stop2");
		  Session sess2 = sessionFactory.getCurrentSession();
		  Transaction tx2 = sess2.beginTransaction();
		  IntersectionDao intDao = new IntersectionDaoImpl(sessionFactory);
		
		  List<Intersection> results2 = (List<Intersection>)sess.createQuery("from Intersection e where e.trgLayer=''").list();
		  Intersection int3 = new Intersection(false, false, true,null, "trgLayer3", null,
					null, "maskLayer2", null, Status.COMPUTED);

		  
		  sess2.update(int3);

		  results = (List<Intersection>)sess2.createQuery("from Intersection e").list();
		  
		  if (results!=null) {
		  for (Intersection intStep: results) {
			  System.out.println("srcLayer : "+intStep.getSrcLayer()+", trgLayer : "+intStep.getTrgCodeField());
		  	}
		  }		  

		  
		  tx2.commit();
	}*/
	
}
