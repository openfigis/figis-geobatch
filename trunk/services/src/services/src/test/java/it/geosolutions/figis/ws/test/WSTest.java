package it.geosolutions.figis.ws.test;

/**
 *
 * @author Luca
 */

import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Global;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.figis.ws.FigisService;
import it.geosolutions.figis.ws.exceptions.ResourceNotFoundFault;
import it.geosolutions.figis.ws.impl.FigisServiceImpl;
import java.io.IOException;
import java.util.ArrayList;
import junit.framework.TestCase;
import org.apache.log4j.Logger;





public class WSTest extends TestCase{

	final static Logger LOGGER = Logger.getLogger(WSTest.class.toString());
        FigisService figisService = null;

	public void setUp() throws Exception {

            figisService = new FigisServiceImpl();

	}

	public void testInsertConfig() throws IOException {

                System.out.println("START TEST");
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
              assertTrue(id>=0);
  	}


	public void testInsertIntersection() {
		  Intersection int1 = new Intersection(true, true, true,"srcLayer", "trgLayer", "srcCodeField",
			"trgCodeField", "maskLayer", "areaCRS", Status.TOCOMPUTE);
		  Intersection int2 = new Intersection(true, true, false,"srcLayer2", "trgLayer2", "srcCodeField2",
			"trgCodeField", "maskLayer2", "areaCRS2", Status.COMPUTING);
                  assertTrue(figisService.insertIntersection(int1)>=0);
                  assertTrue(figisService.insertIntersection(int2)>=0);

	}

	public void testInsertAndDeleteIntersection() throws ResourceNotFoundFault{
		  Intersection int1 = new Intersection(true, true, true,"srcLayer1", "trgLayer1", "srcCodeField1",
			"trgCodeField1", "maskLayer1", "areaCRS1", Status.TOCOMPUTE);
                  long id = figisService.insertIntersection(int1);
                  assertTrue(id>=0);
                  assertTrue(figisService.deleteIntersection(id));
	}
}
