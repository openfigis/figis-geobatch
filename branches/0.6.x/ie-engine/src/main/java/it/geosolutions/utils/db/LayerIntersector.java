package it.geosolutions.utils.db;

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

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.v1_0_0.WFS_1_0_0_DataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;

@SuppressWarnings("unused")
public class LayerIntersector {
    private URL url;
    private static int BATCH_SIZE =10;
    private static int OPERATOR_DIFFERENCE = 0;
    private static int OPERATOR_INTERSECTION = 1;
    private static final Logger LOGGER = Logger.getLogger(LayerIntersector.class);
    public static String statsName = "fifao:statistical";
    public static String statsTable = "STATISTICAL_TABLE";
    public static String spatialName = "fifao:spatial";
    public static String spatialTable = "SPATIAL_TABLE";
    private String source;
    private String target;
    private String mask;
    private Polygon dumbPolygon;
    private int size = 0;
	// ////////////////////////////////////////////////////////////////////////
    //
    // ////////////////////////////////////////////////////////////////////////

    private static Map<String, Serializable> orclMap = new HashMap<String, Serializable>();

    private static String dbtype = "oracle";

    public int getSize(){
    	return this.size;
    }
    
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
    
    public LayerIntersector(URL wfsURL, String source, String target,
            String sourceMask, String targetMask, String sourceCode,
            String targetCode, String hostname, Integer port,
            String database, String schema, String user,
            String password, Long combination) throws Exception {
        this(wfsURL, source, target, sourceMask, targetMask, sourceCode, targetCode,
    	        hostname, port, database, schema, user, password, combination, false);
    }
    
