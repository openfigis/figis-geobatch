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


@XStreamAlias("global")
public class Global implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = -3454450779416778676L;

    @XStreamAlias("geoserver")
    Geoserver geoserver;

    @XStreamAlias("db")
    DB db;

    @XStreamAlias("clean")
    private boolean clean;

    public Global()
    {
        super();
        this.geoserver = new Geoserver();
        this.db = new DB();
    }

    public boolean isClean()
    {
        return clean;
    }

    public void setClean(boolean clean)
    {
        this.clean = clean;
    }

    public Geoserver getGeoserver()
    {
        return geoserver;
    }

    public void setGeoserver(Geoserver geoserver)
    {
        this.geoserver = geoserver;
    }

    public DB getDb()
    {
        return db;
    }

    public void setDb(DB db)
    {
        this.db = db;
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
        if (!(obj instanceof Global))
        {
            return false;
        }

        Global other = (Global) obj;
        if (clean != other.clean)
        {
            return false;
        }
        if (db == null)
        {
            if (other.db != null)
            {
                return false;
            }
        }
        else if (!db.equals(other.db))
        {
            return false;
        }
        if (geoserver == null)
        {
            if (other.geoserver != null)
            {
                return false;
            }
        }
        else if (!geoserver.equals(other.geoserver))
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
        result = (prime * result) + (clean ? 1231 : 1237);
        result = (prime * result) + ((db == null) ? 0 : db.hashCode());
        result = (prime * result) +
            ((geoserver == null) ? 0 : geoserver.hashCode());

        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Global [clean=" + clean + ", db=" + db + ", geoserver=" +
            geoserver + "]";
    }

}
