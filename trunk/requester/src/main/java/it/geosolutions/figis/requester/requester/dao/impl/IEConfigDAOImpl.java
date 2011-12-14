/*
 * ====================================================================
 *
 * Intersection Engine
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
 
package it.geosolutions.figis.requester.requester.dao.impl;

import java.net.MalformedURLException;
import java.util.List;

import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.figis.requester.Request;
import it.geosolutions.figis.requester.requester.dao.IEConfigDAO;
import it.geosolutions.figis.requester.requester.util.IEConfigUtils;

import org.apache.log4j.Logger;


/**
 * 
 * @author Alessio
 *
 */
public class IEConfigDAOImpl implements IEConfigDAO
{

    private static final Logger LOGGER = Logger.getLogger(IEConfigDAO.class);

    /**
    * Search for equivalent Intersection on db-oracle
    * 
    * @param host
    * @param xmlIntersection
    * @param intersections
    * @return
    */
    public static Intersection searchEquivalent(Intersection xmlIntersection, List<Intersection> intersections)
    {
        Intersection matchingIntersection = null;

        if (intersections != null)
        {
            for (Intersection target : intersections)
            {
                if (!IEConfigUtils.areIntersectionParameterDifferent(xmlIntersection, target))
                {
                    matchingIntersection = target;

                    break;
                }
            }
        }

        return matchingIntersection;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.geosolutions.geobatch.figis.setting.dao.IEConfigDAO#dbIsEmpty(java
     * .lang.String)
     */
    public boolean dbIsEmpty(String host, String ieServiceUsername, String ieServicePassword)
        throws MalformedURLException
    {
        Request.initIntersection();

        List<Intersection> list = Request.getAllIntersections(host, ieServiceUsername, ieServicePassword);

        if ((list == null) || list.isEmpty())
        {
            return true;
        }

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Current status of the intersections");
            final int size=list.size();
            for (int i = 0; i < size; i++)
            {
                LOGGER.debug(list.get(i).toString());
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * it.geosolutions.geobatch.figis.setting.dao.IEConfigDAO#saveOrUpdateConfig
     * (java.lang.String, it.geosolutions.figis.requester.model.Config)
     */
    public Config saveOrUpdateConfig(String host, Config ieConfig, String ieServiceUsername, String ieServicePassword)
        throws MalformedURLException
    {
        Request.initConfig();

        Config config = null;
        config = Request.existConfig(host, ieServiceUsername, ieServicePassword); // check if a configuration
        // currently exist in the database
        if (config == null) // check if the DB config is empty
        {
            long id = Request.insertConfig(host, ieConfig, ieServiceUsername, ieServicePassword); // insert the new
            // configuration
            for (Intersection intersection : ieConfig.intersections)
            {
                Request.insertIntersection(host, intersection, ieServiceUsername, ieServicePassword);
            }

            config = Request.getConfigByID(host, id, ieServiceUsername, ieServicePassword);
            config.intersections = Request.getAllIntersections(host, ieServiceUsername, ieServicePassword);

            return config; // return the current
                           // configuration
        }
        else
        { // else check if the current version is less than the XML config version
            if (ieConfig.getUpdateVersion() >= config.getUpdateVersion())
            {
                long id = Request.updateConfig(host, config.getConfigId(),
                        ieConfig, ieServiceUsername, ieServicePassword); // update the configuration with new
                // information

                Request.deleteAllIntersections(host, ieServiceUsername, ieServicePassword);
                for (Intersection intersection : ieConfig.intersections)
                {
                    Request.insertIntersection(host, intersection, ieServiceUsername, ieServicePassword);
                }

                config = Request.getConfigByID(host, id, ieServiceUsername, ieServicePassword);
                config.intersections = Request.getAllIntersections(host, ieServiceUsername, ieServicePassword);

                return config; // return the current
                               // configuration
            } // return a null value to indicate that the new config is not
              // valid because the version
            else
            {
                return null;
            }
        }

    }

    /* (non-Javadoc)
     * @see it.geosolutions.geobatch.figis.setting.dao.IEConfigDAO#loadConfg(java.lang.String)
     */
    public Config loadConfg(String host, String ieServiceUsername, String ieServicePassword)
        throws MalformedURLException
    {
        Request.initConfig();
        Request.initIntersection();

        Config config = null;

        config = Request.existConfig(host, ieServiceUsername, ieServicePassword);

        if(config != null)config.intersections = Request.getAllIntersections(host, ieServiceUsername, ieServicePassword);

        return config;
    }

    /* (non-Javadoc)
     * @see it.geosolutions.geobatch.figis.setting.dao.IEConfigDAO#setStatus(java.lang.String, java.util.List, it.geosolutions.figis.requester.model.Intersection.Status)
     */
    public void setStatus(String host, List<Intersection> intersections, Status status, String ieServiceUsername,
        String ieServicePassword)
    {
        Request.initIntersection();

        if (intersections != null)
        {
            for (Intersection intersection : intersections)
            {
                intersection.setStatus(status);
                Request.updateIntersectionById(host, intersection.getId(), intersection, ieServiceUsername, ieServicePassword);
            }
        }
    }

    /* (non-Javadoc)
     * @see it.geosolutions.geobatch.figis.setting.dao.IEConfigDAO#searchEquivalent(java.lang.String, it.geosolutions.figis.requester.model.Intersection)
     */
    public Intersection searchEquivalentOnDB(String host,
        Intersection xmlIntersection, String ieServiceUsername, String ieServicePassword) throws MalformedURLException
    {
        Request.initIntersection();

        List<Intersection> list = Request.getAllIntersections(host, ieServiceUsername, ieServicePassword);

        return searchEquivalent(xmlIntersection, list);
    }

    /* (non-Javadoc)
     * @see it.geosolutions.geobatch.figis.setting.dao.IEConfigDAO#deleteIntersectionById(java.lang.String, long, java.lang.String, java.lang.String)
     */
    public boolean deleteIntersectionById(String host, long id,
        String ieServiceUsername, String ieServicePassword)
    {
        Request.initIntersection();

        return Request.deleteIntersectionById(host, id, ieServiceUsername, ieServicePassword);
    }

    /* (non-Javadoc)
     * @see it.geosolutions.geobatch.figis.setting.dao.IEConfigDAO#updateIntersectionById(java.lang.String, long, it.geosolutions.figis.requester.model.Intersection, java.lang.String, java.lang.String)
     */
    public long updateIntersectionById(String host, long id,
        Intersection intersection, String ieServiceUsername,
        String ieServicePassword)
    {
        Request.initIntersection();

        return Request.updateIntersectionById(host, id, intersection, ieServiceUsername, ieServicePassword);
    }

}
