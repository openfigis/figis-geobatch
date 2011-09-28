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
package it.geosolutions.geobatch.figis.intersection;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
/*import it.geosolutions.geobatch.figis.intersection.model.Config;
import it.geosolutions.geobatch.figis.intersection.model.ConfigXStreamMapper;
import it.geosolutions.geobatch.figis.intersection.model.Intersection;
import it.geosolutions.geobatch.figis.intersection.model.Intersection.Status;*/
import it.geosolutions.geobatch.figis.intersection.model.Config;
import it.geosolutions.geobatch.figis.intersection.model.ConfigXStreamMapper;
import it.geosolutions.geobatch.figis.intersection.model.Intersection;
import it.geosolutions.geobatch.figis.intersection.model.Intersection.Status;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;


import java.util.EventObject;
import java.util.Iterator;
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
public class IntersectionAction extends BaseAction<EventObject> {
    private final static Logger LOGGER = LoggerFactory.getLogger(IntersectionAction.class);

    /**
     * configuration
     */
    private final IntersectionConfiguration conf;
    private Config XMLConfig;
    
    public IntersectionAction(IntersectionConfiguration configuration) {
        super(configuration);
        conf = configuration;
        
        //TODO initialize your members here
    }

    /**
     * Removes TemplateModelEvents from the queue and put
     */
    public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {

        // return
        final Queue<EventObject> ret=new LinkedList<EventObject>();
        
        while (events.size() > 0) {
            final EventObject ev;
            try {
 
                if ((ev = events.remove()) != null) {
                	
               	FileSystemEvent fileEvent=(FileSystemEvent)ev;
               	      	FileSystemEventType  eventType = fileEvent.getEventType();
                	if (eventType.compareTo(FileSystemEventType.FILE_ADDED)==0){
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("IntersectionAction.execute(): working on incoming event: "+fileEvent.getSource());
                            LOGGER.trace("File added");
                        }
                        // get the config object from the XML
                        XMLConfig = ConfigXStreamMapper.init(fileEvent.getSource().getName());
                        // get the config from the DB
                        Config config = conf.getConfigDao().getConfig();
                        if (config==null) { // check if the DB config is empty
                        	conf.getConfigDao().insertConfig(XMLConfig);
                        }
                        // if the XML config version is greater or equal than the DB config execute the action 
                        if (XMLConfig.getUpdateVersion() >= config.getUpdateVersion() ) {
                          	//update the config in the DB
                          	conf.getConfigDao().updateConfig(XMLConfig);
                           	// check if intersections exist in the XML
                           	List<Intersection> list = XMLConfig.getIntersections();
                           	for (Intersection intersection: list) {
                           		// look for the XML intersection in the DB
                           		Intersection intersectionDB = conf.getIntersectionDao().lookUp(intersection);
                           		if (intersectionDB!=null) { // intersection is present in the DB
	                           		if (intersectionDB.getStatus() == Status.COMPUTED){
	                           			if (XMLConfig.isClean() || intersection.isForce()) {
	                           				conf.getIntersectionDao().force(intersection);
	                           			}
	                           		}
                           		} 
                           		else { // intersection is not in the DB
                           			conf.getIntersectionDao().insert(intersection);
                           		}
                           	}
                           	List<Intersection> DBlist = conf.getIntersectionDao().getAllIntersections();
                           	for (Intersection intersection: DBlist) {
                           		if (XMLConfig.isClean() && !(list.contains(intersection))){
                           			conf.getIntersectionDao().remove(intersection);
                           		}
                           	}
                         }
                    }
                	
                  	
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("IntersectionAction.execute(): action terminated: "+fileEvent.getSource());
                    }
                    // DO SOMETHING WITH THE INCOMING EVENT
                    
                    // add the event to the return
					ret.add(ev);
					
                } else {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("IntersectionAction.execute(): Encountered a NULL event: SKIPPING...");
                    }
                    continue;
                }
            } catch (Exception ioe) {
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
