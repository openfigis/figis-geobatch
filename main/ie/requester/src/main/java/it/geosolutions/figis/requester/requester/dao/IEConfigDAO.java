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
package it.geosolutions.figis.requester.requester.dao;

import java.net.MalformedURLException;
import java.util.List;

import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;


/**
 * @author Alessio
 *
 */
public interface IEConfigDAO
{

    /**
    * Check for db empty
    * @param host
    * @return
    * @throws MalformedURLException
    */
    public boolean dbIsEmpty(String host, String ieServiceUsername, String ieServicePassword)
        throws MalformedURLException;

    /*********
     * SET THE CONFIG IF IT NOT EXIST OR UPDATE IT IF THE XMLConfig IS MORE RECENT OF THE CURRENT STATUS
     * @param host  the host where to address requests
     * @param XMLConfig the new configuration
     * @return a Config object representing the current status of the configuration
     * @throws MalformedURLException
     */
    public Config saveOrUpdateConfig(String host, Config ieConfig, String ieServiceUsername, String ieServicePassword)
        throws MalformedURLException;

    /**
     *
     * @param host
     * @return
     * @throws MalformedURLException
     */
    public Config loadConfg(String host, String ieServiceUsername, String ieServicePassword)
        throws MalformedURLException;

    /**
     *
     * @param host
     * @param id
     * @param ieServiceUsername
     * @param ieServicePassword
     * @return
     */
    public boolean deleteIntersectionById(String host, long id, String ieServiceUsername, String ieServicePassword);

    /**
     *
     * @param host
     * @param id
     * @param intersection
     * @param ieServiceUsername
     * @param ieServicePassword
     * @return
     */
    public long updateIntersectionById(String host, long id, Intersection intersection, String ieServiceUsername,
        String ieServicePassword);

    /**
     *
     * @param host
     * @param intersections
     * @param tocompute
     */
    public void setStatus(String host, List<Intersection> intersections, Status status, String ieServiceUsername,
        String ieServicePassword);

    /**
     *
     * @param host
     * @param xmlIntersection
     * @return
     * @throws MalformedURLException
     */
    public Intersection searchEquivalentOnDB(String host,
        Intersection xmlIntersection, String ieServiceUsername, String ieServicePassword) throws MalformedURLException;
}
