

package it.geosolutions.figis.ws.test;

/**
 *
 * @author Luca
 */




import java.util.List;


import it.geosolutions.figis.persistence.model.Config;
import it.geosolutions.figis.persistence.model.Global;
import it.geosolutions.figis.persistence.model.Intersection;
import it.geosolutions.figis.persistence.model.Intersection.Status;
import it.geosolutions.figis.ws.FigisService;
import it.geosolutions.figis.ws.exceptions.ResourceNotFoundFault;
import it.geosolutions.figis.ws.impl.FigisServiceImpl;
import it.geosolutions.figis.ws.response.Intersections;
import java.io.IOException;



import junit.framework.TestCase;


import org.apache.log4j.Logger;





public class WSTest extends TestCase{

	final static Logger LOGGER = Logger.getLogger(WSTest.class.toString());
        FigisService figisService = null;


	public void setUp() throws Exception {
            figisService = new FigisServiceImpl();
	}

	public void testInsertConfig() throws IOException {
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
              long id = figisService.insertConfig(config);
              assertTrue(id!=0);
	}


	public void testInsertIntersection() {
		  Intersection int1 = new Intersection(true, true, true,"srcLayer", "trgLayer", "srcCodeField",
			"trgCodeField", "maskLayer", "areaCRS", Status.TOCOMPUTE);
		  Intersection int2 = new Intersection(true, true, false,"srcLayer2", "trgLayer2", "srcCodeField2",
			"trgCodeField", "maskLayer2", "areaCRS2", Status.COMPUTING);
                  assertTrue(figisService.insertIntersection(int1)!=0);
                  assertTrue(figisService.insertIntersection(int2)!=0);

	}


	public void testInsertAndDeleteIntersection() throws ResourceNotFoundFault{
		  Intersection int1 = new Intersection(true, true, true,"srcLayer1", "trgLayer1", "srcCodeField1",
			"trgCodeField1", "maskLayer1", "areaCRS1", Status.TOCOMPUTE);
                  long id = figisService.insertIntersection(int1);
                  assertTrue(id!=0);
                  assertTrue(figisService.deleteIntersection(id));

	}

	public void testListAll() {
		  List<Config> listConfig = figisService.getAllConfigs();
                  assertTrue(listConfig.size()==2);
		  Intersections intersections = figisService.getAllIntersections();
                  assertTrue(intersections.getIntersections().size()==2);
 
	}





/*
	@Test
	public void testUpdateConfig() {

		  LOGGER.info("TEST UPDATE O CONFIG");
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


		  configDao.save(config);


		  Config configToUpdate = configDao.find(config.getConfigId());



		  configToUpdate.getGlobal().getGeoserver().setGeoserverPassword("newPassword");
		  configToUpdate.getGlobal().getGeoserver().setGeoserverUrl("newLocalHost");
		  configToUpdate.getGlobal().getDb().setDatabase("newDB");
		  configToUpdate.getGlobal().getDb().setUser("newDBUser");
		  configToUpdate.getGlobal().getDb().setPassword("newSBPassword");
		  configToUpdate.getGlobal().getDb().setHost("newLocalhost");
		  configDao.save(configToUpdate);

		  Config configToCheck = configDao.find(config.getConfigId());

		  String geoserverPassword2 = configToCheck.getGlobal().getGeoserver().getGeoserverPassword();
		  String geoserverURL2 = configToCheck.getGlobal().getGeoserver().getGeoserverUrl();
		  String dbName2 = configToCheck.getGlobal().getDb().getDatabase();
		  String dbUserName2 = configToCheck.getGlobal().getDb().getUser();
		  String dbPassword2 = configToCheck.getGlobal().getDb().getPassword();
		  String dbHost2 = configToCheck.getGlobal().getDb().getHost();


		  assertTrue(!geoserverPassword2.equals("oldPassword"));
		  assertTrue(!geoserverURL2.equals("oldLocalhost"));
		  assertTrue(!dbName2.equals("oldDB"));
		  assertTrue(!dbUserName2.equals("oldDBUser"));
		  assertTrue(!dbPassword2.equals("oldPassword"));
		  assertTrue(!dbHost2.equals("oldLocalhost"));


	}*/
/*
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
