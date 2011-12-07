package it.geosolutions.geobatch.figis.intersection;


import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.MultiPolygon;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.oracle.OracleNGDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.geotools.jdbc.JDBCDataStoreFactory.DATABASE;
import static org.geotools.jdbc.JDBCDataStoreFactory.DBTYPE;
import static org.geotools.jdbc.JDBCDataStoreFactory.HOST;
import static org.geotools.jdbc.JDBCDataStoreFactory.MAXCONN;
import static org.geotools.jdbc.JDBCDataStoreFactory.MAXWAIT;
import static org.geotools.jdbc.JDBCDataStoreFactory.MINCONN;
import static org.geotools.jdbc.JDBCDataStoreFactory.PASSWD;
import static org.geotools.jdbc.JDBCDataStoreFactory.PORT;
import static org.geotools.jdbc.JDBCDataStoreFactory.SCHEMA;
import static org.geotools.jdbc.JDBCDataStoreFactory.USER;
import static org.geotools.jdbc.JDBCDataStoreFactory.VALIDATECONN;


public class OracleDataStoreManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleDataStoreManager.class);


    public static String statsTable = "STATISTICAL_TABLE";
    public static String spatialTable = "SPATIAL_TABLE";
    public static String statsTmpTable = "STATISTICAL_TMP_TABLE";
    public static String spatialTmpTable = "SPATIAL_TMP_TABLE";
    public static String listTable = "LISTINTERSECTIONS";

    // ////////////////////////////////////////////////////////////////////////
    //
    // ////////////////////////////////////////////////////////////////////////

    private static Map<String, Serializable> orclMap = new HashMap<String, Serializable>();

    private static String dbtype = "oracle";


    SimpleFeatureType sfTmpGeom = null;
    SimpleFeatureType sfTmpStats = null;
    SimpleFeatureType sfStats = null;
    SimpleFeatureType sfGeom = null;

    /*
     * Accepts params for connecting to the Oracle datastore
     */
    public OracleDataStoreManager(String hostname, Integer port, String database,
        String schema, String user, String password) throws Exception
    {

        DataStore orclDataStore = null;
        Transaction orclTransaction = null;
        try
        {
            initOrclMap(hostname, port, database, schema, user, password);

            orclDataStore = initOracleDataStore();
            orclTransaction = new DefaultTransaction();
            initTables(orclDataStore, orclTransaction);
            orclTransaction.commit();
        }
        catch (Exception e)
        {
            LOGGER.error("Exception creating tables in ORACLE", e);
            orclTransaction.rollback();
            throw new Exception("Error creating tables in ORACLE", e);
        }
        finally
        {
            close(orclDataStore, orclTransaction);
        }

    }


    /**
     * @return
     * @throws Exception
     */
    private DataStore initOracleDataStore() throws Exception
    {
        try
        {
            DataStore orclDataStore = new OracleNGDataStoreFactory().createDataStore(orclMap);

            return orclDataStore;
        }
        catch (Exception e)
        {
            LOGGER.error("Error creating the ORACLE data store, check the connection parameters", e);
            throw new Exception("Error creating the ORACLE data store, check the connection parameters", e);
        }
    }


    /***********
     * initialize connection parameters to the Oracle dataStore
     * @param hostname
     * @param port
     * @param database
     * @param schema
     * @param user
     * @param password
     */

    private void initOrclMap(String hostname, Integer port, String database, String schema, String user,
        String password)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        orclMap.put(DBTYPE.key, dbtype);
        orclMap.put(HOST.key, hostname);
        orclMap.put(PORT.key, port);
        orclMap.put(DATABASE.key, database);
        orclMap.put(SCHEMA.key, schema);
        orclMap.put(USER.key, user);
        orclMap.put(PASSWD.key, password);
        orclMap.put(MINCONN.key, 1);
        orclMap.put(MAXCONN.key, 10);
        orclMap.put(MAXWAIT.key, 100000);
        orclMap.put(VALIDATECONN.key, true);

    }


    /************
     * initialize the tables: create them if they do not exist
     * @param tx
     * @throws IOException
     */
    private void initTables(DataStore ds, Transaction tx) throws Exception
    {
        // init of the Temp table for stats
        SimpleFeatureTypeBuilder sftbTmpStats = new SimpleFeatureTypeBuilder();
        sftbTmpStats.setName(statsTmpTable);
        sftbTmpStats.add("INTERSECTION_ID", String.class);
        sftbTmpStats.add("SRCODE", String.class);
        sftbTmpStats.add("TRGCODE", String.class);
        sftbTmpStats.add("SRCLAYER", String.class);
        sftbTmpStats.add("TRGLAYER", String.class);
        sftbTmpStats.add("SRCAREA", String.class);
        sftbTmpStats.add("TRGAREA", String.class);
        sftbTmpStats.add("SRCOVLPCT", String.class);
        sftbTmpStats.add("TRGOVLPCT", String.class);
        sftbTmpStats.add("SRCCODENAME", String.class);
        sftbTmpStats.add("TRGCODENAME", String.class);
        sfTmpStats = sftbTmpStats.buildFeatureType();


        // init of the permanent table for stats
        SimpleFeatureTypeBuilder sftbStats = new SimpleFeatureTypeBuilder();
        sftbStats.setName(statsTable);
        sftbStats.add("INTERSECTION_ID", String.class);
        sftbStats.add("SRCODE", String.class);
        sftbStats.add("TRGCODE", String.class);
        sftbStats.add("SRCLAYER", String.class);
        sftbStats.add("TRGLAYER", String.class);
        sftbStats.add("SRCAREA", String.class);
        sftbStats.add("TRGAREA", String.class);
        sftbStats.add("SRCOVLPCT", String.class);
        sftbStats.add("TRGOVLPCT", String.class);
        sftbStats.add("SRCCODENAME", String.class);
        sftbStats.add("TRGCODENAME", String.class);
        sfStats = sftbStats.buildFeatureType();


        // init of the temp table for the list of geometries
        SimpleFeatureTypeBuilder sftbTempGeoms = new SimpleFeatureTypeBuilder();

        sftbTempGeoms.setName(spatialTmpTable);
        sftbTempGeoms.add("THE_GEOM", MultiPolygon.class);
        sftbTempGeoms.add("INTERSECTION_ID", String.class);


        sftbTempGeoms.setCRS(DefaultGeographicCRS.WGS84);

        sfTmpGeom = sftbTempGeoms.buildFeatureType();

        // init of the table for the list of geometries
        SimpleFeatureTypeBuilder sftbGeoms = new SimpleFeatureTypeBuilder();

        sftbGeoms.setName(spatialTable);
        sftbGeoms.add("THE_GEOM", MultiPolygon.class);
        sftbGeoms.add("INTERSECTION_ID", String.class);
        sftbGeoms.setCRS(DefaultGeographicCRS.WGS84);

        sfGeom = sftbGeoms.buildFeatureType();


        // check if tables exist, if not create them

        String[] listTables = ds.getTypeNames();

        boolean statsTmpTableExists = false;
        boolean statsTableExists = false;
        boolean spatialTmpTableExists = false;
        boolean spatialTableExists = false;
        for (int i = 0; i < listTables.length; i++)
        {
            if (listTables[i].equals(statsTmpTable))
            {
                statsTmpTableExists = true;
            }
            if (listTables[i].equals(statsTable))
            {
                statsTableExists = true;
            }
            if (listTables[i].equals(spatialTable))
            {
                spatialTableExists = true;
            }
            if (listTables[i].equals(spatialTmpTable))
            {
                spatialTmpTableExists = true;
            }
        }
        if (!statsTmpTableExists)
        {
            ds.createSchema(sfTmpStats);
        }
        if (!statsTableExists)
        {
            ds.createSchema(sfStats);
        }
        if (!spatialTableExists)
        {
            ds.createSchema(sfGeom);
        }
        if (!spatialTmpTableExists)
        {
            ds.createSchema(sfTmpGeom);
        }
    }

    /***********
     * delete all from temporary tables and then save intersections in them
     * @param ds
     * @param tx
     * @param collection
     * @param srcLayer
     * @param trgLayer
     * @param srcCode
     * @param trgCode
     * @return
     * @throws Exception
     */
    private void actionTemp(DataStore ds, Transaction tx, SimpleFeatureCollection collection, String srcLayer,
        String trgLayer, String srcCode, String trgCode, int itemsPerPage) throws Exception
    {
        cleanTemp(ds, tx);
        saveToTemp(ds, tx, collection, srcLayer, trgLayer, srcCode, trgCode, itemsPerPage);
    }

    /*********
     * delete all the instances of srcLayer and trgLayer from the permanent tables
     * copy the intersections and the stats temporary information into the permanent tables
     * @param ds
     * @param tx
     * @param srcLayer
     * @param trgLayer
     * @throws Exception
     */
    private void action(DataStore ds, Transaction tx, String srcLayer, String trgLayer, String srcLayerCode,
        String trgLayerCode) throws Exception
    {
        deleteOldInstancesFromPermanent(ds, tx, srcLayer, trgLayer, srcLayerCode, trgLayerCode);
        saveToPermanent(ds, tx, statsTmpTable, statsTable);
        saveToPermanent(ds, tx, spatialTmpTable, spatialTable);
    }


    /******
     * copy the intersections and the stats temporary information into the permanent tables
     * @param tx
     * @param tableFrom
     * @param tableTo
     * @param ds
     * @throws IOException
     */
    private void saveToPermanent(DataStore ds, Transaction tx, String tableFrom, String tableTo) throws IOException
    {
        LOGGER.info("Saving data from permanent table " + tableFrom + " to " + tableTo);

        SimpleFeatureSource sfs = ds.getFeatureSource(tableFrom);
        FeatureStore featureStore = (FeatureStore) ds.getFeatureSource(tableTo);
        featureStore.setTransaction(tx);
        try
        {
            featureStore.addFeatures(sfs.getFeatures());
        }
        finally
        {
            if (featureStore != null)
            {
                featureStore.getDataStore().dispose();
            }
        }
    }

    /*********
     * delete all the information about the srcLayer and trgLayer pair from the permanent tables
     * @param tx
     * @param srcLayer
     * @param trgLayer
     * @param trgLayerCode
     * @param srcLayerCode
     * @param ds
     * @throws IOException
     */
    private void deleteOldInstancesFromPermanent(DataStore ds, Transaction tx, String srcLayer, String trgLayer,
        String srcLayerCode, String trgLayerCode) throws Exception
    {
        LOGGER.info("Deleting old instances of the intersection between " + srcLayer + " and " + trgLayer);

        FeatureStore featureStoreData = null;
        FeatureStore featureStoreGeom = null;
        SimpleFeatureIterator iterator = null;

        try
        {
            featureStoreData = (FeatureStore) ds.getFeatureSource(statsTable);
            featureStoreGeom = (FeatureStore) ds.getFeatureSource(spatialTable);
            featureStoreData.setTransaction(tx);
            featureStoreGeom.setTransaction(tx);

            final FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
            Filter filter1 = ff.equals(ff.property("SRCLAYER"), ff.literal(srcLayer));
            Filter filter2 = ff.equals(ff.property("TRGLAYER"), ff.literal(trgLayer));
            Filter filter3 = ff.equals(ff.property("SRCCODENAME"), ff.literal(srcLayerCode));
            Filter filter4 = ff.equals(ff.property("TRGCODENAME"), ff.literal(trgLayerCode));
            Filter filterAnd = ff.and(Arrays.asList(filter1, filter2, filter3, filter4));
            iterator = (SimpleFeatureIterator) featureStoreData.getFeatures(filterAnd).features();

            while (iterator.hasNext())
            {
                String id = (String) iterator.next().getAttribute("INTERSECTION_ID");
                LOGGER.debug("Cascade delete of " + id);

                Filter filter = ff.equals(ff.property("INTERSECTION_ID"), ff.literal(id));
                featureStoreGeom.removeFeatures(filter);
                featureStoreData.removeFeatures(filter);
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            if (featureStoreGeom != null)
            {
                featureStoreGeom.getDataStore().dispose();
            }
            if (featureStoreData != null)
            {
                featureStoreData.getDataStore().dispose();
            }
            if (iterator != null)
            {
                iterator.close();
            }
        }

    }

    /***
     * close the transactions and the datastore
     * @param tx
     * @param ds
     */
    private void close(DataStore ds, Transaction tx)
    {
        try
        {
            if (tx != null)
            {
                tx.close();
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Exception closing the transaction", e);
        }
        finally
        {
            if (ds != null)
            {
                ds.dispose();
            }
        }
    }

    private void cleanTemp(DataStore ds, Transaction tx) throws IOException
    {
        LOGGER.info("Cleaning temp table");

        FeatureStore featureStoreData = null;
        FeatureStore featureStoreGeom = null;
        try
        {
            featureStoreData = (FeatureStore) ds.getFeatureSource(spatialTmpTable);
            featureStoreGeom = (FeatureStore) ds.getFeatureSource(statsTmpTable);

            featureStoreData.setTransaction(tx);
            featureStoreGeom.setTransaction(tx);
            featureStoreData.removeFeatures(Filter.INCLUDE);
            featureStoreGeom.removeFeatures(Filter.INCLUDE);
        }
        finally
        {
            if (featureStoreGeom != null)
            {
                featureStoreGeom.getDataStore().dispose();
            }
            if (featureStoreData != null)
            {
                featureStoreData.getDataStore().dispose();
            }
        }
    }

    /*****
     * perform the intersections, split the results into the two temporary tables
     * @param ds
     * @param tx
     * @param source
     * @param srcLayer
     * @param trgLayer
     * @param srcCode
     * @param trgCode
     * @return
     * @throws Exception
     */
    private void saveToTemp(DataStore ds, Transaction tx, SimpleFeatureCollection source, String srcLayer,
        String trgLayer, String srcCode, String trgCode, int itemsPerPage) throws Exception
    {
        itemsPerPage = (itemsPerPage == 0) ? 50 : itemsPerPage;

        LOGGER.info("Saving intersections between " + srcLayer + " and " + trgLayer + " into temporary table");
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Attributes " + srcCode + " and " + trgCode);
            LOGGER.debug("Items per page " + itemsPerPage);
        }

        FeatureStore featureStoreData = null;
        FeatureStore featureStoreGeom = null;
        SimpleFeatureIterator iterator = null;
        try
        {
            if (source == null)
            {
                throw new Exception("Source cannot be null");
            }

            // initialize CRS transformation. It is performed in case source CRS is different from WGS84
            CoordinateReferenceSystem sourceCRS = source.getSchema().getCoordinateReferenceSystem();
            String geomName = source.getSchema().getGeometryDescriptor().getLocalName();
            CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;

            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

            iterator = source.features();

            SimpleFeatureBuilder featureBuilderData = new SimpleFeatureBuilder(sfTmpStats);
            SimpleFeatureBuilder featureBuilderGeom = new SimpleFeatureBuilder(sfTmpGeom);
            int i = 0;

            featureStoreData = (FeatureStore) ds.getFeatureSource(statsTmpTable);
            featureStoreData.setTransaction(tx);

            featureStoreGeom = (FeatureStore) ds.getFeatureSource(spatialTmpTable);
            featureStoreGeom.setTransaction(tx);

            // follow the iterator of the resulting set and divide information into two collection: geometrical and statistical
            boolean iteratorHasFinished = false;
            // this cycle is necessary to save itemsPerPage items at time
            int page = 0;

            while (!iteratorHasFinished)
            {
                LOGGER.debug("PAGE : " + (page++));

                SimpleFeatureCollection sfcData = FeatureCollections.newCollection();
                SimpleFeatureCollection sfcGeom = FeatureCollections.newCollection();
                boolean save = false;
                while (iterator.hasNext() && !save)
                {
                    i++;
                    if ((i % itemsPerPage) == 0)
                    {
                        save = true;
                    }
                    // LOGGER.debug("SAVING "+save);

                    // String intersectionID = srcLayer+"_"+trgLayer+"_"+i;
                    String intersectionID = srcLayer + "_" + srcCode + "_" + trgLayer + "_" + trgCode + i;
                    SimpleFeature sf = iterator.next();

                    featureBuilderData.set("SRCODE", sf.getAttribute(srcCode));

                    featureBuilderData.set("SRCLAYER", srcLayer);

                    featureBuilderData.set("TRGLAYER", trgLayer);

                    featureBuilderData.set("TRGCODE", sf.getAttribute(trgCode));

                    featureBuilderData.set("INTERSECTION_ID", intersectionID);
                    featureBuilderData.set("SRCCODENAME", srcCode);
                    featureBuilderData.set("TRGCODENAME", trgCode);

                    LOGGER.debug("INTERSECTION_ID : " + intersectionID);
                    // LOGGER.debug("SRCLAYER : "+srcLayer+", SRCCODE : "+sf.getAttribute(srcCode));
                    // LOGGER.debug("TRGLAYER : "+trgLayer+", TRGCODE : "+sf.getAttribute(trgCode));

                    if (sf.getAttribute("areaA") != null)
                    {
                        featureBuilderData.set("SRCAREA", sf.getAttribute("areaA"));
                        // LOGGER.debug("SRCAREA : "+sf.getAttribute("areaA"));
                    }
                    if (sf.getAttribute("areaB") != null)
                    {
                        featureBuilderData.set("TRGAREA", sf.getAttribute("areaB"));
                        // LOGGER.debug("TRGAREA : "+sf.getAttribute("areaB"));
                    }
                    if (sf.getAttribute("percentageA") != null)
                    {
                        featureBuilderData.set("SRCOVLPCT", sf.getAttribute("percentageA"));
                        // LOGGER.debug("SRCOVLPCT : "+sf.getAttribute("percentageA"));
                    }
                    if (sf.getAttribute("percentageB") != null)
                    {
                        featureBuilderData.set("TRGOVLPCT", sf.getAttribute("percentageB"));
                        // LOGGER.debug("TRGOVLPCT : "+sf.getAttribute("percentageB"));
                    }

                    SimpleFeature sfwData = featureBuilderData.buildFeature(intersectionID);
                    sfcData.add(sfwData);

                    MultiPolygon geometry = (MultiPolygon) sf.getAttribute(geomName);

                    MultiPolygon targetGeometry = (MultiPolygon) JTS.transform(geometry, transform);
                    targetGeometry.setSRID(4326);

                    featureBuilderGeom.set("THE_GEOM", targetGeometry);
                    featureBuilderGeom.set("INTERSECTION_ID", intersectionID);

                    SimpleFeature sfwGeom = featureBuilderGeom.buildFeature(intersectionID);
                    // LOGGER.debug("INTERSECTION_ID : "+intersectionID+", GEOMETRY : "+targetGeometry);
                    sfcGeom.add(sfwGeom);
                }

                // save statistics to the statistics temporary table
                featureStoreData.addFeatures(sfcData);
                // save geometries to the statistics temporary table
                featureStoreGeom.addFeatures(sfcGeom);
                if (save == false)
                {
                    iteratorHasFinished = true;
                }
            }
        }
        finally
        {
            if (featureStoreGeom != null)
            {
                featureStoreGeom.getDataStore().dispose();
            }
            if (featureStoreData != null)
            {
                featureStoreData.getDataStore().dispose();
            }
            if (iterator != null)
            {
                iterator.close();
            }
        }
    }

    /********************************************************************************************************************/

    /**
     * recall the steps from updating the database
     * firstly delete all the information from temp tables
     * then, perform the intersection, update temporary and finally update permanent
     * @param collection
     * @param srcLayer
     * @param trgLayer
     * @param srcCode
     * @param trgCode
     * @throws IOException
     */
    public void saveAll(SimpleFeatureCollection collection, String srcLayer,
        String trgLayer, String srcCode,
        String trgCode, int itemsPerPage) throws Exception
    {

        boolean res = false;

        DataStore ds = null;
        Transaction tx = null;
        try
        {
            ds = initOracleDataStore();
            tx = new DefaultTransaction();
            if (ds != null)
            {
                LOGGER.info("Performing data saving: SRCLAYER " + srcLayer + ", SRCCODE " + srcCode + ", TRGLAYER " + trgLayer + ", TRGLAYER " + trgCode);
                actionTemp(ds, tx, collection, srcLayer, trgLayer, srcCode, trgCode,
                    itemsPerPage);
                tx.commit();
                res = true;
            }
            else
            {
                LOGGER.error("The collection cannot be null");
                res = false;
                throw new IOException("The collection cannot be null ");
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Exception during ORACLE saving. Rolling back ", e);
            tx.rollback();
            throw new IOException("Exception during ORACLE saving. Rolling back ", e);
        }
        finally
        {
            close(ds, tx);
        }

        try
        {
            ds = initOracleDataStore();
            tx = new DefaultTransaction();
            if (res && (ds != null))
            {
                action(ds, tx, srcLayer, trgLayer, srcCode, trgCode);
                tx.commit();
            }
            else
            {
                LOGGER.error("The collection cannot be null");
                res = false;
                throw new IOException("The collection cannot be null ");
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Exception during ORACLE saving. Rolling back ", e);
            tx.rollback();
            throw new IOException("Exception during ORACLE saving. Rolling back ", e);
        }
        finally
        {
            close(ds, tx);
        }
    }

    /**
     * manage the delete all the information about the srcLayer and trgLayer pair from the permanent tables from a transaction
     * @param srcLayer
     * @param trgLayer
     * @throws IOException
     */
    public void deleteAll(String srcLayer, String trgLayer, String srcLayerCode, String trgLayerCode) throws IOException
    {
        DataStore orclDatastore = null;
        Transaction orclTransaction = null;
        try
        {
            orclDatastore = initOracleDataStore();
            orclTransaction = new DefaultTransaction();
            deleteOldInstancesFromPermanent(orclDatastore, orclTransaction, srcLayer, trgLayer, srcLayerCode,
                trgLayerCode);
            orclTransaction.commit();
        }
        catch (Exception e)
        {
            LOGGER.error("Exception performing the delete all " + srcLayer + ", " + trgLayer, e);
            orclTransaction.rollback();
            throw new IOException("Delete all raised an exception ", e);
        }
        finally
        {
            close(orclDatastore, orclTransaction);
        }

    }

}
