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


@XStreamAlias("DB")
public class DB implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = -7474579778109771490L;

    @XStreamAlias("database")
    String database;

    @XStreamAlias("schema")
    String schema;

    @XStreamAlias("user")
    String user;

    @XStreamAlias("password")
    String password;

    @XStreamAlias("port")
    String port;

    @XStreamAlias("host")
    String host;

    public DB()
    {
        super();
    }

    public DB(String schema, String database, String user, String password,
        String host, String port)
    {
        this.database = database;
        this.schema = schema;
        this.user = user;
        this.password = password;
        this.port = port;
        this.host = host;
    }

    public String getDatabase()
    {
        return database;
    }

    public void setDatabase(String database)
    {
        this.database = database;
    }

    public String getSchema()
    {
        return schema;
    }

    public void setSchema(String schema)
    {
        this.schema = schema;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPort()
    {
        return port;
    }

    public void setPort(String port)
    {
        this.port = port;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
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
        if (!(obj instanceof DB))
        {
            return false;
        }

        DB other = (DB) obj;
        if (database == null)
        {
            if (other.database != null)
            {
                return false;
            }
        }
        else if (!database.equals(other.database))
        {
            return false;
        }
        if (host == null)
        {
            if (other.host != null)
            {
                return false;
            }
        }
        else if (!host.equals(other.host))
        {
            return false;
        }
        if (password == null)
        {
            if (other.password != null)
            {
                return false;
            }
        }
        else if (!password.equals(other.password))
        {
            return false;
        }
        if (port == null)
        {
            if (other.port != null)
            {
                return false;
            }
        }
        else if (!port.equals(other.port))
        {
            return false;
        }
        if (schema == null)
        {
            if (other.schema != null)
            {
                return false;
            }
        }
        else if (!schema.equals(other.schema))
        {
            return false;
        }
        if (user == null)
        {
            if (other.user != null)
            {
                return false;
            }
        }
        else if (!user.equals(other.user))
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
        result = (prime * result) +
            ((database == null) ? 0 : database.hashCode());
        result = (prime * result) + ((host == null) ? 0 : host.hashCode());
        result = (prime * result) +
            ((password == null) ? 0 : password.hashCode());
        result = (prime * result) + ((port == null) ? 0 : port.hashCode());
        result = (prime * result) + ((schema == null) ? 0 : schema.hashCode());
        result = (prime * result) + ((user == null) ? 0 : user.hashCode());

        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "DB [database=" + database + ", host=" + host + ", password=" +
            password + ", port=" + port + ", schema=" + schema +
            ", user=" + user + "]";
    }

}
