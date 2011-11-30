/**
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

    public InitHSQLDB() throws ServletException
    {
        super();

        this.database = DEFAULT_DATABASE;
        this.dbname = DEFAULT_DBNAME;
        this.port = DEFAULT_PORT;

        init();
    }

    public InitHSQLDB(String database, String dbname, String port) throws ServletException
    {
        super();

        this.database = database;
        this.dbname = dbname;
        this.port = port;

        init();
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

            Server server = new Server();
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
}
