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
package it.geosolutions.figis.model;


import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("geoserver")
public class Geoserver
{


    @XStreamAlias("geoserverUrl")
    String geoserverUrl;


    @XStreamAlias("geoserverUsername")
    String geoserverUsername;


    @XStreamAlias("geoserverPassword")
    String geoserverPassword;


    public Geoserver()
    {
        super();
    }

    public Geoserver(String geoserverUrl, String geoserverUsername, String geoserverPassword)
    {
        this.geoserverUrl = geoserverUrl;
        this.geoserverUsername = geoserverUsername;
        this.geoserverPassword = geoserverPassword;
    }


    public String getGeoserverUrl()
    {
        return geoserverUrl;
    }

    public void setGeoserverUrl(String geoserverUrl)
    {
        this.geoserverUrl = geoserverUrl;
    }

    public String getGeoserverUsername()
    {
        return geoserverUsername;
    }

    public void setGeoserverUsername(String geoserverUsername)
    {
        this.geoserverUsername = geoserverUsername;
    }

    public String getGeoserverPassword()
    {
        return geoserverPassword;
    }

    public void setGeoserverPassword(String geoserverPassword)
    {
        this.geoserverPassword = geoserverPassword;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }

        final Geoserver other = (Geoserver) obj;
        if ((this.geoserverUrl == null) ? (other.geoserverUrl != null) : (!this.geoserverUrl.equals(other.geoserverUrl)))
        {
            return false;
        }
        if ((this.geoserverUsername == null) ? (other.geoserverUsername != null) : (!this.geoserverUsername.equals(other.geoserverUsername)))
        {
            return false;
        }
        if ((this.geoserverPassword == null) ? (other.geoserverPassword != null) : (!this.geoserverPassword.equals(other.geoserverPassword)))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;

        return hash;
    }

}
