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

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("geoserver")
public class Geoserver implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = 6543783674943716775L;

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

    public Geoserver(String geoserverUrl, String geoserverUsername,
        String geoserverPassword)
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
        if (!(obj instanceof Geoserver))
        {
            return false;
        }

        Geoserver other = (Geoserver) obj;
        if (geoserverPassword == null)
        {
            if (other.geoserverPassword != null)
            {
                return false;
            }
        }
        else if (!geoserverPassword.equals(other.geoserverPassword))
        {
            return false;
        }
        if (geoserverUrl == null)
        {
            if (other.geoserverUrl != null)
            {
                return false;
            }
        }
        else if (!geoserverUrl.equals(other.geoserverUrl))
        {
            return false;
        }
        if (geoserverUsername == null)
        {
            if (other.geoserverUsername != null)
            {
                return false;
            }
        }
        else if (!geoserverUsername.equals(other.geoserverUsername))
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
        result = (prime *
                result) +
            ((geoserverPassword == null) ? 0 : geoserverPassword.hashCode());
        result = (prime * result) +
            ((geoserverUrl == null) ? 0 : geoserverUrl.hashCode());
        result = (prime *
                result) +
            ((geoserverUsername == null) ? 0 : geoserverUsername.hashCode());

        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Geoserver [geoserverPassword=" + geoserverPassword +
            ", geoserverUrl=" + geoserverUrl + ", geoserverUsername=" +
            geoserverUsername + "]";
    }

}