    /*
     * Accepts params for connecting to a WFS server and also source, target and
     * mask features.
     */
    public LayerIntersector(URL wfsURL, String source, String target,
        String sourceMask, String targetMask, String sourceCode,
        String targetCode, String hostname, Integer port, String database,
        String schema, String user, String password, Long combination, 
        boolean preserve_target) throws Exception {
	
		initOrclMap(hostname, port, database, schema, user, password);
	
		/*
		 * Polygon create here is a "safety" feature. Some intersections may
		 * result in null geometry. And if this accurs, inserting it into DB
		 * causes NPE. So in such cases we simple replace unexisting geometry
		 * with this fake null area polygon.
		 */
		GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
		WKTReader reader = new WKTReader(geometryFactory);
		
		dumbPolygon = (Polygon) reader.read("POLYGON((0 0, 0 0, 0 0, 0 0, 0 0))");

		log("Source table: " + source + ", target table: " + target + ", mask table src: " + sourceMask + " and mask table trg: " + targetMask);
		this.source = source;
		this.target = target;
		// 1. read src, trg and msk and store results into lists
		List<Geometry> tableSrcGeoms = new Vector<Geometry>(), tableTrgGeoms = new Vector<Geometry>(), tableMskSrcGeoms = new Vector<Geometry>(), tableMskTrgGeoms = new Vector<Geometry>();

		List<String> tableSrcCodes = new Vector<String>(), tableTrgCodes = new Vector<String>();

		url = wfsURL;

		long start = System.currentTimeMillis();

		//log("read(source = "+source+", tableSrcGeoms="+tableSrcGeoms+", sourceCode="+sourceCode+", tableSrcCodes="+tableSrcCodes+")");
		read(source, tableSrcGeoms, sourceCode, tableSrcCodes);
		//log("read(target = "+target+", tableTrgGeoms="+tableTrgGeoms+", targetCode="+targetCode+", tableTrgCodes="+tableTrgCodes+")");
		read(target, tableTrgGeoms, targetCode, tableTrgCodes);

		if (sourceMask != null)
			read(sourceMask, tableMskSrcGeoms);

		if (targetMask != null)
			read(targetMask, tableMskTrgGeoms);

		log("Reading data took " + (System.currentTimeMillis() - start) + " ms.");
		//log(tableSrcCodes.toString());
		//log(tableTrgCodes.toString());

		// 2. calculate intersection of mask source and target geometries
		List<Geometry> tableSrcGeoms_ = new ArrayList<Geometry>(tableSrcGeoms);
		List<String> tableSrcCodes_ = new ArrayList<String>(tableSrcCodes);

		start = System.currentTimeMillis();

		if (sourceMask != null) {
			log("\n\nCalculating difference between '" + source + "' and '" + sourceMask + ".");
			tableSrcGeoms_ = action(tableSrcGeoms, tableSrcCodes, tableMskSrcGeoms, null, OPERATOR_DIFFERENCE);
			tableSrcCodes_ = tableSrcCodes;
		}

		List<Geometry> tableTrgGeoms_ = new ArrayList<Geometry>(tableTrgGeoms);
		List<String> tableTrgCodes_ = new ArrayList<String>(tableTrgCodes);

		if (targetMask != null) {
			log("\n\nCalculating difference between '" + target + "' and '" + targetMask + ".");
			tableTrgGeoms_ = action(tableTrgGeoms, tableTrgCodes, tableMskTrgGeoms, null, OPERATOR_DIFFERENCE);
			tableTrgCodes_ = tableTrgCodes;
		}

		// performing some memory clean-up
		tableSrcGeoms.clear();
		tableTrgGeoms.clear();
		
		tableMskSrcGeoms.clear();
		tableMskTrgGeoms.clear();
		
		// 3. calculate final intersection
		log("\n\nCalculating intersection between masked '" + source + "' and '" + target + ".");
		Map<String, List<Geometry>> originalGeoms = new HashMap<String, List<Geometry>>();
		originalGeoms.put("srcGeoms", new ArrayList<Geometry>());
		originalGeoms.put("trgGeoms", new ArrayList<Geometry>());

		Map<String, List<String>> originalCodes = new HashMap<String, List<String>>();
		originalCodes.put("srcCodes", new ArrayList<String>());
		originalCodes.put("trgCodes", new ArrayList<String>());

		// 4. store the calculations into tables
		
		DataStore orclDataStore = prepareORCLDataStore();
		Transaction orclTransaction = new DefaultTransaction();

		if (orclDataStore != null) {
			try {
				List<Geometry> results = action(tableSrcGeoms_, tableSrcCodes_, tableTrgGeoms_, tableTrgCodes_, OPERATOR_INTERSECTION, originalGeoms, originalCodes, preserve_target);
				log("Preparing to persist features for Combination # " + combination);
				originalGeoms.put("resultGeoms", results);
				save(orclDataStore, orclTransaction, originalGeoms, originalCodes, combination);
				this.size = results.size();
				log("Computation finished successfully!");
			}catch (Exception e) {
				orclTransaction.rollback();
				error("FATAL ERROR: Could not save computation resutls!", e);
			} finally {
				orclTransaction.commit();
				orclTransaction.close();
				orclDataStore.dispose();
			}
		}
        
	}

	/**
	 * Project geometries from src to trg CRS.
	 * 
	 * @param originalGeoms
	 * @param srcCRSCode
	 * @param trgCRSCode
	 * @return 
	 * @throws FactoryException 
	 * @throws NoSuchAuthorityCodeException 
	 * @throws TransformException 
	 * @throws MismatchedDimensionException 
	 */
	private static Geometry projectGeometry(Geometry originalGeom,
			String srcCRSCode, String trgCRSCode) throws NoSuchAuthorityCodeException, FactoryException, MismatchedDimensionException, TransformException {

		Geometry projectedGeometry = null;
		final MathTransform transform = CRS.findMathTransform(CRS.decode(srcCRSCode, true), CRS.decode(trgCRSCode, true), true);

		// converting geometries into a linear system
		try {
			projectedGeometry = JTS.transform(originalGeom, transform);
		} catch (Exception e) {
			GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
			WKTReader reader = new WKTReader(geometryFactory);

			try {
				Polygon worldCutPolygon = (Polygon) reader.read("POLYGON((-180 -89.9, -180 89.9, 180 89.9, 180 -89.9, -180 -89.9))");

				projectedGeometry = JTS.transform(originalGeom.intersection(worldCutPolygon), transform);
			} catch (Exception ex) {
				error("projectGeoms FATAL ERROR: ", ex);
			}
		}
		
		return projectedGeometry;
	}

