
package it.geosolutions.figis;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Configs;
import it.geosolutions.figis.model.DB;
import it.geosolutions.figis.model.Geoserver;
import it.geosolutions.figis.model.Global;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import java.util.List;
/**************************
 * @description this class allows to query the REST interface of the it.geosloutions.figis.model classes
 ************************/
/**
 *
 * @author Luca
 */
public class Request {
    static XStream xStreamConfig = null;
    static XStream xStreamIntersection = null;
/***********************
 * initialize the XSTREAM parser for the XML representation of it.geosolutions.figis.model.Config object
 */
    public static void initConfig() {
        xStreamConfig = new XStream(new DomDriver());
        xStreamConfig.aliasType("configs", List.class);        
        xStreamConfig.aliasType("config", Config.class);

        xStreamConfig.aliasType("global", Global.class);
        xStreamConfig.aliasType("geoserver", Geoserver.class);
        xStreamConfig.aliasType("db", DB.class);
    }
/***********************
 * initialize the XSTREAM parser for the XML representation of it.geosolutions.figis.model.Intersection object
 */
    public static void initIntersection() {
        xStreamIntersection = new XStream(new DomDriver());
        xStreamIntersection.aliasType("intersections", List.class);
        xStreamIntersection.aliasType("intersection", Intersection.class);
/*        xStreamIntersection.useAttributeFor(boolean.class, "mask");
        xStreamIntersection.useAttributeFor(boolean.class, "force");
        xStreamIntersection.useAttributeFor(boolean.class, "preserveTrgGeom");*/
    }

    /***************
     * Return all the Config instances
     * @param host the host name where address request
     * @return a list containing the instances
     * @throws java.net.MalformedURLException  in case the URL is not valid
     */
    public static  List<Config> getConfigs(String host) throws java.net.MalformedURLException{
        String result = HTTPUtils.get(host+"/ie-services/config/", null, null);
        System.out.println("RESULT GETCONFIG"+result);
        if (result==null) return null;
        System.out.println("Sono qui");
        List<Config> configs = (List<Config>)xStreamConfig.fromXML(result); 
        System.out.println("LA SIZE E'"+configs.size());
        return configs;
    }
   /************************
    * Check if a Config object exists in the DB
    * @param host the host name where address request
    * @return a reference to the found Config object
    * @throws java.net.MalformedURLException in case the URL is not valid
    */
    public static Config existConfig(String host) throws java.net.MalformedURLException{
         List<Config> configs = getConfigs(host);
        if (configs==null || configs.size()==0) return null;
        return configs.get(0);
    }
    
    /********************************
     * Update the data of the Config instance
     * @param host the host name where address request
     * @param id the id of the instance to update
     * @param config the instance containing changes
     * @return the id of the changed instance
     */
    public static long updateConfig(String host, long id, Config config) {
        String xml = xStreamConfig.toXML(config);
        System.out.println("XML : "+xml);
        String result = HTTPUtils.put(host+"/ie-services/config/"+id, xml, "text/xml", null, null);
        System.out.println("RESULT PUT"+result);
        Long value = Long.parseLong(result);
        return value;
    }
    /***************
     * Returns the Config instance identified by id
     * @param host the host name where address request
     * @param id the id of the Config instance to look up
     * @return the Config instance found
     * @throws java.net.MalformedURLException in case the URL is not valid
     */
    public static Config getConfigByID(String host, Long id) throws java.net.MalformedURLException{

        String result = HTTPUtils.get(host+"/ie-services/config/"+id, null, null);
        System.out.println("RESULT PUT"+result);
        Config config = (Config)xStreamConfig.fromXML(result);
        return config;
    }
    /********************
     * Insert a new Config instance into the DB
     * @param host the host name where address request
     * @param config the instance to insert in the DB
     * @return the id of the instance into the DB
     * @throws java.net.MalformedURLException in case the URL is not valid
     */
    public static long insertConfig(String host, Config config) throws java.net.MalformedURLException {
        String xml = xStreamConfig.toXML(config);
        System.out.println("XML INSERT CONFIG: "+xml);
        String result = HTTPUtils.post(host+"/ie-services/config", xml, "text/xml", null, null);
        System.out.println("RESULT POST"+result);
        if (result==null) return -1;
        Long value = Long.parseLong(result);
        return value;
    }
    /*******************
     * Delete the Config instance identified by id
     * @param host the host name where address request
     * @param id the id of the instance to delete from the DB
     * @return true if the request has success
     * @throws java.net.MalformedURLException in case the URL is not valid
     */
    public static boolean deleteConfig(String host, long id) throws java.net.MalformedURLException {
        boolean result = HTTPUtils.delete(host+"/ie-services/config/"+id, null, null);
        System.out.println("RESULT DELETE"+result);
        return result;
    }
    /**********************
     * Insert a new Intersection instance into the DB
     * @param host the host name where address request
     * @param intersection the Intersection instance to insert into the DB
     * @return the id of the instance into the DB
     * @throws java.net.MalformedURLException in case the URL is not valid
     */
   public static long insertIntersection(String host, Intersection intersection) throws java.net.MalformedURLException {
        String xml = xStreamIntersection.toXML(intersection);
        System.out.println("XML : "+xml);
        String result = HTTPUtils.post(host+"/ie-services/intersection", xml, "text/xml", null, null);
        System.out.println("RESULT POST INSERT INTERSECTION"+result);
        if (result!=null) {
            Long value = Long.parseLong(result);
            return value;
        }
        return -1;
    }
    /***********************
     * Returns all the Intersection instance from the DB
     * @param host the host name where address request
     * @return a list of all Intersection instances
     * @throws java.net.MalformedURLException in case the URL is not valid
     */
    public static List<Intersection> getAllIntersections(String host) throws java.net.MalformedURLException{
        String result = HTTPUtils.get(host+"/ie-services/intersection", null, null);
        System.out.println("RESULT GET ALL INTERSECTIONS: "+result);
        if (result!=null) return (List<Intersection>)xStreamIntersection.fromXML(result);
        return null;
    }

    /**************************
     * Delete all the Intersection instance from the DB
     * @param host the host name where address request
     * @return true if all the Intersection instances where deleted from the DB
     * @throws java.net.MalformedURLException
     */
    public static boolean deleteAllIntersections(String host) throws java.net.MalformedURLException{
        boolean result = HTTPUtils.delete(host+"/ie-services/intersection/", null, null);
        System.out.println("RESULT DELETE ALL INTERSECTIONS"+result);
        return result;
    }
    /***************
     * Delete the Intersection instance identified by id
     * @param host the host name where address request
     * @param id the id of the Intersection instance to delete
     * @return true in case of success
     */
    public static boolean deleteIntersectionById(String host, long id) {
        boolean result = HTTPUtils.delete(host+"/ie-services/intersection/"+id, null, null);
        System.out.println("RESULT PUT"+result);
        return result;
    }
    /*******************
     * Update the status of the Intersection instance identified by id
     * @param id the identifier
     * @param status the new status
     * @return the identifier of the returned instance
     */
    public static long updateIntersectionById(String host, long id, Intersection intersection){
        String xml = xStreamIntersection.toXML(intersection);
        System.out.println("XML : "+xml);
        String result = HTTPUtils.put(host+"/ie-services/intersection/"+id, xml, "text/xml", null, null);
        System.out.println("RESULT PUT"+result);
        if (result!=null) {
            Long value = Long.parseLong(result);
            return value;
        }
        return -1;
    }
}
