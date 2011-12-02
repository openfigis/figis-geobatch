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
package it.geosolutions.geobatch.figis.intersection;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

import it.geosolutions.figis.Request;
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Geoserver;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.figis.persistence.dao.util.PwEncoder;
import it.geosolutions.geobatch.figis.intersection.util.TmpDirManager;
import it.geosolutions.geobatch.figis.intersection.util.ZipStreamReader;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.process.feature.gs.ClipProcess;
import org.geotools.process.feature.gs.IntersectionFeatureCollection;
import org.geotools.process.feature.gs.IntersectionFeatureCollection.IntersectionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class IntersectionAction extends BaseAction<EventObject>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IntersectionAction.class);
    private static int itemsPerPage = 50;
    private GeoServerRESTReader gsRestReader = null;
    Random generator = new Random();
    // private WFS_1_0_0_DataStore dataStore = null;
    private Geoserver geoserver = null;
    /**
     * configuration
     */
    private final IntersectionConfiguration conf;
    private String host = "http://localhost:9999";
    private String tmpDirName = "figis";
    /* Username ie-service */
    private String ieServiceUsername = null;
    /* Password ie-service */
    private String ieServicePassword = null;
    
    private static Geometry union(Geometry a, Geometry b)
    {
        return reduce(EnhancedPrecisionOp.union(a, b));
    }

    /**
     * Reduce a GeometryCollection to a MultiPolygon.  This method basically explores
     * the collection and assembles all the linear rings and polygons into a multipolygon.
     * The idea there is that contains() works on a multi polygon but not a collection.
     * If we throw out points and lines, etc, we should still be OK.  This is not 100%
     * correct, but we should still be able to throw away some features which is the point
     * of all this.
     *
     * @param geometry
     * @return
     */
    private static Geometry reduce(Geometry geometry)
    {
        if (geometry instanceof GeometryCollection)
        {

            if (geometry instanceof MultiPolygon)
            {
                return geometry;
            }

            // WKTWriter wktWriter = new WKTWriter();
            // logger.warn("REDUCING COLLECTION: " + wktWriter.write(geometry));

            final ArrayList<Polygon> polygons = new ArrayList<Polygon>();
            final GeometryFactory factory = geometry.getFactory();

            geometry.apply(new GeometryComponentFilter()
                {
                    public void filter(Geometry geom)
                    {
                        // logger.info("FILTER: " + geom);
                        // System.out.println("  ==>  filter:  " + geom);

                        if (geom instanceof LinearRing)
                        {
                            // System.out.println("    --> add linear ring");
                            polygons.add(factory.createPolygon((LinearRing) geom, null));
                        }
                        else if (geom instanceof LineString)
                        {
                            // what to do?
                        }
                        else if (geom instanceof Polygon)
                        {
                            // System.out.println("    --> add polygon");
                            polygons.add((Polygon) geom);
                        }
                    }
                });

            // System.out.println("  ==>  collected " + polygons.size() + " polygons");
            MultiPolygon multiPolygon = factory.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
            multiPolygon.normalize();

            // logger.info("REDUCED TO: " + wktWriter.write(multiPolygon));

            return multiPolygon;
        }

        return geometry;
    }

    
    
    public IntersectionAction(IntersectionConfiguration configuration)
    {
        super(configuration);
        conf = configuration;
        host = conf.getPersistencyHost();
        ieServiceUsername = conf.getIeServiceUsername();
        ieServicePassword = conf.getIeServicePassword();
    }


    /******
     * this method takes a zip file name and return its SimpleFeatureCollection
     *
     * @param filename
     *            the full name of the shape file
     * @return the simple feature collection
     */
    private SimpleFeatureCollection SimpleFeatureCollectionByShp(String filename)
    {
        // ShapefileDirectoryFactory factory = new ShapefileDirectoryFactory();
        // URL url = new URL(directory);
        // String typeName = factory.getTypeName(url);

        File shpfile = new File(filename);
        LOGGER.trace("ZSR: SimpleFeatureCollection: shpfile: " + shpfile);
        if (shpfile != null)
        {
            LOGGER.trace("ZSR: SimpleFeatureCollection: shpfile.getAbsolutePath(): " +
                shpfile.getAbsolutePath());
        }
        if (shpfile != null)
        {
            LOGGER.trace("ZSR: SimpleFeatureCollection: shpfile.getName(): " +
                shpfile.getName());
        }
        if (!shpfile.exists())
        {
            return null;
        }

        FileDataStore store = null;
        SimpleFeatureCollection sfc = null;

        try
        {
            URL url = new URL("file://" + shpfile);
            LOGGER.trace("ZSR: SimpleFeatureCollection: url: " + url);
            store = new ShapefileDataStore(url);

            FeatureSource fs = store.getFeatureSource();
            LOGGER.trace("ZSR: SimpleFeatureCollection: fs: " + fs);
            sfc = (SimpleFeatureCollection) fs.getFeatures();

            return sfc;
        }
        catch (Exception e1)
        {
            LOGGER.trace("ZSR: error:");
            e1.printStackTrace();

            return null;
        }

    }

    /**********
     * download a zip file from geoserver, save it in the tmp directory,
     * uncompress it and return the name of the containe shape file
     *
     * @param layername
     *            the layer to download
     * @param tmpDir
     *            the folder where saving and uncompress the downloaded zip file
     * @return the name of the shape file downloaded
     */
    private String downloadFromGeoserver(String layername, String tmpDir)
    {
        String urlCollection = "";
        try
        {
            LOGGER.info("Downloading " + layername + " ");

            String name = getName(layername);
            if (geoserver != null)
            {
                LOGGER.info("Georserverl URL " + geoserver.getGeoserverUrl());
                // build the url to download the zip file by wfs
                urlCollection = geoserver.getGeoserverUrl() +
                    "/wfs?outputFormat=SHAPE-ZIP&request=GetFeature&version=1.1.1&typeName=" +
                    name + "&srs=EPSG:4326";
            }
            else
            {
                LOGGER.error("Geoserver is null, check the data");

                return null;
            }

            // uncompress the downloaded file and return the name of the shape
            // file
            return ZipStreamReader.getShapeFileFromURLbyZIP(urlCollection,
                    tmpDir, name);
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to download the " + urlCollection + " layer",
                e);

            return null;
        }
    }

    /***********
     * perform the ie-intersection action
     *
     * @param intersection
     *            the data on which to perform the intersection
     * @return true in case no intersection is performed because any reason
     */

    public SimpleFeatureCollection intersection(Intersection intersection,
        String tmpDir)
    {

        // initialize variables
        String srcLayer = intersection.getSrcLayer();
        String trgLayer = intersection.getTrgLayer();
        String srcCodeField = intersection.getSrcCodeField();
        String trgCodeField = intersection.getTrgCodeField();
        String maskLayer = intersection.getMaskLayer();

        boolean isMasked = intersection.isMask();

        IntersectionMode mode = IntersectionMode.INTERSECTION;
        if (!intersection.isPreserveTrgGeom())
        {
            mode = IntersectionMode.SECOND;
        }

        // load feature collections from geoserver
        SimpleFeatureCollection srcCollection = null;
        SimpleFeatureCollection trgCollection = null;
        SimpleFeatureCollection maskCollection = null;
        String srcfilename = null;
        String trgfilename = null;
        String maskfilename = null;
        try
        {
            // try to load src collection

            LOGGER.info("download first geometry " + srcLayer + " " +
                srcCodeField);
            srcfilename = downloadFromGeoserver(srcLayer, tmpDir);
            LOGGER.info("finish download first geometry");
            srcCollection = SimpleFeatureCollectionByShp(srcfilename);
            // SimpleFeatureIterator iterator = srcCollection.features();
            // while (iterator.hasNext()) {
            // System.out.println("ID"+iterator.next().getID());
            // }
            // check if the src attribute exists and srcCollection is not empty
            if ((srcCollection == null) ||
                    (srcCollection.getSchema().getDescriptor(srcCodeField) == null))
            {
                LOGGER.error("Error downloading " + srcLayer + " or the " +
                    srcCodeField + " attribute does not exist");

                return null;
            }

            // try to load trg collection
            LOGGER.info("download second geometry " + trgLayer + " " +
                trgCodeField);
            trgfilename = downloadFromGeoserver(trgLayer, tmpDir);
            LOGGER.info("finish download second geometry");
            trgCollection = SimpleFeatureCollectionByShp(trgfilename);
            // check if the src attribute exists and trgCollection is not empty
            if ((trgCollection == null) ||
                    (trgCollection.getSchema().getDescriptor(trgCodeField) == null))
            {
                LOGGER.error("Error downloading " + trgLayer + " or the " +
                    trgCodeField + " attribute does not exist");

                return null;
            }

            // try to load mask collection
            if (isMasked)
            {
                LOGGER.info("download mask layer " + getName(maskLayer));
                maskfilename = downloadFromGeoserver(getName(maskLayer), tmpDir);
                LOGGER.info("mask layer " + maskLayer + " downloaded");
                maskCollection = SimpleFeatureCollectionByShp(maskfilename);

                if (maskCollection == null)
                {
                    LOGGER.error("Error downloading " + maskLayer);

                    return null;
                }
            }

        }
        catch (Throwable e)
        {
            LOGGER.error("Failed to load some layers", e);

            return null;

        }

        // check if intersection requires masking
        if (isMasked)
        {
            LOGGER.info("Mask flag is set on true");

            // generate the union of the mask geometries
            ClipProcess clipProcess = new ClipProcess();

            SimpleFeatureIterator sfi = maskCollection.features();
            Geometry maskGeometry = null;
            LOGGER.trace("Generating the union geometry");
            if (sfi.hasNext())
            {
                maskGeometry = (Geometry) sfi.next().getDefaultGeometry();
            }
            while (sfi.hasNext())
            {
                Geometry geometry = (Geometry) sfi.next().getDefaultGeometry();
                // maskGeometry = maskGeometry.union(geometry);
                maskGeometry = union(maskGeometry, geometry);
            }
            LOGGER.info("clipping the first layer");
            try
            {
                // clip the src Collection using the mask collection
                srcCollection = clipProcess.execute(srcCollection, maskGeometry);
            }
            catch (Exception e)
            {
                LOGGER.error("Exception when clipping the first layer", e);

                return null;
            }
            LOGGER.info("clipping the second layer");
            try
            {
                // clip the trg Collection using the mask collection
                trgCollection = clipProcess.execute(trgCollection, maskGeometry);
            }
            catch (Exception e)
            {
                LOGGER.error("Exception when clipping the second layer", e);

                return null;
            }

        }

        // setup for the IntersectionFeatureCollectionProcess
        List<String> srcAttributes = new ArrayList<String>();
        srcAttributes.add(srcCodeField);

        List<String> trgAttributes = new ArrayList<String>();

        trgAttributes.add(trgCodeField);

        // perform the IntersectionFeatureCollection process
        IntersectionFeatureCollection intersectionProcess = new IntersectionFeatureCollection();
        // long dif_beg = Long.parseLong(new Timestamp(new
        // java.util.Date().getTime()).toString());
        // LOGGER.trace(">>>>>>>>>>>>>>>>>>>>>>>>>>> trgCollection: intersectionProcess: >>>>>>"+this+"<<<>>>"+dif_beg+" <<<<<<<<<<");
        SimpleFeatureCollection result2 = null;
        try
        {
            result2 = intersectionProcess.execute(srcCollection, trgCollection,
                    srcAttributes, trgAttributes, mode, true, true);
        }
        catch (Exception e)
        {
            LOGGER.error("Exception when performing the intersection", e);

            return null;
        }
        // long dif_end = Long.parseLong(new Timestamp(new
        // java.util.Date().getTime()).toString());
        // LOGGER.trace(">>>>>>>>>>>>>>>>>>>>>>>>>>> trgCollection: intersectionProcess: >>>>>>"+this+"<<<>>>"+(dif_end-dif_beg)+" <<<<<<<<<<");
        LOGGER.info("Intersection process prepared");

        return result2;
    }

    public boolean initConnections(Geoserver geoserver)
    {
        try
        {
            // check if this control works as expected
            String url = geoserver.getGeoserverUrl(); // +"/geoserver";
            LOGGER.trace(url + " USER " + geoserver.getGeoserverUsername() +
                ", PWD(enc) " + geoserver.getGeoserverPassword());
            gsRestReader = new GeoServerRESTReader(url, geoserver.getGeoserverUsername(), PwEncoder.decode(geoserver.getGeoserverPassword()));

            if (!gsRestReader.existGeoserver())
            {
                return false;
            }

        }
        catch (Exception e)
        {
            LOGGER.error("Exception when initializing the connection", e);

            return false;
        }
        LOGGER.info("Connection successfully initialized");

        return true;
    }

    private String getName(String layername)
    {
        int i = layername.indexOf(":");
        if (i > 0)
        {
            return layername.substring(i + 1);
        }

        return layername;
    }

    /************
     * execute the intersections and update their status on the basis of the
     * content od the intersections table if status is DELETE then the
     * intersection will be removed if status is TOCOMPUTE then the intersection
     * will be executed, stored and published and then the STATUS will be
     * updated to COMPUTED otherwise no changes are applied to the intersection
     *
     * @param host
     *            where address the request of delete, update
     * @param intersections
     *            the intersections to parse
     * @return true everything goes well
     * @throws Exception
     */
    public synchronized boolean executeIntersectionStatements(String host,
        Config config, boolean simulate, String tmpdir)
    {
        OracleDataStoreManager dataStoreOracle = null;
        try
        {
            List<Intersection> intersections = null;

            try
            {
                Request.initIntersection();
                intersections = Request.getAllIntersections(host, getIeServiceUsername(), getIeServicePassword());
            }
            catch (MalformedURLException e)
            {
                LOGGER.error("Problems querying the list of intersections", e);

                // TODO Auto-generated catch block
                return false;
            }
            // check if this control works as expected
            if (intersections == null)
            {
                LOGGER.error("The list of the intersections is null, cannot continue");

                return false;
            }
            LOGGER.info("Updating intersections");

            // init of the DB connectio to the ORACLE datastore
            String dbHost = config.getGlobal().getDb().getHost();
            String schema = config.getGlobal().getDb().getSchema();
            String db = config.getGlobal().getDb().getDatabase();
            String user = config.getGlobal().getDb().getUser();
            String pwd = PwEncoder.decode(config.getGlobal().getDb().getPassword());
            int port = Integer.parseInt(config.getGlobal().getDb().getPort());

            try
            {
                dataStoreOracle = new OracleDataStoreManager(dbHost, port, db,
                        schema, user, pwd);
            }
            catch (Exception e1)
            {
                LOGGER.error(
                    "Problems creating the ORACLE datastore instance, check the parameters",
                    e1);

                return false;
            }

            for (Intersection intersection : intersections)
            {

                Status status = intersection.getStatus();
                String srcLayer = intersection.getSrcLayer();
                String trgLayer = intersection.getTrgLayer();
                String srcCode = intersection.getSrcCodeField();
                String trgCode = intersection.getTrgCodeField();
                long id = intersection.getId();

                // in case the intersection has been scheduled to be deleted,
                // delete the intersection from the list
                // and its intersection from the DB
                LOGGER.info(" Performing intersection command " + status);
                LOGGER.info("COMMAND: " + intersection);
                if (status == Status.TODELETE)
                {
                    try
                    {
                        dataStoreOracle.deleteAll(getName(srcLayer), getName(trgLayer), srcCode, trgCode);
                        Request.deleteIntersectionById(host, id, getIeServiceUsername(), getIeServicePassword());
                    }
                    catch (Exception e)
                    {
                        LOGGER.error(
                            "Problem deleting intersection from the database identified by " +
                            srcLayer + "," + trgLayer, e);
                    }
                    // still to implement
                }
                if (status == Status.TOCOMPUTE) // if the intersection should be
                                                // computed
                {
                    intersection.setStatus(Status.COMPUTING);
                    Request.updateIntersectionById(host, id, intersection, getIeServiceUsername(), getIeServicePassword());

                    SimpleFeatureCollection resultInt = null;
                    resultInt = intersection(intersection, tmpdir); // compute
                                                                    // the
                                                                    // intersection
                                                                    // between
                                                                    // the
                                                                    // layers

                    // SimpleFeatureIterator iterator = resultInt.features();
                    // while (iterator.hasNext()) {
                    // System.out.println("ID"+iterator.next().getID());
                    // }
                    String geometryType = "unknown";
                    try
                    {
                        if (resultInt != null)
                        {
                            geometryType =
                                resultInt.getSchema().getGeometryDescriptor().getType().getName().getLocalPart();
                        }
                        else
                        {
                            intersection.setStatus(Status.TOCOMPUTE);
                            Request.updateIntersectionById(host, id, intersection,getIeServiceUsername(),getIeServicePassword());
                        }
                    }
                    catch (Exception e)
                    {
                        intersection.setStatus(Status.TOCOMPUTE);
                        Request.updateIntersectionById(host, id, intersection,getIeServiceUsername(),getIeServicePassword());

                        LOGGER.error(
                            "Cannot identify the geometry type of the intersection result",
                            e);
                    }

                    // the intersection can be updated on the db only if the
                    // intersection generate a reuslt and it is Multipolygon
                    // typed
                    // else it must be deleted by both the db and by the
                    // intersection list
                    if ((resultInt != null) &&
                            geometryType.equals("MultiPolygon"))
                    {
                        // set the intersection to Computing. This is to avoid
                        // that a concurrent
                        // compute again the intersection
                        intersection.setStatus(Status.COMPUTING);
                        Request.updateIntersectionById(host, id, intersection, getIeServiceUsername(), getIeServicePassword());

                        try
                        {
                            LOGGER.info("Trying to store intersection result in ORACLE " +
                                schema +
                                ":" +
                                db +
                                " on " +
                                dbHost +
                                ":" +
                                port +
                                "(" +
                                user +
                                ",*****) ");
                            dataStoreOracle.perform(resultInt,
                                getName(srcLayer), getName(trgLayer),
                                srcCode, trgCode, itemsPerPage);
                            intersection.setStatus(Status.COMPUTED);
                            Request.updateIntersectionById(host, id, intersection, getIeServiceUsername(), getIeServicePassword());
                            LOGGER.info("Store operation successfully computed");
                        }
                        catch (Exception e)
                        {
                            // some problems occurred when saving the
                            // intersections in the db.
                            // in this case we schedule to delete this
                            // intersection and will be deleted at next action
                            LOGGER.error("Problem performing Intersection on " +
                                srcLayer + "," + trgLayer + "," + srcCode +
                                "," + trgCode, e);
                            intersection.setStatus(Status.TOCOMPUTE);
                            Request.updateIntersectionById(host, id, intersection, getIeServiceUsername(), getIeServicePassword());
                        }
                        finally
                        {
                            LOGGER.info("updating intersection " +
                                intersection);
                        }
                    }
                    else
                    {
                        LOGGER.error("Skipping intersection between " +
                            srcLayer +
                            " and " +
                            trgLayer +
                            " because the intersection cannot be computed");
                        LOGGER.error("Intersections will be deleted");
                        // Request.deleteIntersectionById(host, id,getIeServiceUsername(),getIeServicePassword());
                        intersection.setStatus(Status.TODELETE);
                        Request.updateIntersectionById(host, id, intersection, getIeServiceUsername(), getIeServicePassword());
                        try
                        {
                            dataStoreOracle.deleteAll(getName(srcLayer),
                                getName(trgLayer), srcCode, trgCode);
                        }
                        catch (IOException e)
                        {
                            LOGGER.error(
                                "Some problems occured deleting intersection from the database identified by " +
                                srcLayer + "," + trgLayer, e);
                        }
                    }
                }
            }
            LOGGER.info("Intersections updates: Successfull");

            return true;
        }
        finally
        {
            if (dataStoreOracle != null)
            {
                dataStoreOracle.close();
            }
        }
    }

	/**
     *
     * @param host
     * @return
     * @throws MalformedURLException
     */
    public List<Intersection> getIntersection(String host) throws MalformedURLException
    {
        return Request.getAllIntersections(host, getIeServiceUsername(), getIeServicePassword());
    }

    /**
     *
     * @return
     */
    public Config basicChecks()
    {
        Request.initConfig();

        Config config;
        try
        {
            LOGGER.info("Reading config information");
            config = Request.existConfig(host, getIeServiceUsername(), getIeServicePassword());
        }
        catch (MalformedURLException e)
        {
            LOGGER.error("Exception when reading config information", e);

            // TODO Auto-generated catch block
            return null;
        }
        // check if this control works as expected
        if (config == null)
        {
            LOGGER.error("Problems to find config information. ");

            return null;
        }
        LOGGER.info("Config information correctly read. Trying to connect to Geoserver on " +
            config.getGlobal().getGeoserver().getGeoserverUrl());
        // check the datastore and REST manager geoserver connections
        if (!initConnections(config.getGlobal().getGeoserver()))
        {
            LOGGER.error("Problems to find Geoserver. ");

            return null;
        }
        LOGGER.info("Geoserver found");

        return config;
    }

    /**
     * Removes TemplateModelEvents from the queue and put
     */
    public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException
    {
        host = conf.getPersistencyHost();
        itemsPerPage = conf.getItemsPerPages();
        LOGGER.info("*** Injected Setting *****");
        LOGGER.info("Persistence host " + host);
        LOGGER.info("Items Per Page " + itemsPerPage);
        LOGGER.info("ieServiceUsername: " + this.ieServiceUsername);
        LOGGER.info("ieServicePassword: " + this.ieServicePassword);
        LOGGER.info("**************************");

        // return
        final Queue<EventObject> ret = new LinkedList<EventObject>();

        while (events.size() > 0)
        {
            final EventObject ev;
            try
            {
                if ((ev = events.remove()) != null)
                {
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("ConfigAction.execute(): working on incoming event: " +
                            ev.getSource());
                    }

                    // FileSystemEvent fileEvent=(FileSystemEvent)ev;

                    // perform basic checks and return the current config in the
                    // DB
                    LOGGER.info("Trying to connect to " + host +
                        " and retrieve config data");

                    Config config = basicChecks();
                    if (config != null)
                    {
                        LOGGER.info("Config data correctly read");
                        geoserver = config.getGlobal().getGeoserver();
                        // create the figis temporary dir
                        LOGGER.info("Creating the temporary dir: " +
                            tmpDirName);

                        int randomIndex = generator.nextInt(Integer.MAX_VALUE);
                        File tmpDir = TmpDirManager.createTmpDir(tmpDirName +
                                System.getProperty("file.separator") +
                                Integer.toString(randomIndex));
                        LOGGER.info(tmpDirName + " successfully created");

                        // update the status of the intersections on the basis
                        // of the new input
                        boolean areIntersectionsUpdated = executeIntersectionStatements(
                                host, config, false, tmpDir.getAbsolutePath());
                        if (!areIntersectionsUpdated)
                        {
                            LOGGER.error("Problems occurred during the execution, check the log to see the reason");
                        }
                        // delete the tmpDir after execution
                        LOGGER.trace("Deleting the temporary dir: " +
                            tmpDirName);
                        TmpDirManager.deleteDir(tmpDir);
                        LOGGER.trace(tmpDirName + " successfully deleted");
                    }
                    else
                    {
                        LOGGER.error("Cannot read the configuration " + config +
                            ". Skip execution");
                    }

                    // add the event to the return
                    ret.add(ev);

                }
                else
                {
                    if (LOGGER.isErrorEnabled())
                    {
                        LOGGER.error("ConfigAction.execute(): Encountered a NULL event: SKIPPING...");
                    }

                    continue;
                }
            }
            catch (Exception ioe)
            {
                final String message = "ConfigAction.execute(): Unable to produce the output: " +
                    ioe.getLocalizedMessage();
                if (LOGGER.isErrorEnabled())
                {
                    LOGGER.error(message);
                }
                throw new ActionException(this, message);
            }
        }

        return ret;
    }
    

    public void setGeoserver(Geoserver geoserver)
    {
        this.geoserver = geoserver;
    }

    public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getIeServiceUsername() {
		return ieServiceUsername;
	}

	public void setIeServiceUsername(String ieServiceUsername) {
		this.ieServiceUsername = ieServiceUsername;
	}

	public String getIeServicePassword() {
		return ieServicePassword;
	}

	public void setIeServicePassword(String ieServicePassword) {
		this.ieServicePassword = ieServicePassword;
	}

}