	/**
	 * 
	 * @return
	 */
	private DataStore prepareORCLDataStore() {
		// get a feature writer
		DataStore orclDataStore;
		try {
			orclDataStore = aquireFactory(orclMap).createDataStore(orclMap);
		} catch (IOException e) {
			error("prepareORCLDataStore FATAL ERROR: ", e);
			return null;
		}
		
		return orclDataStore;
	}

	/**
	 * 
	 * @param ds
	 * @param transaction
	 * @param originalGeoms
	 * @param originalCodes
	 * @throws IOException
	 * @throws TransformException 
	 * @throws FactoryException 
	 * @throws NoSuchAuthorityCodeException 
	 * @throws MismatchedDimensionException 
	 */
	private void save(DataStore ds, Transaction transaction, Map<String, List<Geometry>> originalGeoms,
			Map<String, List<String>> originalCodes, Long combination) throws IOException, MismatchedDimensionException, NoSuchAuthorityCodeException, FactoryException, TransformException {

		FeatureCollection<SimpleFeatureType, SimpleFeature> statFeatures;
		SimpleFeatureType statsType = ds.getSchema(statsTable);
		statFeatures = DefaultFeatureCollections.newCollection();
		SimpleFeatureBuilder statsFb = new SimpleFeatureBuilder(statsType);
		int len = originalGeoms.get("resultGeoms").size();
		log("Result geoms vector size: " + len);
		log("Source geoms vector size: " + originalGeoms.get("srcGeoms").size());
		log("Target geoms vector size: " + originalGeoms.get("trgGeoms").size());

		Map<String, Geometry> geoms = new HashMap<String, Geometry>();
		String[] statFids = null;

		log("Creating results...");
		final long base_fid = new Date().getTime();
		for (int i = 0; i < len; i++) {
				Geometry srcg = originalGeoms.get("srcGeoms").get(i);
				Geometry trgg = originalGeoms.get("trgGeoms").get(i);
				Geometry finalg = originalGeoms.get("resultGeoms").get(i);

				String sourceCodeValue = originalCodes.get("srcCodes").get(i);
				String targetCodeValue = originalCodes.get("trgCodes").get(i);

				if (srcg != null && trgg != null && finalg != null) {
					double srcArea = projectGeometry(srcg, "EPSG:4326", "EPSG:41001").getArea() / (1000000); // Km2
					//log("Source Area: " + srcArea);
					double trgArea = projectGeometry(trgg, "EPSG:4326", "EPSG:41001").getArea() / (1000000); // Km2
					//log("Target Area: " + trgArea);
					double finArea = projectGeometry(finalg, "EPSG:4326", "EPSG:41001").getArea() / (1000000); // Km2
					//log("Final Area: " + finArea);
					double srcPercArea = (finArea / srcArea) * 100.0;
					double trgPercArea = (finArea / trgArea) * 100.0;
					// STATS_FID
					statsFb.add(String.valueOf(base_fid + i));
					// SRC_NAME: Acronym of the source layer
					statsFb.add(source);
					// SRC_CODE: Unique Code of the source layer
					statsFb.add(sourceCodeValue);
					// TOT_AREA_SRC: Total surface in sqm of the whole src layer
					// polygon
					statsFb.add(new Double(srcArea));
					// TRG_NAME: Acronym of the target layer
					statsFb.add(target);
					// TRG_CODE: Unique Code of the target layer
					statsFb.add(targetCodeValue);
					// TOT_AREA_TRG: Total surface in sqm of the trg layer polygon
					statsFb.add(new Double(trgArea));
					// AREA: Surface in sqm of the resulting intersection polygon
					statsFb.add(new Double(finArea));
					// OV_SRC: Percentage of surface of single polygon vs total area
					// of overlapping source
					statsFb.add(new Double(srcPercArea));
					// OV_TRG: Percentage of surface of single polygon vs total area
					// of overlapping target
					statsFb.add(new Double(trgPercArea));
					// COMBINATION: The combination id on the table IE_COMBINATION
					statsFb.add(new Long(combination));

					SimpleFeature statFeature = statsFb.buildFeature(null);
					String geomType = finalg.getClass().getName();

					if (finalg.getNumGeometries() > 0 && finalg.getArea() > 0.0) {
						if (finalg instanceof Polygon || finalg instanceof MultiPolygon) {
							geoms.put(String.valueOf(statFeature.getAttribute("STATS_FID")), finalg);
						} else {
							if (finalg instanceof GeometryCollection) {
								boolean containsPoly = false;
								for (int g = 0; g < ((GeometryCollection) finalg).getNumGeometries(); g++) {
									Geometry gg = ((GeometryCollection) finalg).getGeometryN(g);
									if (gg instanceof Polygon || gg instanceof MultiPolygon) {
										geoms.put(String.valueOf(statFeature.getAttribute("STATS_FID")), finalg);
										containsPoly = true;
										break;
									}
								}
								
								if (!containsPoly)
									geoms.put(String.valueOf(statFeature.getAttribute("STATS_FID")), (Polygon) dumbPolygon.clone());
							} else {
								LOGGER
										.warn("Adding dummy polygon, because geometry is not valid"
												+ " Number of geometries: "
												+ finalg.getNumGeometries()
												+ " Area: "
												+ finalg.getArea()
												+ " Type: " + geomType);

								geoms.put(String.valueOf(statFeature.getAttribute("STATS_FID")), (Polygon) dumbPolygon.clone());
							}
						}
					} else {
						LOGGER
								.warn("Adding dummy polygon, because geometry is not valid"
										+ " Number of geometries: "
										+ finalg.getNumGeometries()
										+ " Area: "
										+ finalg.getArea()
										+ " Type: "
										+ geomType);
						geoms.put(String.valueOf(statFeature.getAttribute("STATS_FID")), (Polygon) dumbPolygon.clone());
					}
					statFeatures.add(statFeature);
					//log("Prepared " + (i + 1) + "/" + len);
				}
			}
				
			/**
			 * SAVING RESULTS 
			 */
			log("Saving results ...");
            
    		FeatureCollection<SimpleFeatureType, SimpleFeature> batchStatFeatures;
    		batchStatFeatures = DefaultFeatureCollections.newCollection();
    		List<Geometry> batchGeoms = new ArrayList<Geometry>();
    		
            int index = 1;
            for (FeatureIterator ftt=statFeatures.features(); ftt.hasNext(); index++) {
            	SimpleFeature statFeature = (SimpleFeature) ftt.next();
            	batchStatFeatures.add(statFeature);
            	batchGeoms.add(geoms.get(String.valueOf(statFeature.getAttribute("STATS_FID"))));
            }
            
            if (batchStatFeatures.size() > 0) {
            	batchSaveFeatureTypes(ds, transaction, statsType, batchStatFeatures, batchGeoms);
            }
            
            log("Saved "+(index-1) + "/"+len);
	}

