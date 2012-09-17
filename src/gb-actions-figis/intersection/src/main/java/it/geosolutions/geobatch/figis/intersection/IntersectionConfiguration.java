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
package it.geosolutions.geobatch.figis.intersection;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;


/**
 *
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */

public class IntersectionConfiguration extends ActionConfiguration implements Configuration
{
    String persistencyHost = null;

    int itemsPerPages = -1;

    String ieServiceUsername = null;

    String ieServicePassword = null;

    public IntersectionConfiguration(String id, String name, String description)
    {
        super(id, name, description);
        // TODO INITIALIZE MEMBERS
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

    // TODO ADD YOUR MEMBERS

    public String getPersistencyHost()
    {
        return persistencyHost;
    }

    public void setPersistencyHost(String persistencyHost)
    {
        this.persistencyHost = persistencyHost;
    }

    public int getItemsPerPages()
    {
        return itemsPerPages;
    }

    public void setItemsPerPages(int itemsPerPages)
    {
        this.itemsPerPages = itemsPerPages;
    }

    @Override
    public IntersectionConfiguration clone()
    {
        final IntersectionConfiguration ret = (IntersectionConfiguration) super.clone();

        // TODO CLONE YOUR MEMBERS
        ret.setPersistencyHost(persistencyHost);
        ret.setItemsPerPages(itemsPerPages);
        ret.setIeServiceUsername(ieServiceUsername);
        ret.setIeServicePassword(ieServicePassword);
        ret.setServiceID(this.getServiceID());
        ret.setListenerConfigurations(ret.getListenerConfigurations());

        return ret;
    }

}
