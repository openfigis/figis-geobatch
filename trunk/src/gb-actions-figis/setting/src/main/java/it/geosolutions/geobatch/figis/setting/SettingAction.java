/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geobatch.figis.setting;

import it.geosolutions.figis.Request;
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.ConfigXStreamMapper;
import it.geosolutions.figis.model.Intersection;

import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.net.MalformedURLException;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.converters.ConversionException;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class SettingAction extends BaseAction<EventObject> {
    private final static Logger LOGGER = LoggerFactory.getLogger(SettingAction.class);
    private String defaultMaskLayer = null;
    private String host = null;

    /**
     * configuration
     */
    private final SettingConfiguration conf;
    
    public SettingAction(SettingConfiguration configuration) {
        super(configuration);
        conf = configuration;
        //TODO initialize your members here
    }

   /*********
    * SET THE CONFIG IF IT NOT EXIST OR UPDATE IT IF THE XMLConfig IS MORE RECENT OF THE CURRENT STATUS 
    * @param host  the host where to address requests
    * @param XMLConfig the new configuration
    * @return a Config object representing the current status of the configuration
    */
    public Config saveOrUpdateConfig(String host, Config XMLConfig){
        Config config=null;
        Request.initConfig();
		try {
			config = Request.existConfig(host); // check if a configuration currently exist in the database
	        if (config==null) { // check if the DB config is empty
	        	long id = Request.insertConfig(host, XMLConfig); // insert the new configuration
	        	return Request.getConfigByID(host, id); // return the current configuration
	        }		
	        else { // else
	        	if (XMLConfig.getUpdateVersion()>= config.getUpdateVersion()){ // check if the current version is less than the XML config version
		        	long id  = Request.updateConfig(host, config.getConfigId(), XMLConfig); // update the configuration with new information
		        	return Request.getConfigByID(host, id); // return the current configuration
	        	} // return a null value to indicate that the new config is not valid because the version
	        	else return null;
	        }
		} catch (MalformedURLException e) {
			// return a null value because an exception related to the host name
			return null;
		}

    }
    
    /***********
     * Remove the intersection in the DB which are not listed in the intersections list
     * @param host the host where address requests
     * @param intersections the intersections to retain
     * @param dbList the list of the intersection into the db 
     * @throws MalformedURLException in case a wrong address
     */
    public void removeUnlistedIntersectionsFromDB(String host, List<Intersection> intersections, List<Intersection> dbList) throws MalformedURLException{
    
    	for (Intersection intersection: dbList) {
    		if (!(intersections.contains(intersection))){
    			LOGGER.info(intersection.toString());
    			LOGGER.info("will be set to be deleted");
    			intersection.setStatus(Status.TODELETE);
    			Request.updateIntersectionById(host, intersection.getId(), intersection);
    		}
    	}
    }
    /**********
     * this method checks whether two intersections are different considering only CRS, SrcCodeField, TrgCodeField, MaskLayer, PreserveTrgGeom or isMask 
     * the srcLayer and the trgLayer are not compared
     * @param xmlIntersection
     * @param dbIntersection
     * @return true if one of the parameters is different, false in the other case
     */
    private boolean areIntersectionParameterDifferent(Intersection xmlIntersection, Intersection dbIntersection) {
    	if (!(xmlIntersection.getAreaCRS().equals(dbIntersection.getAreaCRS()))) return true;
    	if (!(xmlIntersection.getSrcCodeField().equals(dbIntersection.getSrcCodeField()))) return true;
    	if (!(xmlIntersection.getTrgCodeField().equals(dbIntersection.getTrgCodeField()))) return true;
    	if (!(xmlIntersection.getMaskLayer().equals(dbIntersection.getMaskLayer()))) return true;
    	if (!(xmlIntersection.isMask() == dbIntersection.isMask())) return true;
    	if (!(xmlIntersection.isPreserveTrgGeom()== dbIntersection.isPreserveTrgGeom())) return true;
    	return false;
    }
    
    /***************
     * update the dbList on the basis of the xmllist configuration
     * new intersections are added
     * present intersections are updated
     * @param host the host where address requests
     * @param XMLList the new XML intersection list
     * @param dbList the current status of the intersection in the db
     * @param isClean 
     * @throws MalformedURLException
     */
 
    public void updateIntersectionsOnDB(String host, List<Intersection> XMLList, List<Intersection> dbList, boolean isClean, String defaultMaskLayer) throws MalformedURLException{
    	
    	for (Intersection xmlIntersection: XMLList) {
    		// look for the XML intersection in the DB
    		int dbIntersectionID = dbList.indexOf(xmlIntersection);
    		if (dbIntersectionID>=0) { //if xmlIntersection exists in the DB
    			Intersection dbIntersection = dbList.get(dbIntersectionID); 
    			if (dbIntersection.getStatus()==Status.COMPUTED || dbIntersection.getStatus()==Status.TOCOMPUTE || dbIntersection.getStatus()==Status.COMPUTING) {
    				boolean isDifferent = areIntersectionParameterDifferent(xmlIntersection, dbIntersection);
    				if (xmlIntersection.isForce() || isClean || isDifferent){
    					// if one of the three is valid we need to recompute the intersection and
    					// only in case different parameters update them 
    					dbIntersection.setStatus(Status.TOCOMPUTE);
    					
    					if (isDifferent) {
    						dbIntersection.setAreaCRS(xmlIntersection.getAreaCRS());
    						dbIntersection.setSrcCodeField(xmlIntersection.getSrcCodeField());
    						dbIntersection.setTrgCodeField(xmlIntersection.getTrgCodeField());
    						dbIntersection.setMask(xmlIntersection.isMask());
    						dbIntersection.setForce(xmlIntersection.isForce());
    						dbIntersection.setPreserveTrgGeom(xmlIntersection.isPreserveTrgGeom());
    						dbIntersection.setMaskLayer(xmlIntersection.getMaskLayer());
    					}
    					Request.updateIntersectionById(host, dbIntersection.getId(), dbIntersection);
    				}
    			}
    		}
    		else { // xmlIntersection does not exist in the DB
    			if (xmlIntersection.isMask() && (xmlIntersection.getMaskLayer()==null || xmlIntersection.getMaskLayer().equals(""))) xmlIntersection.setMaskLayer(defaultMaskLayer);
    			Request.insertIntersection(host, xmlIntersection);
    		}
    	}
    	
    }
    
    /*****
     * This method update the intersections in the database
     * 
     * @param host the host where addressing the request to update the database
     * @param XMLConfig the configuration file
     * @throws MalformedURLException
     */
    public void updateDataStore(String host, Config XMLConfig, String defaultMaskLayer ) throws MalformedURLException {
        // get the config either from the DB or from the XML
        Request.initConfig();
        Request.initIntersection();
        for (Intersection inter: XMLConfig.intersections) 
        	inter.setStatus(Status.TOCOMPUTE);
        Config config =  saveOrUpdateConfig(host, XMLConfig);
        
         // config is null in case of a wrong host name or an invalid update version of XMLConfig
         if (config!=null) {
        	 List<Intersection> XMLlist = XMLConfig.intersections;
        	 List<Intersection> dbList = Request.getAllIntersections(host);
        	 
        	 // update the status of the DB intersections on the basis of the XML intersections 
        	 updateIntersectionsOnDB(host, XMLlist, dbList, XMLConfig.getGlobal().isClean(), defaultMaskLayer);
        	 
           	//update the config in the DB
        	 if (XMLConfig.getGlobal().isClean()) removeUnlistedIntersectionsFromDB(host, XMLlist, dbList) ;
          }
    }
    
    /**
     * Removes TemplateModelEvents from the queue and put
     */
    public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {
    	Config XMLConfig = null;
    	host =  conf.getPersistencyHost();
    	defaultMaskLayer = conf.getDefaultMaskLayer();
        final Queue<EventObject> ret=new LinkedList<EventObject>();
                
        while (events.size() > 0) {
            final EventObject ev;
            try {
                if ((ev = events.remove()) != null) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("IntersectionAction.execute(): working on incoming event: "+ev.getSource());
                    }
                    FileSystemEvent fileEvent=(FileSystemEvent)ev;
           	      	FileSystemEventType  eventType = fileEvent.getEventType();


           	      	try {
                        // READ THE XML AND CREATE A CONFIG OBJECT
           	      		XMLConfig = ConfigXStreamMapper.init(fileEvent.getSource().getAbsolutePath());
           	      		LOGGER.info("Managing : "+fileEvent.getSource().getAbsolutePath());
           	      		// READ THE COMING CONFIG (XMLConfig) AND EVENTUALLY UPDATE THE CURRENT STATUS OF BOTH THE CONFIG AND THE INTERSECTIONS
                        updateDataStore(host, XMLConfig, defaultMaskLayer );
           	      	} catch(ConversionException e) {
           	      		LOGGER.error("Failed to convert the "+fileEvent.getSource().getName()+" configuration file");
           	      	}
                    
                    // THIS IS FOR DEBUGGING AIMS. SHOW THE CURRENT STATUS OF THE INTERSECTION AS THE EVENT IS RAISED.
                    // REMOVE IF NOT NECESSARY
            		try {
            			List<Intersection> list = Request.getAllIntersections(host);
            	        LOGGER.debug("Current status of the intersections");
            	        for (int i=0; i< list.size();i++) LOGGER.debug(list.get(i).toString());
            		} catch (MalformedURLException e) {
            		// TODO Auto-generated catch block
            			LOGGER.error("Failed to show the intersections",e);
            		}                    
                    
                    
                    
                    // add the event to the return
					ret.add(ev);
					
                } else {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("IntersectionAction.execute(): Encountered a NULL event: SKIPPING...");
                    }
                    continue;
                }
            } catch (Exception ioe) {
            	ioe.printStackTrace();
                final String message = "IntersectionAction.execute(): Unable to produce the output: "
                        + ioe.getLocalizedMessage();
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new ActionException(this, message);
            }
        }
        
        return ret;
    }
    
}