	/**
	 * @param ds
	 * @param statsType
	 * @param batchStatFeatures
	 * @param batchGeoms
	 * @throws IOException
	 */
	private void batchSaveFeatureTypes(
			DataStore ds,
			Transaction transaction,
			SimpleFeatureType statsType,
			FeatureCollection<SimpleFeatureType, SimpleFeature> batchStatFeatures,
			List<Geometry> batchGeoms) throws IOException {
		try {
			// /////////////////////////////////////////////////////////////////////
	        //
	        // create the features
	        //
	        // /////////////////////////////////////////////////////////////////////
			FeatureCollection<SimpleFeatureType, SimpleFeature> statsFeatures = readFeatures(ds, statsTable);
			statsFeatures.addAll(batchStatFeatures);
			
	        SimpleFeature feature = null;
	        final int size = statsFeatures.size();
			final FeatureWriter<SimpleFeatureType, SimpleFeature> statsFwriter = ds.getFeatureWriter(statsTable, transaction);
			final FeatureIterator<?> fr = statsFeatures.features();
			
	        while (fr.hasNext()) {
	            SimpleFeature srcFeature = (SimpleFeature) fr.next();
	            
	            // avoid illegal state
	            statsFwriter.hasNext();
	            feature = statsFwriter.next();

	            if (srcFeature != null) {
	                int attr = srcFeature.getAttributeCount();
	                for (int a = 0; a < attr; a++) {
	                    Object attribute = srcFeature.getAttribute(a);
	                    feature.setAttribute(a, attribute);
	                }
	                statsFwriter.write();
	            }
	        }
	        fr.close();

	        try {
	            statsFwriter.close();
	        } catch (Exception whatever) {
	            // amen
	        }
		} catch (Exception e) {
			error("Could not save statistics.", e);
		}
		
		 
		/*
		 * ID: The same above THE_GEOM: Resulting intersection
		 * polygon between src and trg layers STATS: A Foreign Key
		 * to the Statistical table
		 */

		FeatureCollection<SimpleFeatureType, SimpleFeature> spatialFeatures = readFeatures(ds, spatialTable);
		SimpleFeatureType spatialType = ds.getSchema(spatialTable);
		SimpleFeatureBuilder spatialFb = new SimpleFeatureBuilder(spatialType);

		String spatialCode = "4326"; // Sensible default
		String userData = "http://www.opengis.net/gml/srs/epsg.xml#" + spatialCode;
		//LOGGER.info(userData);
		// Save geometry array and stats id array
		int j = 0;
		for (Iterator<SimpleFeature> it = batchStatFeatures.iterator(); it.hasNext(); ) {
			Geometry g = batchGeoms.get(j);
			if (g != null) {
				g.setUserData(userData);
				GeometryDescriptor gd = spatialType.getGeometryDescriptor();
				Object n = (Object) gd.getName();
				String ln = gd.getLocalName();
				spatialFb.add(g);
				spatialFb.add(String.valueOf(it.next().getAttribute("STATS_FID")));
				SimpleFeature spatialFeature = spatialFb.buildFeature(null);
				spatialFeatures.add(spatialFeature);
			}
			j++;
		}

		try {
			// /////////////////////////////////////////////////////////////////////
	        //
	        // create the features
	        //
	        // /////////////////////////////////////////////////////////////////////
	        SimpleFeature feature = null;
	        final int size = spatialFeatures.size();
	        final FeatureWriter<SimpleFeatureType, SimpleFeature> spatialFwriter = ds.getFeatureWriter(spatialTable, transaction);
			final FeatureIterator<?> fr = spatialFeatures.features();
			
	        while (fr.hasNext()) {
	            SimpleFeature srcFeature = (SimpleFeature) fr.next();
	            
	            // avoid illegal state
	            spatialFwriter.hasNext();
	            feature = spatialFwriter.next();

	            if (srcFeature != null) {
	                int attr = srcFeature.getAttributeCount();
	                for (int a = 0; a < attr; a++) {
	                    Object attribute = srcFeature.getAttribute(a);
	                    feature.setAttribute(a, attribute);
	                }
	                spatialFwriter.write();
	            }
	        }
	        fr.close();

	        try {
	            spatialFwriter.close();
	        } catch (Exception whatever) {
	            // amen
	        }
		} catch (Exception e) {
			error("Could not save geometries, perhaps they are not compatible?", e);
		} finally {
			batchStatFeatures.clear();
			batchGeoms.clear();
		}
	}

