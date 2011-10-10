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
package it.geosolutions.geobatch.figis.config;

import it.geosolutions.figis.Request;
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.ConfigXStreamMapper;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class ConfigAction extends BaseAction<EventObject> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigAction.class);

    /**
     * configuration
     */
    private final ConfigConfiguration conf;

    public ConfigAction(ConfigConfiguration configuration) {
        super(configuration);
        conf = configuration;
        //TODO initialize your members here
    }

    /**
     * Removes TemplateModelEvents from the queue and put
     */
    public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {
    	final String host = "http://localhot:8080";
        // return
        final Queue<EventObject> ret=new LinkedList<EventObject>();
        
        while (events.size() > 0) {
            final EventObject ev;
            try {
                if ((ev = events.remove()) != null) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("ConfigAction.execute(): working on incoming event: "+ev.getSource());
                    }
                    FileSystemEvent fileEvent=(FileSystemEvent)ev;
                    // DO SOMETHING WITH THE INCOMING EVENT
                    //*****************************************************
                    // get the config object from the XML
                    // at this moment it contains both Config info and Intersections
                    Config XMLConfig = ConfigXStreamMapper.init(fileEvent.getSource().getName());
                    
                    // enable Request to parse Config XML Stream
                    Request.initConfig();
                    
                    // get the config from the DB
                    Config currentConfig = Request.existConfig(host);
                    
                    if (currentConfig==null) { // check if the DB config is empty
                    	Request.insertConfig(host, XMLConfig);
                        List<Intersection> list = XMLConfig.intersections;
                        for (Intersection intersection:list) Request.insertIntersection(host, intersection);    	
                    }
                    else {
                       // if the XML config version is greater or equal than the DB config execute the action 
                    	if (XMLConfig.getUpdateVersion() >= currentConfig.getUpdateVersion() ) {
                    		//update the config in the DB
                    		Request.updateConfig(host, currentConfig.getConfigId(), XMLConfig);
                    		
                          	// get intersections from XMLConfig
                         	List<Intersection> XMLlist = XMLConfig.intersections;
                         	
                         	// get intersections from DB
                         	List<Intersection> DBList = Request.getAllIntersections(host);
                         	
                         	for (Intersection intersection: XMLlist) {
                       		    // look for the XML intersection in the DB
                         		int index = DBList.indexOf(intersection);
                         		if (index>=0) {
                         			Intersection indexIntersection = DBList.get(index);
                         			if (indexIntersection.getStatus()== Status.COMPUTED && (XMLConfig.isClean() || intersection.isForce() || !intersection.equals(indexIntersection))) {
                         				indexIntersection.setStatus(Status.TOCOMPUTE);
                         				Request.updateIntersectionById(host, indexIntersection.getId(), indexIntersection);
                         			}
                         		}
                         		else  Request.insertIntersection(host, intersection);
                        	}
                         	// in case the clean attribute is set, delete all the DB intersections not present in the XML
                         	for (Intersection dbIntersection: DBList) {
                        		int index = XMLlist.indexOf(dbIntersection);
                        		if (index==-1 && XMLConfig.isClean()) {
                        			dbIntersection.setStatus(Status.TODELETE);
                     				Request.updateIntersectionById(host, dbIntersection.getId(), dbIntersection);
     
                        		}                         		
                         	}
                    	} // close if (XMLConfig.getUpdateVersion() >= config.getUpdateVersion() )
                    } // close else of if (config==null)
                        	
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("IntersectionAction.execute(): action terminated: "+fileEvent.getSource());
                    }
  
 //		******************************************************                          
                    // add the event to the return
					ret.add(ev);
					
                } else {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("ConfigAction.execute(): Encountered a NULL event: SKIPPING...");
                    }
                    continue;
                }
            } catch (Exception ioe) {
                final String message = "ConfigAction.execute(): Unable to produce the output: "
                        + ioe.getLocalizedMessage();
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new ActionException(this, message);
            }
        }
        
        return ret;
    }
    
}
