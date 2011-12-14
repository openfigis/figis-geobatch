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

/**************
 * This class is the model for the Config object
 */

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.*;


@XStreamAlias("ie-config")
@XmlRootElement(name = "config")
public class Config
{
    private long configId;

    @XStreamAlias("updateVersion")
    private int updateVersion;


    @XStreamAlias("global")
    private Global global;


    public List<Intersection> intersections;


    public Config()
    {
        super();
    }


    public long getConfigId()
    {
        return configId;
    }


    public void setConfigId(long configId)
    {
        this.configId = configId;
    }


    public Global getGlobal()
    {
        return global;
    }


    public void setGlobal(Global global)
    {
        this.global = global;
    }


    public int getUpdateVersion()
    {
        return updateVersion;
    }

    public void setUpdateVersion(int updateVersion)
    {
        this.updateVersion = updateVersion;
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

        final Config other = (Config) obj;
        if (this.configId != other.configId)
        {
            return false;
        }
        if (this.updateVersion != other.updateVersion)
        {
            return false;
        }
        if ((this.global != other.global) && ((this.global == null) || !this.global.equals(other.global)))
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