	/**
	 * 
	 * @param server_url
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static WFS_1_0_0_DataStore getWFSDataStore(URL server_url) {
		try {
			Map m = new HashMap();
			m.put(WFSDataStoreFactory.URL.key, server_url);
			m.put(WFSDataStoreFactory.TIMEOUT.key, new Integer(10000)); // not debug
			m.put(WFSDataStoreFactory.TIMEOUT.key, new Integer(1000000)); // for debug
			return (WFS_1_0_0_DataStore) (new WFSDataStoreFactory()).createDataStore(m);
		} catch (IOException e) {
			error("getWFSDataStore FATAL ERROR: ", e);
			return null;
		}
	}

	/**
	 * 
	 * @param table
	 * @param resultGeoms
	 * @param code
	 * @param resultCodes
	 * @throws Exception
	 */
	private void read(String table, List<Geometry> resultGeoms, String code,
			List<String> resultCodes) throws Exception {
		read(table, resultGeoms, code, resultCodes, null);
	}

	/**
	 * 
	 * @param table
	 * @param results
	 * @throws Exception
	 */
	private void read(String table, List<Geometry> results) throws Exception {
		read(table, results, null, null, null);
	}

	/**
	 * 
	 * @param table
	 * @param resultGeoms
	 * @param code
	 * @param resultCodes
	 * @param query
	 * @throws Exception
	 */
	private void read(String table, List<Geometry> resultGeoms, String code,
			List<String> resultCodes, Query query) throws Exception {

		log("Reading data from table: " + table);
		DataStore wfs = getWFSDataStore(url);
		if (wfs == null) {
			throw new IOException("No WFS server found at " + url + "\nPlease make sure it's running and you have network connectivity");
		}
		String types[] = wfs.getTypeNames();
		Boolean present = false;
		for (String type : types) {
			if (table.equals(type))
				present = true;
		}
		if (present == false) {
			throw new Exception("Layer '" + table + "' not present in " + url);
		} else {
			SimpleFeatureType type = wfs.getSchema(table);
			type.getTypeName();
			type.getName().getNamespaceURI();
			FeatureSource<SimpleFeatureType, SimpleFeature> source = wfs.getFeatureSource(table);
			source.getBounds();

			FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures();
			features.getBounds();
			features.getSchema();
			if (query == null) {
				query = new DefaultQuery(table);
			}
			features = source.getFeatures(query);

			/*
			 * final FeatureSource<?, ?> dbSource = ds.getFeatureSource(table);
			 * final FeatureCollection<?, ?> dbShape = dbSource.getFeatures();
			 * final FeatureIterator<?> ftReader = dbShape.features();
			 */

			FeatureIterator<SimpleFeature> ftReader = features.features();

			int read = 0;
			while (ftReader.hasNext()) {
				SimpleFeature srcFeature = (SimpleFeature) ftReader.next();

				if (srcFeature != null) {
					int attr = srcFeature.getAttributeCount();
					for (int a = 0; a < attr; a++) {
						Object attribute = srcFeature.getAttribute(a);
						if (attribute instanceof Geometry) {
							//log("Reading Geometry = " + attribute);
							resultGeoms.add((Geometry) attribute);
							if (code != null && resultCodes != null) {
								try {
									//log("Reading Attribute " + code + " = " + srcFeature.getAttribute(code));
									resultCodes.add(srcFeature.getAttribute(code).toString());
								} catch (Exception e) {
									error("Error reading Code Value", e);
									resultCodes.add("ERROR READING");
								}
							}else{
								//LOGGER.info("No codes read");
							}
							break;
						}
					}

					read++;
				}
			}

			ftReader.close();
			log("Number of features: '" + table + "' = " + read);

		}// End if present
		wfs.dispose();
	}

