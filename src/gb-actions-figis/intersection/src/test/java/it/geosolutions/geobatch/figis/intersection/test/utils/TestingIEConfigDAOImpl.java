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
package it.geosolutions.geobatch.figis.intersection.test.utils;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.figis.persistence.dao.util.PwEncoder;
import it.geosolutions.figis.requester.requester.dao.IEConfigDAO;
import it.geosolutions.figis.requester.requester.dao.impl.IEConfigDAOImpl;


/**
 * @author Alessio
 *
 */
public class TestingIEConfigDAOImpl implements IEConfigDAO
{

    Config ieConfig = null;

    /**
     * Default Constructor.
     *
     * @param dbIntersections
     */
    public TestingIEConfigDAOImpl(Config ieConfig)
    {
        this.ieConfig = ieConfig;
        this.ieConfig.getGlobal().getDb().setPassword(PwEncoder.encode(this.ieConfig.getGlobal().getDb().getPassword()));
        this.ieConfig.getGlobal().getGeoserver().setGeoserverPassword(PwEncoder.encode(this.ieConfig.getGlobal().getGeoserver().getGeoserverPassword()));
    }

    /**
     * @param updateVersion the updateVersion to set
     */
    public void setUpdateVersion(int updateVersion)
    {
        this.ieConfig.setUpdateVersion(updateVersion);
    }

    /**
     * @return the updateVersion
     */
    public int getUpdateVersion()
    {
        return this.ieConfig.getUpdateVersion();
    }

    /**
    * @param dbIntersections the dbIntersections to set
    */
    public void setDbIntersections(List<Intersection> dbIntersections)
    {
        this.ieConfig.intersections = dbIntersections;
    }

    /**
     * @return the dbIntersections
     */
    public List<Intersection> getDbIntersections()
    {
        return this.ieConfig.intersections;
    }

    /* (non-Javadoc)
    * @see it.geosolutions.figis.requester.requester.dao.IEConfigDAO#dbIsEmpty(java.lang.String, java.lang.String, java.lang.String)
    */
    public boolean dbIsEmpty(String host, String ieServiceUsername,
        String ieServicePassword) throws MalformedURLException
    {
        return ((getDbIntersections() == null) || getDbIntersections().isEmpty());
    }

    /* (non-Javadoc)
     * @see it.geosolutions.figis.requester.requester.dao.IEConfigDAO#deleteIntersectionById(java.lang.String, long, java.lang.String, java.lang.String)
     */
    public boolean deleteIntersectionById(String host, long id,
        String ieServiceUsername, String ieServicePassword)
    {
        List<Intersection> reducedIntersections = new ArrayList<Intersection>();

        for (Intersection intersection : getDbIntersections())
        {
            if (intersection.getId() != id)
            {
                reducedIntersections.add(intersection);
            }
        }

        setDbIntersections(reducedIntersections);

        return true;
    }

    /* (non-Javadoc)
     * @see it.geosolutions.figis.requester.requester.dao.IEConfigDAO#loadConfg(java.lang.String, java.lang.String, java.lang.String)
     */
    public Config loadConfg(String host, String ieServiceUsername,
        String ieServicePassword) throws MalformedURLException
    {
        return ieConfig;
    }

    /* (non-Javadoc)
     * @see it.geosolutions.figis.requester.requester.dao.IEConfigDAO#saveOrUpdateConfig(java.lang.String, it.geosolutions.figis.model.Config, java.lang.String, java.lang.String)
     */
    public Config saveOrUpdateConfig(String host, Config ieConfig,
        String ieServiceUsername, String ieServicePassword) throws MalformedURLException
    {
        if (ieConfig.getUpdateVersion() >= this.getUpdateVersion())
        {
            this.setUpdateVersion(ieConfig.getUpdateVersion());
            this.ieConfig.intersections = ieConfig.intersections;

            return ieConfig;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see it.geosolutions.figis.requester.requester.dao.IEConfigDAO#searchEquivalentOnDB(java.lang.String, it.geosolutions.figis.model.Intersection, java.lang.String, java.lang.String)
     */
    public Intersection searchEquivalentOnDB(String host,
        Intersection xmlIntersection, String ieServiceUsername,
        String ieServicePassword) throws MalformedURLException
    {
        return IEConfigDAOImpl.searchEquivalent(xmlIntersection, getDbIntersections());
    }

    /* (non-Javadoc)
     * @see it.geosolutions.figis.requester.requester.dao.IEConfigDAO#setStatus(java.lang.String, java.util.List, it.geosolutions.figis.model.Intersection.Status, java.lang.String, java.lang.String)
     */
    public void setStatus(String host, List<Intersection> intersections,
        Status status, String ieServiceUsername, String ieServicePassword)
    {
        for (Intersection intersection : intersections)
        {
            intersection.setStatus(status);
            updateIntersectionById(host, intersection.getId(), intersection, ieServiceUsername, ieServicePassword);
        }
    }

    /* (non-Javadoc)
     * @see it.geosolutions.figis.requester.requester.dao.IEConfigDAO#updateIntersectionById(java.lang.String, long, it.geosolutions.figis.model.Intersection, java.lang.String, java.lang.String)
     */
    public long updateIntersectionById(String host, long id,
        Intersection intersection, String ieServiceUsername,
        String ieServicePassword)
    {
        for (Intersection dbIntersection : getDbIntersections())
        {
            if (dbIntersection.getId() == id)
            {
                dbIntersection = intersection;

                return intersection.getId();
            }
        }

        return -1;
    }

}
