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
package it.geosolutions.geobatch.figis.cron;

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
public class CronAction extends BaseAction<EventObject> {
    private final static Logger LOGGER = LoggerFactory.getLogger(CronAction.class);

    /**
     * configuration
     */
    private final CronConfiguration conf;

    public CronAction(CronConfiguration configuration) {
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
