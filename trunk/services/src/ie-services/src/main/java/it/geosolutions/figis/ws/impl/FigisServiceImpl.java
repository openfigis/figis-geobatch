package it.geosolutions.figis.ws.impl;

import com.trg.search.Filter;
import com.trg.search.Search;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.figis.persistence.dao.ConfigDao;
import it.geosolutions.figis.persistence.dao.IntersectionDao;
import it.geosolutions.figis.persistence.dao.daoImpl.IntersectionDaoImpl;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.ws.FigisService;
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.ws.exceptions.ResourceNotFoundDetails;
import it.geosolutions.figis.ws.exceptions.ResourceNotFoundFault;
import it.geosolutions.figis.ws.response.Intersections;
import java.util.List;
import javax.jws.WebService;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


//@WebService(endpointInterface = "it.geosolutions.figis.ws.FigisService")
@WebService(name = "FigisService", serviceName = "FigisServiceService", portName = "FigisServicePort", endpointInterface = "it.geosolutions.figis.ws.FigisService", targetNamespace = "http://services.figis.geosolutions.it/")
public class FigisServiceImpl implements FigisService{

    public FigisServiceImpl() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        this.configDao = (ConfigDao) ctx.getBean("ie-configDAO");
        intersectionDao = (IntersectionDao) ctx.getBean("ie-intersectionDAO");
    }


    ConfigDao configDao =  null;
    IntersectionDao intersectionDao= null;

    public ConfigDao getConfigDao() {
        return configDao;
    }


    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

    public IntersectionDao getIntersectionDao() {
        return intersectionDao;
    }

    public void setIntersectionDao(IntersectionDao intersectionDao) {
        this.intersectionDao = intersectionDao;
    }

    /***************************
     *  Returns the instance of the Config object having id as identifier
     * @param id the id of the Config object to find
     * @return the found Config object
     * @throws ResourceNotFoundFault in case no Config where found
     */
    @Override
    public Config getConfig(Long id) throws ResourceNotFoundFault{
        Config config = configDao.find(id);
        if (config==null) {
            ResourceNotFoundDetails resourceNotFoundDetails =
                    new ResourceNotFoundDetails();
            resourceNotFoundDetails.setId(id);
            resourceNotFoundDetails.setMessage("the id's config does not exist");
            throw new ResourceNotFoundFault(resourceNotFoundDetails);
        }
        return config;
    }

    /********************
     *  lookup for the first returned instance of Config in the DB
     * @return a Config instance if it is present in the DB
     */