	/**
	 * 
	 * @param theUrl
	 * @param layer
	 * @return
	 * @throws IOException
	 */
	public static FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures(URL theUrl, String layer) throws IOException{
		DataStore ds = getWFSDataStore(theUrl);
		if (ds == null) {
			throw new IOException("No WFS server found at " + theUrl + "\nPlease make sure it's running and you have network connectivity");
		}
		
        SimpleFeatureType statsType = ds.getSchema(layer);
        Filter statsFilter = Filter.INCLUDE;

 		FeatureSource<SimpleFeatureType, SimpleFeature> feats = ds.getFeatureSource(layer);
 		FeatureCollection<SimpleFeatureType, SimpleFeature> fc = feats.getFeatures(statsFilter);
 	
 		return fc;
	}
	
	/**
	 * 
	 * @param theUrl
	 * @param layer
	 * @return
	 */
	public static int getLayerSize(URL theUrl, String layer){
        FeatureCollection<SimpleFeatureType, SimpleFeature> fc;
		try {
			fc = getFeatures(theUrl, layer);
	        int len = fc.size();
	        return len;
		} catch (IOException e) {
			error("getLayerSize FATAL ERROR: ", e);
		}
		return 0;
	}

	/**
	 * 
	 * @param srcGeoms
	 * @param tableSrcCodes
	 * @param trgGeoms
	 * @param tableTrgCodes
	 * @param operator
	 * @return
	 * @throws IOException
	 */
	private List<Geometry> action(
			List<Geometry> srcGeoms,
			List<String> tableSrcCodes, 
			List<Geometry> trgGeoms,
			List<String> tableTrgCodes, 
			int operator) throws IOException {
		return action(srcGeoms, tableSrcCodes, trgGeoms, tableTrgCodes, operator, null, null, false);
	}
	
