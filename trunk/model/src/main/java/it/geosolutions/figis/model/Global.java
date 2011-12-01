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


import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("global")
public class Global
{


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

        final Global other = (Global) obj;
        if ((this.geoserver != other.geoserver) && ((this.geoserver == null) || !this.geoserver.equals(other.geoserver)))
        {
            return false;
        }
        if ((this.db != other.db) && ((this.db == null) || !this.db.equals(other.db)))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;

        return hash;
    }


}
