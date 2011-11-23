/*
 *  Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
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

import it.geosolutions.geobatch.figis.intersection.util.ZipStreamReader;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * @author Luca Paolino - luca.paolino@geo-solutions.it
 * 
 */


public class OracleDataStoreManagerCopy {
	
	static final Logger LOGGER = LoggerFactory.getLogger(OracleDataStoreManager.class);
    private URL url;
    private static int BATCH_SIZE =10;
    private static int OPERATOR_DIFFERENCE = 0;
    private static int OPERATOR_INTERSECTION = 1;
 
    public static String statsName = "fifao:statistical";
    public static String spatialName = "fifao:spatial";
    
    public static String statsTable = "STATISTICAL_TABLE";
    public static String spatialTable = "SPATIAL_TABLE";
    public static String statsTmpTable = "STATISTICAL_TMP_TABLE";
    public static String spatialTmpTable = "SPATIAL_TMP_TABLE";
    public static String listTable = "LISTINTERSECTIONS";
    
    private String source;
    private String target;
    private String mask;
    private Polygon dumbPolygon;
    private int size = 0;
    
    public DataStore orclDataStore = null;
    Transaction orclTransaction = null;
    Transaction orclTransactionTemp = null;
    Transaction orclTransactionDelete = null;

    SimpleFeatureType sfTmpGeom = null;
    SimpleFeatureType sfTmpStats = null;
    SimpleFeatureType sfGeom = null;
    
    // ////////////////////////////////////////////////////////////////////////
    //
    // ////////////////////////////////////////////////////////////////////////

    private static Map<String, Serializable> orclMap = new HashMap<String, Serializable>();

    private static String dbtype = "oracle";

