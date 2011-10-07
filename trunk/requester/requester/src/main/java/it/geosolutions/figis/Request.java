
package it.geosolutions.figis;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import it.geosolutions.figis.model.Config;
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
        xStreamConfig.aliasType("config", Config.class);
        xStreamConfig.useAttributeFor(Config.class, "updateVersion");

        xStreamConfig.aliasType("global", Global.class);
        xStreamConfig.aliasType("geoserver", Geoserver.class);
        xStreamConfig.aliasType("db", DB.class);
    }
/***********************
 * initialize the XSTREAM parser for the XML representation of it.geosolutions.figis.model.Intersection object
 */
    public static void initIntersection() {
        xStreamIntersection = new XStream(new DomDriver());
        xStreamIntersection.aliasType("Intersections", List.class);
        xStreamIntersection.aliasType("intersection", Intersection.class);
        xStreamIntersection.useAttributeFor(Intersection.class, "mask");
        xStreamIntersection.useAttributeFor(Intersection.class, "force");
        xStreamIntersection.useAttributeFor(Intersection.class, "preserveTrgGeom");
    }

    public static Config getConfig(){
        return null;
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
        System.out.println("XML : "+xml);
        String result = HTTPUtils.put(host+"/ie-services/config", xml, "text/xml", null, null);

        System.out.println("RESULT PUT"+result);
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
        System.out.println("RESULT PUT"+result);
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
        String result = HTTPUtils.put(host+"/ie-services/intersection", xml, "text/xml", null, null);
        System.out.println("RESULT PUT"+result);
        Long value = Long.parseLong(result);
        return value;
    }
    /***********************
     * Returns all the Intersection instance from the DB
     * @param host the host name where address request
     * @return a list of all Intersection instances
     * @throws java.net.MalformedURLException in case the URL is not valid
     */
    public static List<Intersection> getAllIntersections(String host) throws java.net.MalformedURLException{
        String result = HTTPUtils.get(host+"/ie-services/intersection", null, null);
        System.out.println("RESULT: "+result);
        return (List<Intersection>)xStreamIntersection.fromXML(result);
    }
    public static List<Intersection> getIntersectionsByLayerNames(String srcLayer, String trgLayer) {
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
        System.out.println("RESULT PUT"+result);
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
    public static long updateIntersectionStatusById(long id, Status status){
        return 0;
    }
}
