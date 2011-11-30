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
package it.geosolutions.figis.model;

/**************
 * This class is the model for the Config object
 */

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("ie-config")
@XmlRootElement(name = "config")
public class Config implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = -1498334087489667209L;

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

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof Config))
        {
            return false;
        }

        Config other = (Config) obj;
        if (configId != other.configId)
        {
            return false;
        }
        if (global == null)
        {
            if (other.global != null)
            {
                return false;
            }
        }
        else if (!global.equals(other.global))
        {
            return false;
        }
        if (intersections == null)
        {
            if (other.intersections != null)
            {
                return false;
            }
        }
        else if (!intersections.equals(other.intersections))
        {
            return false;
        }
        if (updateVersion != other.updateVersion)
        {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (configId ^ (configId >>> 32));
        result = (prime * result) + ((global == null) ? 0 : global.hashCode());
        result = (prime * result) +
            ((intersections == null) ? 0 : intersections.hashCode());
        result = (prime * result) + updateVersion;

        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Config [configId=" + configId + ", global=" + global +
            ", intersections=" + intersections + ", updateVersion=" +
            updateVersion + "]";
    }

}