    public int getSize(){
    	return this.size;
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
    
    private void initOrclMap (String hostname, Integer port, String database, String schema, String user, String password) {
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
     */
    private synchronized void initTables() {
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
		SimpleFeatureType sfStats = sftbStats.buildFeatureType();
		
		
		// init of the temp table for the list of geometries		
		SimpleFeatureTypeBuilder sftbTempGeoms = new SimpleFeatureTypeBuilder();
		
		sftbTempGeoms.setName(spatialTmpTable);
		sftbTempGeoms.add("GEOMETRY", MultiPolygon.class);		
		sftbTempGeoms.add("INTERSECTION_ID", String.class);
		sftbTempGeoms.setCRS(DefaultGeographicCRS.WGS84);

		 sfTmpGeom = sftbTempGeoms.buildFeatureType();		
		
		// init of the table for the list of geometries		
		SimpleFeatureTypeBuilder sftbGeoms = new SimpleFeatureTypeBuilder();
		
		sftbGeoms.setName(spatialTable);
		sftbGeoms.add("GEOMETRY", MultiPolygon.class);		
		sftbGeoms.add("INTERSECTION_ID", String.class);
		sftbGeoms.setCRS(DefaultGeographicCRS.WGS84);

		sfGeom = sftbGeoms.buildFeatureType();	
		

		// check if tables exist, if not create them
		try {
			String[] listTables = orclDataStore.getTypeNames();

			boolean statsTmpTableExists = false;
			boolean statsTableExists = false;			
			boolean spatialTmpTableExists = false;
			boolean spatialTableExists = false;			
			boolean intersectionListExist = false;
			for  (int i=0; i < listTables.length;i++) {
				if (listTables[i].equals(statsTmpTable)) 
					statsTmpTableExists = true;
				if (listTables[i].equals(statsTable)) 
					statsTableExists = true;	
				if (listTables[i].equals(spatialTable)) 
					spatialTableExists = true;					
				if (listTables[i].equals(spatialTmpTable)) 
					spatialTmpTableExists = true;
			}
			if (!statsTmpTableExists) {
				LOGGER.trace("Creatinging Temp Statistic table");
				orclDataStore.createSchema(sfTmpStats);
				LOGGER.trace("Temp Statistic table created");
			}
			if (!statsTableExists) {
				LOGGER.trace("Creatinging Statistic table");
				orclDataStore.createSchema(sfStats);
				LOGGER.trace("Statistic table created");
			}			
			if (!spatialTableExists) {
				LOGGER.trace("Creating Spatial table");
				orclDataStore.createSchema(sfGeom);
				LOGGER.trace("Spatial table created");
			}			
			if (!spatialTmpTableExists) {
				LOGGER.trace("Creating Spatial Temp table");	
				orclDataStore.createSchema(sfTmpGeom);
				LOGGER.trace("Spatial Temp table created");
			}
			

		} catch(Exception e) {
			LOGGER.trace("Problems creating the ORACLE tables"+e.getMessage());
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
     * @throws IOException
     */
    public boolean actionTemp(DataStore ds, Transaction tx, SimpleFeatureCollection collection, String srcLayer, String trgLayer, String srcCode, String trgCode) throws IOException {
        cleanTemp(tx);
    	saveToTemp(ds, tx, collection,srcLayer, trgLayer, srcLayer+"_"+srcCode, trgLayer+"_"+trgCode);
    	return true;
    }
    /*********
     * delete all the instances of srcLayer and trgLayer from the permanent tables
     * copy the intersections and the stats temporary information into the permanent tables
     * @param ds
     * @param tx
     * @param srcLayer
     * @param trgLayer
     * @throws IOException
     */
    public void action(DataStore ds, Transaction tx,  String srcLayer, String trgLayer) throws IOException {
    		
   		deleteOldInstancesFromPermanent(tx, srcLayer, trgLayer);
   		
 	
      	saveToPermanent(tx, statsTmpTable,statsTable);
       	
      	
    	saveToPermanent(tx, spatialTmpTable,spatialTable);

    	
    }
    
    
    
    
    /******
     * copy the intersections and the stats temporary information into the permanent tables
     * @param tx
     * @param tableFrom
     * @param tableTo
     * @throws IOException
     */
    private void saveToPermanent(Transaction tx, String tableFrom, String tableTo) throws IOException {
    	SimpleFeatureSource sfs = orclDataStore.getFeatureSource(tableFrom);
    	
    	FeatureStore featureStore = (FeatureStore)orclDataStore.getFeatureSource(tableTo);
    	featureStore.setTransaction(tx);
    	featureStore.addFeatures(sfs.getFeatures());
    }
    /*********
     * manage the delete all the information about the srcLayer and trgLayer pair from the permanent tables from a transaction
     * @param srcLayer
     * @param trgLayer
     * @throws IOException
     */
    public void deleteAll(String srcLayer, String trgLayer) throws IOException {
		try {
			
			deleteOldInstancesFromPermanent(orclTransactionDelete, srcLayer, trgLayer);
			orclTransactionDelete.commit();
		} catch(Throwable e ) {
			LOGGER.trace("Problems deleting all "+e.getMessage());
			orclTransactionDelete.rollback();
		}
	
    }
    
    
    /*********
     * delete all the information about the srcLayer and trgLayer pair from the permanent tables
     * @param tx
     * @param srcLayer
     * @param trgLayer
     * @throws IOException
     */
    public void deleteOldInstancesFromPermanent(Transaction tx,String srcLayer, String trgLayer) throws IOException {
    	
    	FeatureStore featureStoreData = (FeatureStore)orclDataStore.getFeatureSource(statsTable);
    	FeatureStore featureStoreGeom = (FeatureStore)orclDataStore.getFeatureSource(spatialTable);
    	featureStoreData.setTransaction(tx);  
    	featureStoreGeom.setTransaction(tx);  
    	final FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
    	Filter filter1 = ff.equals( ff.property( "SRCLAYER"), ff.literal( srcLayer ) );
    	Filter filter2 = ff.equals( ff.property( "TRGLAYER"), ff.literal( trgLayer ) );
    	Filter filterAnd = ff.and(filter1, filter2);
    	SimpleFeatureIterator iterator = (SimpleFeatureIterator) featureStoreData.getFeatures(filterAnd).features();
    
    	while (iterator.hasNext()) {

    		String id = (String) iterator.next().getAttribute("INTERSECTION_ID");
    		Filter filter = ff.equals( ff.property( "INTERSECTION_ID"), ff.literal( id ) );
    		featureStoreGeom.removeFeatures(filter);
    		featureStoreData.removeFeatures(filter);
    	}
    	
    }
    
    /**********
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
    public void perform(SimpleFeatureCollection collection, String srcLayer,  String trgLayer,  String srcCode,  
    								String trgCode) throws IOException {
		if (orclDataStore != null) {
			boolean resultTempCreate = false;
			try {
				resultTempCreate = actionTemp(orclDataStore, orclTransactionTemp, collection, srcLayer,  trgLayer,  srcCode,  trgCode);
				orclTransactionTemp.commit();
			} catch(Throwable e ) {
				System.out.println("eccezione");
				e.printStackTrace();
				orclTransactionTemp.rollback();
			}
			try {
				if (resultTempCreate==true) {
					action(orclDataStore, orclTransaction,   srcLayer,  trgLayer);
					orclTransaction.commit();
				}
			} catch(Exception e ) {
				System.out.println("eccezione");
				e.printStackTrace();
				orclTransaction.rollback();
			}

		}
    	
    	
    }
    
    /***
     * close the transactions and the datastore
     */
    public void close(){
		try {
			orclTransactionTemp.close();
			orclTransaction.close();
			orclTransactionDelete.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			orclDataStore.dispose();
		}
    }
       
    /*
     * Accepts params for connecting to the Oracle datastore
     */
    public OracleDataStoreManagerCopy(String hostname, Integer port, String database,
        String schema, String user, String password) throws Exception {
	
	    initOrclMap(hostname, port, database, schema, user, password);
	    
		//create the connection to the oracle datastore
		orclDataStore = prepareORCLDataStore();
		orclTransaction = new DefaultTransaction();
		orclTransactionTemp = new DefaultTransaction();
		orclTransactionDelete = new DefaultTransaction();
		initTables();
		
	}

    public void cleanTemp(Transaction tx) throws IOException {
    	FeatureStore featureStoreData = (FeatureStore)orclDataStore.getFeatureSource(spatialTmpTable);
    	FeatureStore featureStoreGeom = (FeatureStore)orclDataStore.getFeatureSource(statsTmpTable);

    	featureStoreData.setTransaction(tx);
    	featureStoreGeom.setTransaction(tx);
    	featureStoreData.removeFeatures(Filter.INCLUDE);
    	featureStoreGeom.removeFeatures(Filter.INCLUDE);
    	
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
     * @throws IOException
     */
    public boolean saveToTemp(DataStore ds, Transaction tx, SimpleFeatureCollection source, String srcLayer, String trgLayer, String srcCode, String trgCode) throws IOException {

    	SimpleFeatureIterator iterator = null;
    	try {
	    	if (source==null) return false;
	    	//initialize CRS transformation. It is performed in case source CRS is different from WGS84
	    	CoordinateReferenceSystem sourceCRS = source.getSchema().getCoordinateReferenceSystem();
	    	String geomName = source.getSchema().getGeometryDescriptor().getLocalName();
	    	CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;

	    	MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

	    	iterator = source.features();
	      	
	    	SimpleFeatureCollection sfcData = FeatureCollections.newCollection();
	    	SimpleFeatureCollection sfcGeom = FeatureCollections.newCollection();
	    	SimpleFeatureBuilder  featureBuilderData = new SimpleFeatureBuilder(sfTmpStats); 
	    	SimpleFeatureBuilder  featureBuilderGeom = new SimpleFeatureBuilder(sfTmpGeom); 
	    	int i=0;

	    	// follow the iterator of the resulting set and divide information into two collection: geometrical and statistical
	        while (iterator.hasNext()) {
	    		try {
	    		String intersectionID = srcLayer+"_"+trgLayer+"_"+(i++);
	    		
	    		SimpleFeature sf = iterator.next();
	    		
	    		featureBuilderData.set("SRCODE", sf.getAttribute(srcCode));

	    		featureBuilderData.set("SRCLAYER", srcLayer);

	    		featureBuilderData.set("TRGLAYER", trgLayer);  

	    		featureBuilderData.set("TRGCODE", sf.getAttribute(trgCode));

	    		if(sf.getAttribute("areaA")!=null)featureBuilderData.set("SRCAREA", sf.getAttribute("areaA"));
	    		if(sf.getAttribute("areaB")!=null) featureBuilderData.set("TRGAREA", sf.getAttribute("areaB"));
	    		if(sf.getAttribute("percentageA")!=null) featureBuilderData.set("SRCOVLPCT", sf.getAttribute("percentageA"));
	    	    if(sf.getAttribute("percentageB")!=null)featureBuilderData.set("TRGOVLPCT", sf.getAttribute("percentageB"));

	    		featureBuilderData.set("INTERSECTION_ID", intersectionID);
	    		SimpleFeature sfwData = featureBuilderData.buildFeature(intersectionID);
	    		sfcData.add(sfwData);
	
	    		MultiPolygon geometry = (MultiPolygon) sf.getAttribute(geomName);

		    	MultiPolygon targetGeometry = (MultiPolygon) JTS.transform( geometry, transform);
		    	targetGeometry.setSRID(4326);

	    		featureBuilderGeom.set("GEOMETRY",targetGeometry );
	    		featureBuilderGeom.set("INTERSECTION_ID", intersectionID);
	    		SimpleFeature sfwGeom = featureBuilderGeom.buildFeature(intersectionID);
	    		sfcGeom.add(sfwGeom);
	    		} catch(Exception e) {
	    			e.printStackTrace();
	    		}
	    	}
	    	

	    	
	
	    	FeatureStore featureStoreData = (FeatureStore)ds.getFeatureSource(statsTmpTable);
	    	featureStoreData.setTransaction(tx);
	    	// save statistics to the statistics temporary table
	    	featureStoreData.addFeatures(sfcData);
	    	
	    	FeatureStore featureStoreGeom = (FeatureStore)ds.getFeatureSource(spatialTmpTable);
	    	featureStoreGeom.setTransaction(tx);
	    	// save geometries to the statistics temporary table	
	    	featureStoreGeom.addFeatures(sfcGeom);

    		return true;
   	} catch(Throwable e) {
    		e.printStackTrace();
    	}
    	finally {
    	
    		iterator.close();
    	}
    	return false;
    }
    
  
 


	/**
	 * create the ORACLE datastore
	 * @return
	 */
	private DataStore prepareORCLDataStore() {
		// get a feature writer
		DataStore orclDataStore;
		try {
			 orclDataStore = new OracleNGDataStoreFactory().createDataStore(orclMap);
		} catch (IOException e) {

			e.printStackTrace();
			return null;
		}
		
		return orclDataStore;
	}
	


	

	    
	

}