/*    @Override
    public Config existConfig()  {
        List<Config> configs = configDao.findAll();
        if (configs!=null && configs.size()>0) return configs.get(0);
        return null;
    } */
    /*************************************
     *  Returns the instance of the Intersection object having id as identifier
     * @param id the id of the Intersection object to find
     * @return the found Intersection object
     * @throws ResourceNotFoundFault in case no Intersection where found
     */
    @Override
    public Intersection getIntersection(Long id) throws ResourceNotFoundFault{
      //  intersectionDao = new IntersectionDaoImpl();
        Intersection intersection = intersectionDao.find(id);
        if (intersection==null) {
            ResourceNotFoundDetails resourceNotFoundDetails =
                    new ResourceNotFoundDetails();
            resourceNotFoundDetails.setId(id);
            resourceNotFoundDetails.setMessage("the id's intersection does not exist");
            throw new ResourceNotFoundFault(resourceNotFoundDetails);
        }
        return intersection;
    }
    /*****************************
     * Returns a list of Intersection instances whose srcLayer is equal to the first parameter and
     * the trgLayer is equal to the second one
     * @param srcLayer the value to compare to the srcLayer attribute
     * @param trgLayer the value to compare to the trgLayer attribute
     * @return the  Intersection instances found
     */
    @Override
    public Intersections getIntersectionsByLayerNames(String srcLayer, String trgLayer)  {
        Search search = new Search();
        search.addFilterAnd(
        Filter.equal("srcLayer", "srcLayer"),
                Filter.equal("trgLayer", "trgLayer"));
        List<Intersection> intersectionList = intersectionDao.search(search);
        Intersections intersections = new Intersections();
        intersections.setIntersections(intersectionList);
        return intersections;
    }

    
    /********************************************
     * Returns all the Intersection instances
     * @return all the Intersection instances
     */
    @Override
    public List<Intersection> getAllIntersections() {
        List<Intersection> intersectionList = intersectionDao.findAll();
        return intersectionList;
    }
    /***********************************
     * update the content of the Config instance identified by id
     * @param id the id to look up
     * @param config the Config instance containing the values to update
     * @return the id of the updated Config instance
     */
    @Override
    public long updateConfig(long id, Config config) {
        Config conf = configDao.find(id);

        // if no config exists yet
        if (conf==null) {
            System.out.println("**************************IL VALORE è nullo");
            insertConfig(config);
            return config.getConfigId();
        }
        System.out.println("************** HO trovato l'elemento");
        // update all the fields

        String userName = config.getGlobal().getGeoserver().getGeoserverUsername();
        if (userName!=null)conf.getGlobal().getGeoserver().setGeoserverUsername(userName);

        String url = config.getGlobal().getGeoserver().getGeoserverUrl();
        if (url!=null ) conf.getGlobal().getGeoserver().setGeoserverUrl(url);

        String geoPassword = config.getGlobal().getGeoserver().getGeoserverPassword();
        if (geoPassword!=null )conf.getGlobal().getGeoserver().setGeoserverPassword(geoPassword);

        String database = config.getGlobal().getDb().getDatabase();
        if (database!=null ) conf.getGlobal().getDb().setDatabase(database);

        String host = config.getGlobal().getDb().getHost();
        if (host!=null ) conf.getGlobal().getDb().setHost(host);

        String dbPassword = config.getGlobal().getDb().getPassword();
        if (dbPassword!=null ) conf.getGlobal().getDb().setPassword(dbPassword);

        String port = config.getGlobal().getDb().getPort();
        if (port!=null ) conf.getGlobal().getDb().setPort(port);

        String schema = config.getGlobal().getDb().getSchema();
        if (schema!=null) conf.getGlobal().getDb().setSchema(schema);

        String dbUser = config.getGlobal().getDb().getUser();
        if (dbUser!=null && !dbUser.equals("")) conf.getGlobal().getDb().setUser(dbUser);

        conf.setUpdateVersion(config.getUpdateVersion());
        // save the updated version of the config
        configDao.save(conf);
        return conf.getConfigId();

    }

    @Override
    public List<Config> getConfigs() {
        return configDao.findAll();
    }



    /***********************************
     * insert a Config instance in the DB
     * @param config the instance to insert
     * @return the id associated to the inserted instance
     */
    @Override
    public long insertConfig(Config config) {
       Config conf = (Config)configDao.save(config);
       return conf.getConfigId();
    }
    /**************************
     * insert a new Intersection in the DB
     * @param intersection the instance to insert
     * @return the id associated to the inserted instance
     */
    @Override
    public long insertIntersection(Intersection intersection) {
        intersectionDao.save(intersection);
        return intersection.getId();
    }
    /****************************
     * update the status value of the Intersection instace identified by id
     * @param id is the identifier of the instance to find
     * @param status the new status to update
     * @return the id of the updated instance
     * @throws ResourceNotFoundFault in case no Intersection where found
     */
    @Override
    public long updateIntersectionByID(long id, Intersection intersection) throws ResourceNotFoundFault {
        Intersection inter = getIntersection(id);

        if (inter==null) {
            ResourceNotFoundDetails resourceNotFoundDetails = 
                    new ResourceNotFoundDetails();
            resourceNotFoundDetails.setId(id);
            resourceNotFoundDetails.setMessage("the id's intersection does not exist");
            throw new ResourceNotFoundFault(resourceNotFoundDetails);
        }
        String areaCRS = intersection.getAreaCRS();
        if (areaCRS!=null) inter.setAreaCRS(areaCRS);
        
        String maskLayer = intersection.getMaskLayer();
        if (maskLayer!=null) inter.setMaskLayer(maskLayer);
        
        String srcCodeField = intersection.getSrcCodeField();
        if (srcCodeField!=null) inter.setSrcCodeField(srcCodeField);
        
        String srcLayer = intersection.getSrcLayer();
        if (srcLayer!=null) inter.setSrcLayer(srcLayer);
        
        String trgCodeField = intersection.getTrgCodeField();
        if (trgCodeField!=null) inter.setTrgCodeField(trgCodeField);
        
        String trgLayer = intersection.getTrgLayer();
        if (trgLayer!=null) inter.setTrgLayer(trgLayer);
        
        Status status = intersection.getStatus();
        if (status!=Status.NOVALUE) intersection.setStatus(status);
        intersectionDao.save(intersection);
        return intersection.getId();
    }



    /*************************
     * Delete a Config instance from the DB
     * @param id is the identifier of the instance to delete
     * @return true if the instance was deleted
     * @throws ResourceNotFoundFault in case no Config where found
     */
    @Override
    public boolean deleteConfig(long id) throws ResourceNotFoundFault {
                Config config = configDao.find(id);

		if (config == null) {
			ResourceNotFoundDetails details = new ResourceNotFoundDetails();
			details.setId(id);
			details.setMessage("Config not found!");
			throw new ResourceNotFoundFault(details);
		}
		return configDao.remove(config);
    }
        /*************************
     * Delete an Intersection instance from the DB
     * @param id is the identifier of the instance to delete
     * @return true if the instance was deleted
     * @throws ResourceNotFoundFault in case no Intersection where found
     */
    @Override
    public boolean deleteIntersection(long id) throws ResourceNotFoundFault {
                Intersection intersection = intersectionDao.find(id);

		if (intersection == null) {
			ResourceNotFoundDetails details = new ResourceNotFoundDetails();
			details.setId(id);
			details.setMessage("Intersection not found!");
			throw new ResourceNotFoundFault(details);
		}


		return intersectionDao.remove(intersection);
    }
    /*******************
     * Delete all the intersections from the DB
     * @return true if all the intersections where deleted, false elsewhere
     * @throws ResourceNotFoundFault
     */
      @Override
    public boolean deleteIntersections()  {
        List<Intersection> intersections = getAllIntersections();
         for (Intersection intersection: intersections) {
             try {
             boolean isDeleted = deleteIntersection(intersection.getId());
             } catch (ResourceNotFoundFault e) {
                 return false;
             }
         }
         return true;
    }



}