	/**
	 * 
	 * @param srcGeoms
	 * @param tableSrcCodes
	 * @param trgGeoms
	 * @param tableTrgCodes
	 * @param operator
	 * @param originalGeoms
	 * @param originalCodes
	 * @param preserve_target 
	 * @param orclDataStore
	 * @param orclTransaction
	 * @return
	 * @throws IOException
	 */
	private List<Geometry> action(
			List<Geometry> srcGeoms,
			List<String> tableSrcCodes, 
			List<Geometry> trgGeoms,
			List<String> tableTrgCodes, 
			int operator, 
			Map<String, List<Geometry>> originalGeoms,
			Map<String, List<String>> originalCodes, boolean preserve_target) throws IOException {
		List<Geometry> results = new Vector<Geometry>();
		boolean contained = false;
		int cnt = 0;
		log ("Going to check " + srcGeoms.size() + "*" + trgGeoms.size() + " = " + (srcGeoms.size() * trgGeoms.size()) + " intersections." );
		for (int j = 0; j < srcGeoms.size(); j++) {
			int intersectionCounter = 0;
			for (int i = 0; i < trgGeoms.size(); i++) {
				final Geometry srcPoly = (operator == OPERATOR_DIFFERENCE ? (results.size() < j+1 ? srcGeoms.get(j) : results.get(j)) : srcGeoms.get(j));
				final Geometry trgPoly = trgGeoms.get(i);
				final Geometry maskPoly = trgGeoms.get(i);

				boolean allowOperationCondition = false;
				
				if (operator == OPERATOR_DIFFERENCE) {
					allowOperationCondition = maskPoly.getEnvelope().intersects(srcPoly) || maskPoly.getEnvelope().overlaps(srcPoly) || maskPoly.getEnvelope().touches(srcPoly);
					if (maskPoly.contains(srcPoly)) contained = true;
				} else {
					allowOperationCondition = maskPoly.getEnvelope().intersects(srcPoly);
				}
				
				if (allowOperationCondition) {
					final Geometry p = maskPoly.buffer(0, 0);
					Geometry fn = null;
					
					
				    if (operator == OPERATOR_DIFFERENCE)
						fn = srcPoly.difference(p);
					else
						fn = srcPoly.intersection(p);
					

					if ((fn != null) && (fn instanceof Polygon || fn instanceof MultiPolygon)) {
						intersectionCounter++;
						
						if (originalGeoms != null) {
							if (operator == OPERATOR_DIFFERENCE && results.size() == j+1) originalGeoms.get("srcGeoms").remove(j);
							if (operator == OPERATOR_DIFFERENCE && results.size() == j+1) originalGeoms.get("trgGeoms").remove(j);
							originalGeoms.get("srcGeoms").add(srcPoly);
							originalGeoms.get("trgGeoms").add(trgPoly);
						}

						if (originalCodes != null) {
							String srcCode = "ERROR IN ACTION"; // Sensible default
							String trgCode = "ERROR IN ACTION"; // Sensible default

							if (tableSrcCodes != null && tableSrcCodes.size() > 0) {
								srcCode = tableSrcCodes.get(j);
								//log("action::originalCodes::srcCodes == " + srcCode);
							}

							if (tableTrgCodes != null && tableTrgCodes.size()>0) {
								trgCode = tableTrgCodes.get(i);
								//log("action::originalCodes::trgCodes == " + trgCode);
							}

							if ((operator == OPERATOR_DIFFERENCE && results.size() < j+1) || (operator == OPERATOR_INTERSECTION)) {
								originalCodes.get("srcCodes").add(srcCode);
								originalCodes.get("trgCodes").add(trgCode);
							}
						}
						
						if (operator == OPERATOR_DIFFERENCE && results.size() == j+1) results.remove(j);
						
						// Do not save if any of the masks contains the geometry
						//if (operator == OPERATOR_DIFFERENCE && contained) continue; 
						if (preserve_target)
							results.add(trgPoly);
						else
						    results.add(fn);
					}
				}
			}

			if (intersectionCounter == 0 && operator == OPERATOR_DIFFERENCE) {
				Geometry srcPoly = srcGeoms.get(j);
				results.add(srcPoly);

				intersectionCounter++;
			}
			
			log("Calculated "+(1 + (cnt++)) + "/" + srcGeoms.size() + " : " + intersectionCounter + " polygons");
			
		}

		return results;
	}
	
