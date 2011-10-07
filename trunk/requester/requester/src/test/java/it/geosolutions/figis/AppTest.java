package it.geosolutions.figis;

import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Global;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import junit.framework.TestCase;
/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{

public void testPutConfig(){
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
      try {
        Request.initConfig();
        Request.insertConfig("http://localhost:8080", config);
    } catch( Exception e) {
        e.printStackTrace();;
    }
}

public void testPutIntersection(){
    Intersection intersection = new Intersection(true, true, true,"srcLayer", "trgLayer", "srcCodeField",
			"trgCodeField", "maskLayer", "areaCRS", Status.TOCOMPUTE);
    try {
        Request.initIntersection();
        Request.insertIntersection("http://localhost:8080", intersection);
    } catch( Exception e) {
        e.printStackTrace();;
    }

    }
/*
 public void testGetAllConfig() throws MalformedURLException{
     try {
     XStream xstream = new XStream(new DomDriver());
        xstream.aliasType("config", Config.class);
        xstream.useAttributeFor(Config.class, "updateVersion");

        xstream.aliasType("global", Global.class);
        xstream.aliasType("geoserver", Geoserver.class);
        xstream.aliasType("db", DB.class);
     String result = HTTPUtils.get("http://localhost:8080/ie-services/config/", null, null);
     System.out.println("RESULT: "+result);

     } catch(Throwable e) {
         e.printStackTrace();
     }
 }

  public void testGetAllIntersections() throws MalformedURLException{
      try {
      XStream xstream = new XStream(new DomDriver());
      xstream.aliasType("Intersections", List.class);
    xstream.aliasType("intersection", Intersection.class);
    xstream.useAttributeFor(Intersection.class, "mask");
    xstream.useAttributeFor(Intersection.class, "force");
    xstream.useAttributeFor(Intersection.class, "preserveTrgGeom");

     String result = HTTPUtils.get("http://localhost:8080/ie-services/intersection", null, null);
     System.out.println("RESULT: "+result);
     List<Intersection> list = (List<Intersection>)xstream.fromXML(result);
          } catch(Throwable e) {
         e.printStackTrace();
     }
 }*/
}
