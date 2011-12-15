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
package it.geosolutions.figis.ws.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Servlet to initialize ie-db (hsql db). This can start fronm servlet
 * or by initialization injection by context-spring
 * 
 * @author Alessio
 *
 */
public class InitHSQLDB extends HttpServlet
{

    /**
    *
    */
    private static final long serialVersionUID = 1465759324373937724L;

    private static final Logger LOGGER = LoggerFactory.getLogger(InitHSQLDB.class);

    private static final String DEFAULT_PORT = "9001";

    private static final String DEFAULT_DBNAME = "intersectionengine";

    private static final String DEFAULT_DATABASE = "file:/tmp/data/intersectionengine";

    private String database;

    private String dbname;

    private String port;

	private Server server;

    public InitHSQLDB() throws ServletException
    {
    	this(DEFAULT_DATABASE,DEFAULT_DBNAME,DEFAULT_PORT);

    }

    public InitHSQLDB(String database, String dbname, String port) throws ServletException
    {
        this.database = database;
        this.dbname = dbname;
        this.port = port;

    }

    @Override
    public void init() throws ServletException
    {
        super.init();
        try
        {
            LOGGER.info("Starting Database...");

            final HsqlProperties p = new HsqlProperties();
            p.setProperty("server.database.0", getDatabase());
            p.setProperty("server.dbname.0", getDbname());
            p.setProperty("server.port", getPort());

            server = new Server();
            server.setProperties(p);
            server.setLogWriter(null); // can use custom writer
            server.setErrWriter(null); // can use custom writer
            server.start();
            
        }
        catch (Exception ex)
        {
            throw new ServletException(ex);
        }
    }

    /**
     * @param database the database to set
     */
    public void setDatabase(String database)
    {
        this.database = database;
    }

    /**
     * @return the database
     */
    public String getDatabase()
    {
        return database;
    }

    /**
     * @param dbname the dbname to set
     */
    public void setDbname(String dbname)
    {
        this.dbname = dbname;
    }

    /**
     * @return the dbname
     */
    public String getDbname()
    {
        return dbname;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port)
    {
        this.port = port;
    }

    /**
     * @return the port
     */
    public String getPort()
    {
        return port;
    }
    
    public void destroy(){
    	if(server!=null){
    		try{
    			server.stop();
    		} catch (Exception e) {
				LOGGER.debug(e.getLocalizedMessage(),e);
			}
    	}
    }
}
