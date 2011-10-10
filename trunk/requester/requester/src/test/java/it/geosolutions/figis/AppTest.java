package it.geosolutions.figis;

import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Global;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import java.net.MalformedURLException;
import java.util.List;
import junit.framework.TestCase;
/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    Config config = new Config();
    String host = "http://localhost:8080";
    
    @Override
    protected void setUp()  {
        
       try {
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
      config.setUpdateVersion(1);
      config.setGlobal(global);
       }catch(Throwable e)
       {
           e.printStackTrace();
       }
    }
/*
public void testDeleteConfig() throws java.net.MalformedURLException{
    System.out.println("Start testDeleteConfig");
    Request.initConfig();
    long id1 = Request.insertConfig(host, config);
    long id2 = Request.insertConfig(host, config);
    List<Config> list = Request.getConfigs(host);
    for (Config conf : list) {
        boolean resultDelete = Request.deleteConfig(host, conf.getConfigId());
        assertTrue(resultDelete);
    }
}    
    
public void testExistConfigBeforeAndAfter() throws java.net.MalformedURLException {
        System.out.println("Start testExistConfigBeforeAndAfter");
        Request.initConfig();
        Config confBefore = Request.existConfig(host);
        assertTrue(confBefore==null);
        long id = Request.insertConfig(host, config);
        Config confAfter = Request.existConfig(host);
        assertTrue(confAfter!=null);        
}    
public void testUpdateConfig() throws java.net.MalformedURLException {
    System.out.println("testUpdateConfig");
    Config upConfig=new Config();
    // init updating fields
    String newGSpassword = "newadmin";
    String newGSusername = "newadmin";
    String newGSURL = "newlocalhost";
    String newdatabase = "newwtrial";
    String newHost = "newlocalhost";
    String newSBpassword  = "newdbpassword";
    String newPort = "9090";
    String newSchema = "newEmpty";
    String newDBuser = "newdbuser";
    
    // new update object
      Global global = new Global();
      global.getGeoserver().setGeoserverUsername(newGSusername);
      global.getGeoserver().setGeoserverPassword(newGSpassword);
      global.getGeoserver().setGeoserverUrl(newGSURL);
      global.getDb().setDatabase(newdatabase);
      global.getDb().setHost(newHost);
      global.getDb().setPassword(newSBpassword);
      global.getDb().setPort(newPort);
      global.getDb().setSchema(newSchema);
      upConfig.setUpdateVersion(2);
      upConfig.setGlobal(global);
      
      Config confBeforeUpdate = Request.existConfig(host);
      assertTrue(confBeforeUpdate!=null);
      long updateID = Request.updateConfig(host, confBeforeUpdate.getConfigId(), upConfig);
      Config confAfterUpdate = Request.getConfigByID(host, updateID);
      assertTrue(confAfterUpdate.getGlobal().getGeoserver().getGeoserverUsername().equals(newGSusername));
      assertTrue(confAfterUpdate.getGlobal().getGeoserver().getGeoserverPassword().equals(newGSpassword));
      assertTrue(confAfterUpdate.getGlobal().getGeoserver().getGeoserverUrl().equals(newGSURL));
      assertTrue(confAfterUpdate.getGlobal().getDb().getDatabase().equals(newdatabase));
      assertTrue(confAfterUpdate.getGlobal().getDb().getUser().equals("dbuser"));
}
*/

  public void testListAndDeleteIntersections() throws MalformedURLException{
      try {
          Request.initIntersection();
      Intersection int1 = new Intersection(true, true, true,"srcLayer", "trgLayer", "srcCodeField",
            "trgCodeField", "maskLayer", "areaCRS", Status.TOCOMPUTE);
      Request.insertIntersection(host, int1);
          System.out.println("AFTER INTERSECTION");
      List<Intersection> list = Request.getAllIntersections(host);
          System.out.println("SIZE OF "+list.size());
      for (Intersection intersection: list) {
          boolean value = Request.deleteIntersectionById(host, intersection.getId());
          assertTrue(value);
      }
      } catch(Throwable e) {
          e.printStackTrace();
      }

  }
    
  public void testInsertAndGetAllIntersections() throws MalformedURLException{
      Intersection int1 = new Intersection(true, true, true,"srcLayer", "trgLayer", "srcCodeField",
            "trgCodeField", "maskLayer", "areaCRS", Status.TOCOMPUTE);
      Intersection int2 = new Intersection(true, true, false,"srcLayer2", "trgLayer2", "srcCodeField2",
            "trgCodeField", "maskLayer2", "areaCRS2", Status.COMPUTING);
      assertTrue(Request.insertIntersection(host, int1)!=0);
      assertTrue(Request.insertIntersection(host, int2)!=0);
      List<Intersection> list = Request.getAllIntersections(host);
      assertTrue(list.size()==2);
     }
  
  
  public void testDeleteAllandUpdate() throws MalformedURLException{
      try {
      assertTrue(Request.deleteAllIntersections(host));
      Intersection int1 = new Intersection(true, true, true,"srcLayer", "trgLayer", "srcCodeField",
            "trgCodeField", "maskLayer", "areaCRS", Status.TOCOMPUTE);
      long id = Request.insertIntersection(host, int1);
      Intersection int2 = new Intersection(false, true, true,"srcLayer1", "trgLayer2", "srcCodeField",
            "trgCodeField", "maskLayer", "areaCRS", Status.COMPUTED);     
      Request.updateIntersectionById(host, id, int2);
      } catch(Throwable e){
          e.printStackTrace();
      }
  }
 
}
