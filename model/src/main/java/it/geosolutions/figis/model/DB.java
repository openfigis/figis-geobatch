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


@XStreamAlias("DB")
public class DB
{

    @XStreamAlias("dbtype")
    String dbtype;
    
    
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

    public DB(String schema, String database, String user, String password, String host, String port, String dbtype)
    {
        this.database = database;
        this.schema = schema;
        this.user = user;
        this.password = password;
        this.port = port;
        this.host = host;
        this.dbtype = dbtype;
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
    
    public String getDbtype() {
        return dbtype;
    }

    public void setDbtype(String dbtype) {
        this.dbtype = dbtype;
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

        final DB other = (DB) obj;
        if ((this.database == null) ? (other.database != null) : (!this.database.equals(other.database)))
        {
            return false;
        }
        if ((this.schema == null) ? (other.schema != null) : (!this.schema.equals(other.schema)))
        {
            return false;
        }
        if ((this.user == null) ? (other.user != null) : (!this.user.equals(other.user)))
        {
            return false;
        }
        if ((this.password == null) ? (other.password != null) : (!this.password.equals(other.password)))
        {
            return false;
        }
        if ((this.port == null) ? (other.port != null) : (!this.port.equals(other.port)))
        {
            return false;
        }
        if ((this.host == null) ? (other.host != null) : (!this.host.equals(other.host)))
        {
            return false;
        }
        if ((this.dbtype == null) ? (other.dbtype != null) : (!this.dbtype.equals(other.dbtype)))
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