	/**
	 * 
	 * @param ds
	 * @param layerName
	 * @return 
	 * @throws IOException
	 */
	private static FeatureCollection<SimpleFeatureType, SimpleFeature> readFeatures(DataStore ds, String layerName) throws IOException {
        SimpleFeatureType statsType = ds.getSchema(layerName);
        Filter statsFilter = Filter.INCLUDE;

        FeatureSource<SimpleFeatureType, SimpleFeature> stats = ds.getFeatureSource(layerName);
        FeatureCollection<SimpleFeatureType, SimpleFeature> fc = stats.getFeatures(statsFilter);
 	    
 	    return fc;
	}
	
	/**
	 * 
	 * @param url
	 * @throws IOException
	 * @throws CQLException
	 */
	public static void clean(URL url) throws IOException {
            DataStore ds = getWFSDataStore(url);
		
	    LOGGER.info("Deleting all records");
	    cleanLayer(ds, spatialName);	
	    LOGGER.info("Spatial features cleared");
	    cleanLayer(ds, statsName);
	    LOGGER.info("Statistical features cleared");
	}
	
	/**
	 * 
	 * @param ds
	 * @param layerName
	 * @throws IOException
	 * @throws CQLException
	 */
	private static void cleanLayer(DataStore ds, String layerName) throws IOException {
		LOGGER.info("Started cleaning "+ layerName+" features");

        SimpleFeatureType statsType = ds.getSchema(layerName);
        Filter statsFilter = Filter.INCLUDE;

        FeatureSource<SimpleFeatureType, SimpleFeature> stats = ds.getFeatureSource(layerName);
        FeatureCollection<SimpleFeatureType, SimpleFeature> fc = stats.getFeatures(statsFilter);
        FeatureIterator<SimpleFeature> statsFr = fc.features();
        Set<FeatureId> statsFeatureIds = new HashSet<FeatureId>(); 
        int len = fc.size();
        int index =1;
        for (; statsFr.hasNext();index++) {
        	SimpleFeature feat  = statsFr.next();
    	    statsFeatureIds.add(feat.getIdentifier());
            LOGGER.info("Deleted " + index + "/"+len);
    	    
        	if(statsFeatureIds.size() == BATCH_SIZE || index == len){
         	    FilterFactory statsFac=CommonFactoryFinder.getFilterFactory(null);       
                Id statsFidFilter = statsFac.id(statsFeatureIds);
                doDelete(ds, statsType, statsFidFilter);
                statsFeatureIds = new HashSet<FeatureId>();
        	}
 	      }
 	    statsFr.close(); 	     
		LOGGER.info("Finished cleaning "+layerName+" features");
	}
	
	/**
	 * 
	 * @param ds
	 * @param ft
	 * @param ff
	 * @throws NoSuchElementException
	 * @throws IOException
	 */
    private static void doDelete(DataStore ds,SimpleFeatureType ft, Id ff) throws NoSuchElementException, IOException{
        Transaction t = new DefaultTransaction();
        FeatureStore<SimpleFeatureType, SimpleFeature> fs = (FeatureStore<SimpleFeatureType, SimpleFeature>)ds.getFeatureSource(ft.getTypeName());
        fs.setTransaction(t);
        fs.removeFeatures(ff);
        try{
           t.commit();
        }finally{
           t.close();
        }
    }

    /**
     * When loading from DTO use the params to locate factory.
     *
     * <p>
     * bleck
     * </p>
     *
     * @param params
     *
     * @return
     */
    public static DataStoreFactorySpi aquireFactory(Map params) {
        for (Iterator i = DataStoreFinder.getAvailableDataStores(); i.hasNext();) {
            DataStoreFactorySpi factory = (DataStoreFactorySpi) i.next();
            if (factory.canProcess(params)) {
                System.out.println(factory.getDisplayName());
                return factory;
            }
        }

        return null;
    }
    
    /**
     * 
     * @param msg
     */
	private static void log(String msg) 
    {
       LOGGER.info(msg);
    }	

    /**
     * 
     * @param msg
     */
	private static void error(String msg, Throwable t) 
    {
       LOGGER.error(msg, t);
    }	

}
