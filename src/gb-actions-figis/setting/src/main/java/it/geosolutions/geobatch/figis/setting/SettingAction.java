/*
 * ====================================================================
 *
 * GeoBatch - Intersection Engine
 *
 * Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 *
 * GPLv3 + Classpath exception
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
package it.geosolutions.geobatch.figis.setting;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.figis.requester.requester.dao.IEConfigDAO;
import it.geosolutions.figis.requester.requester.dao.impl.IEConfigDAOImpl;
import it.geosolutions.figis.requester.requester.util.IEConfigUtils;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class SettingAction extends BaseAction<EventObject>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingAction.class);


    private IEConfigDAO ieConfigDAO = null;

    private String defaultMaskLayer = null;
    private String host = null;
    private String ieServiceUsername = null;
    private String ieServicePassword = null;
    
    /**
     * @param xmlConfig
     * @param dbConfig
     * @param intersectionsToAdd
     * @throws CloneNotSupportedException
     */
    public static void compareXMLConfigAndDBConfig(Config xmlConfig,
        Config dbConfig, List<Intersection> intersectionsToAdd) throws CloneNotSupportedException
    {
        if (xmlConfig.getUpdateVersion() > dbConfig.getUpdateVersion())
        {
            dbConfig.setUpdateVersion(xmlConfig.getUpdateVersion());
            dbConfig.setGlobal(xmlConfig.getGlobal());
            if (xmlConfig.intersections != null)
            {
                if(LOGGER.isInfoEnabled()){LOGGER.info("Iterate over the intersectons");}
                for (Intersection xmlIntersection : xmlConfig.intersections)
                {
                    Intersection dbIntersection = IEConfigDAOImpl.searchEquivalent(xmlIntersection,
                            dbConfig.intersections);

                    
                    // not present in DB, lets add the new one
                    if ((dbIntersection == null))
                    {
                        // store the intersection in DB although the flagClean == TRUE
                        if(LOGGER.isDebugEnabled()){LOGGER.debug("intersection id: " + xmlIntersection.getId() + " - Set status to TOCOMPUTE");}
                        xmlIntersection.setStatus(Status.TOCOMPUTE);
                        intersectionsToAdd.add(xmlIntersection);
                    }
                    else
                    {
                        // it already computed or computing but maybe we want to force
                        // the re-computation ...
                        if(LOGGER.isDebugEnabled()){LOGGER.debug("Intersection id: " + xmlIntersection.getId() + " - already stored, evaluate the right status to set...");}
                        if (dbIntersection.getStatus().equals(Status.COMPUTED) ||
                                dbIntersection.getStatus().equals(
                                    Status.COMPUTING) ||
                                dbIntersection.getStatus().equals(
                                    Status.NOVALUE) ||
                                dbIntersection.getStatus().equals(
                                        Status.FAILED))
                        {
                            // Do we want to force the re-computation?
                            if (xmlIntersection.isForce())
                            {
                                xmlIntersection.setStatus(Status.TOCOMPUTE);
                            }
                            else
                            {
                                xmlIntersection.setStatus(dbIntersection.getStatus());
                            }
                        }
                        // otherwise in any case we assume the user wanted to
                        // re-schedule it for computation ...
                        else
                        {
                            xmlIntersection.setStatus(Status.TOCOMPUTE);
                        }
                        intersectionsToAdd.add(xmlIntersection);
                    }
                }
            }

            // if clean flag is set to true we need to schedule other
            // intersections for deletion
            if (dbConfig.intersections != null)
            {
                if(LOGGER.isInfoEnabled()){LOGGER.info("flag clean = TRUE, schedule to DELETE the other intersections too");}
                for (Intersection dbIntersection : dbConfig.intersections)
                {
                    Intersection equivalentToAdd = IEConfigDAOImpl.searchEquivalent(dbIntersection,
                            intersectionsToAdd);

                    if (equivalentToAdd == null)
                    {
                        if (xmlConfig.getGlobal().isClean())
                        {
                            dbIntersection.setStatus(Status.TODELETE);
                        }
                        intersectionsToAdd.add((Intersection) dbIntersection.clone());
                    }

                }
            }
        }
    }


    /**
     * configuration
     */
    private final SettingConfiguration conf;

    public SettingAction(SettingConfiguration configuration)
    {
        super(configuration);
        conf = configuration;
    }

    /**
     * Removes TemplateModelEvents from the queue and put
     */
    public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException
    {
        Config xmlConfig = null;
        Config dbConfig = null;
        host = conf.getPersistencyHost();
        ieServiceUsername = conf.getIeServiceUsername();
        ieServicePassword = conf.getIeServicePassword();
        defaultMaskLayer = conf.getDefaultMaskLayer();

        final Queue<EventObject> ret = new LinkedList<EventObject>();
        if(LOGGER.isInfoEnabled()){
        	LOGGER.info("Setting action started with parameters " + host + ", " + defaultMaskLayer);
        }
        while (events.size() > 0)
        {
            final EventObject ev;
            try
            {
                if ((ev = events.remove()) != null)
                {
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("IntersectionAction.execute(): working on incoming event: " +
                            ev.getSource());
                    }

                    FileSystemEvent fileEvent = (FileSystemEvent) ev;

                    if (ieConfigDAO == null)
                    {
                        LOGGER.error("ieConfigDAO was null!");
                        throw new ActionException(this, "ieConfigDAO was null!");
                    }

                    // READ THE XML AND CREATE A CONFIG OBJECT
                    xmlConfig = IEConfigUtils.parseXMLConfig(fileEvent.getSource().getAbsolutePath());
                    
                    if(LOGGER.isInfoEnabled()){LOGGER.info("xmlConfig unmarshalled");}
                    if(LOGGER.isDebugEnabled()){LOGGER.debug("xmlConfig unmarshalled toString: " + xmlConfig);}
                    
                    // if DB is empty lets insert the new configuration...
                    if (getIeConfigDAO().dbIsEmpty(host,
                                getIeServiceUsername(), getIeServicePassword()))
                    {
                        if(LOGGER.isInfoEnabled()){LOGGER.info("DB is EMPTY: going to save or update the config... ");}
                        
                        dbConfig = getIeConfigDAO().saveOrUpdateConfig(host,
                                xmlConfig, getIeServiceUsername(),
                                getIeServicePassword());
                        if(LOGGER.isDebugEnabled()){LOGGER.debug("config returned:  unmarshalled toString: " + dbConfig);}
                        if(dbConfig == null){
                            throw new Exception("An exception is occurred while managing the configuration provided... Please check the configuration version.");
                        }
                        
                        if(LOGGER.isInfoEnabled()){LOGGER.info("going to set global, updateVersion, status");}
                        dbConfig.setGlobal(xmlConfig.getGlobal());
                        dbConfig.setUpdateVersion(xmlConfig.getUpdateVersion() - 1);

                        getIeConfigDAO().setStatus(host,
                            dbConfig.intersections, Status.TOCOMPUTE,
                            getIeServiceUsername(), getIeServicePassword());
                    }
                    // check for updates otherwise...
                    else
                    {
                        if(LOGGER.isInfoEnabled()){LOGGER.info("DB is NOT EMPTY: load the config... ");}
                        dbConfig = getIeConfigDAO().loadConfg(host,
                                getIeServiceUsername(), getIeServicePassword());
                    }

                    if(LOGGER.isInfoEnabled()){LOGGER.info("Compare the config update...");}
                    if(LOGGER.isDebugEnabled()){LOGGER.debug("xmlConfig: " + xmlConfig.getUpdateVersion() + " - dbConfig: " + dbConfig.getUpdateVersion());}

                    // after checking for a valid update version ...                    
                    if (xmlConfig.getUpdateVersion() > dbConfig.getUpdateVersion())
                    {
                        if(LOGGER.isInfoEnabled()){LOGGER.info("Found a more recent intersectin version");}
                        if(LOGGER.isDebugEnabled()){LOGGER.debug("xmlConfig: " + xmlConfig.getUpdateVersion() + " - dbConfig: " + dbConfig.getUpdateVersion());}

                        List<Intersection> intersectionsToAdd = new ArrayList<Intersection>();

                        if(LOGGER.isInfoEnabled()){LOGGER.info("lets compare the intersections between xml config and db");}
                        compareXMLConfigAndDBConfig(xmlConfig, dbConfig,
                            intersectionsToAdd);

                        if (dbConfig.intersections != null)
                        {
                            if(LOGGER.isInfoEnabled()){LOGGER.info("Delete intersections by Id");}
                            for (Intersection dbIntersection : dbConfig.intersections)
                            {
                                if(LOGGER.isDebugEnabled()){LOGGER.debug("Delete Intersection " + dbIntersection.getId());}
                                getIeConfigDAO().deleteIntersectionById(host,
                                    dbIntersection.getId(),
                                    ieServiceUsername, ieServicePassword);
                            }
                        }
                        
                        dbConfig.intersections = intersectionsToAdd;

                        if(LOGGER.isInfoEnabled()){LOGGER.info("finally update the db-config");}
                        getIeConfigDAO().saveOrUpdateConfig(host, dbConfig,
                            getIeServiceUsername(), getIeServicePassword());
                    }
                    else{
                        if(LOGGER.isInfoEnabled()){LOGGER.info("Found an old or equals intersection version.");}
                    }

                    // add the event to the return
                    // ret.add(ev);
                }
                else
                {
                    if (LOGGER.isErrorEnabled())
                    {
                        LOGGER.error("IntersectionAction.execute(): Encountered a NULL event: SKIPPING...");
                    }

                    continue;
                }
            }
            catch (Exception ioe)
            {
                if (LOGGER.isErrorEnabled()){
                    LOGGER.error( "IntersectionAction.execute(): Unable to produce the output: ",ioe.getLocalizedMessage(),ioe);
                }
                throw new ActionException(this,ioe.getLocalizedMessage(),ioe );
            }
        }
        
        if(LOGGER.isInfoEnabled()){LOGGER.info("The action is finished");}
        return ret;
    }

    /**
     * @param ieConfigDAO
     *            the ieConfigDAO to set
     */
    public void setIeConfigDAO(IEConfigDAO ieConfigDAO)
    {
        this.ieConfigDAO = ieConfigDAO;
    }

    /**
     * @return the ieConfigDAO
     */
    public IEConfigDAO getIeConfigDAO()
    {
        return ieConfigDAO;
    }

    public String getIeServiceUsername()
    {
        return ieServiceUsername;
    }

    public void setIeServiceUsername(String ieServiceUsername)
    {
        this.ieServiceUsername = ieServiceUsername;
    }

    public String getIeServicePassword()
    {
        return ieServicePassword;
    }

    public void setIeServicePassword(String ieServicePassword)
    {
        this.ieServicePassword = ieServicePassword;
    }

    // ------------------------------------------------------------------------------------------------------------------
}
