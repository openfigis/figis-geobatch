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

import static org.geotools.jdbc.JDBCDataStoreFactory.MAXWAIT;
import it.geosolutions.figis.Request;
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.ConfigXStreamMapper;
import it.geosolutions.figis.model.DB;
import it.geosolutions.figis.model.Geoserver;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.figis.intersection.util.TmpDirManager;
import it.geosolutions.geobatch.figis.intersection.util.ZipStreamReader;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.v1_0_0.WFS_1_0_0_DataStore;
import org.geotools.process.feature.gs.ClipProcess;
import org.geotools.process.feature.gs.IntersectionFeatureCollection;
import org.geotools.process.feature.gs.IntersectionFeatureCollection.IntersectionMode;
import org.geotools.process.feature.gs.QueryProcess;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
import it.geosolutions.geoserver.rest.decoder.RESTDataStoreList;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.decoder.utils.NameLinkElem;


/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class IntersectionAction extends BaseAction<EventObject> {
    private final static Logger LOGGER = LoggerFactory.getLogger(IntersectionAction.class);
    private GeoServerRESTReader gsRestReader= null;
//    private WFS_1_0_0_DataStore dataStore = null;
    private Geoserver geoserver = null;
    /**
     * configuration
     */
    private final IntersectionConfiguration conf;

    private String host = "http://localhost:9999";
    private String tmpDirName = "figis";
    
    public IntersectionAction(IntersectionConfiguration configuration) {
        super(configuration);
        conf = configuration;
        host = conf.getPersistencyHost();
        //TODO initialize your members here
    }
 

    
    
    public void setGeoserver(Geoserver geoserver) {
		this.geoserver = geoserver;
	}




    
    /**********
     * download a zip file from geoserver, save it in the tmp directory, uncompress it and return a simpleFeaturecollection
     * @param layername the layer to download
     * @return the simplefeaturecollection of the downloaded layer
     */
    private SimpleFeatureCollection downloadFromGeoserver(String layername){
    	String name = layername.substring(layername.indexOf(":")+1);
    	String urlCollection  = geoserver.getGeoserverUrl()+"/wfs?outputFormat=SHAPE-ZIP&request=GetFeature&version=1.1.1&typeName="+name+"&srs=EPSG:4326";
    	try {
	    	String tmp = System.getProperty("java.io.tmpdir")+System.getProperty("file.separator")+tmpDirName;
	       	return  ZipStreamReader.getShapeFileFromURLbyZIP(urlCollection, tmp, name, true);
    	} catch(Exception e) {
    		System.out.println("Failed to download the "+urlCollection+" layer");
    		LOGGER.trace("Failed to download the "+urlCollection+" layer");
    		return null;
    	}
    }
    
    /***********
     * perform the ie-intersection action
     * @param intersection the data on which to perform the intersection
     * @return true in case no intersection is performed because any reason
     */
    public SimpleFeatureCollection intersection(Intersection intersection) {

    	//initialize variables
    	String srcLayer = intersection.getSrcLayer();
    	String trgLayer = intersection.getTrgLayer();
    	String srcCodeField =  intersection.getSrcCodeField();
    	String trgCodeField = intersection.getTrgCodeField();
    	String maskLayer = intersection.getMaskLayer();
    	
    	boolean isMasked = intersection.isMask();
    	
    	IntersectionMode mode = IntersectionMode.INTERSECTION;
    	if (!intersection.isPreserveTrgGeom()) mode = IntersectionMode.SECOND;
    	
    	//load feature collections from geoserver
    	SimpleFeatureCollection srcCollection =null;
    	SimpleFeatureCollection trgCollection = null;
    	SimpleFeatureCollection maskCollection = null;
		try {
			// try to load src collection
			
			LOGGER.trace("download first geometry "+srcLayer+" "+srcCodeField);
			srcCollection = downloadFromGeoserver(srcLayer);
			LOGGER.trace("finish download first geometry");

			// check if the src attribute exists and srcCollection is not empty
			if (srcCollection == null || srcCollection.getSchema().getDescriptor(srcCodeField)==null) {
				LOGGER.trace("Error downloading "+srcLayer+" or the "+srcCodeField+" attribute does not exist");
				return null;
			}
			// try to load trg collection 
			LOGGER.trace("download second geometry "+trgLayer+" "+trgCodeField);
			trgCollection = downloadFromGeoserver(trgLayer);
			LOGGER.trace("finish download second geometry");

			// check if the src attribute exists and trgCollection is not empty
			if (trgCollection==null || trgCollection.getSchema().getDescriptor(trgCodeField)==null) {
				LOGGER.trace("Error downloading "+trgLayer+" or the "+trgCodeField+" attribute does not exist");
				return null;
			}

			// try to load mask collection
			if (isMasked) {
				LOGGER.trace("download mask layer "+getName(maskLayer));
				maskCollection = downloadFromGeoserver(getName(maskLayer));
				if (maskCollection== null) {
					LOGGER.trace("Error downloading "+maskLayer);
					return null;
				}
				
				LOGGER.trace("mask layer "+maskLayer+" downloaded");
			}
			
			
		} catch(Throwable e) {
			LOGGER.trace("Failed to load some layers");
			return null;
			
		}

    	
    	// check if intersection requires masking
    	if (isMasked) {
    		// generate the union of the mask geometries
    		ClipProcess clipProcess = new ClipProcess();
    		SimpleFeatureIterator sfi = maskCollection.features();
    		Geometry maskGeometry = null;
    		if (sfi.hasNext()) {
    			maskGeometry =(Geometry)sfi.next().getDefaultGeometry();
    		}
    		while (sfi.hasNext()) {
    			maskGeometry = maskGeometry.union((Geometry)sfi.next().getDefaultGeometry());
    		}

    		// clip the src Collection using the mask collection
    		srcCollection = clipProcess.execute(srcCollection, maskGeometry);
    		
    		// clip the trg Collection using the mask collection    		
    		trgCollection = clipProcess.execute(trgCollection, maskGeometry);
    	}

    	// setup for the IntersectionFeatureCollectionProcess
    	List<String> srcAttributes = new ArrayList<String>();
    	srcAttributes.add(srcCodeField);
    	List<String> trgAttributes = new ArrayList<String>();

    	trgAttributes.add(trgCodeField);  
    	// perform the IntersectionFeatureCollection process
    	IntersectionFeatureCollection intersectionProcess = new IntersectionFeatureCollection();
    	SimpleFeatureCollection result2 = intersectionProcess.execute(srcCollection, trgCollection, srcAttributes, trgAttributes, mode, true, true);
    	return result2;
    }
    
    

    
    public boolean initConnections(Geoserver geoserver) {
    	try {
	        //check if this control works as expected
    		String url = geoserver.getGeoserverUrl()+"/geoserver";
    		LOGGER.trace(url+" USER "+geoserver.getGeoserverUsername()+", PWD "+geoserver.getGeoserverPassword());
    		gsRestReader = new GeoServerRESTReader(url, geoserver.getGeoserverUsername(), geoserver.getGeoserverPassword());

	        if (!gsRestReader.existGeoserver()) return false;

	       //check if this control works as expected
//	        dataStore = geoServerConnection(geoserver);
//	        
//	        if (dataStore==null) return false;

    	} catch(Exception e) {
    		return false;
    	}
        return true;
    }
    
    private String getName(String layername){
    	int i= layername.indexOf(":");
    	if (i > 0 ) return layername.substring(i+1);
    	return layername;
    }
    
    /************
     * execute the intersections and update their status on the basis of the content od the intersections table
     * if status is DELETE then the intersection will be removed
     * if status is TOCOMPUTE then the intersection will be executed, stored and published and then the STATUS will be updated to COMPUTED
     * otherwise no changes are applied to the intersection
     * @param host where address the request of delete, update
     * @param intersections the intersections to parse
     * @return true everything goes well
     * @throws Exception 
     */
    public synchronized boolean executeIntersectionStatements(String host, Config config, boolean simulate) {
    	OracleDataStoreManager dataStoreOracle = null;
    	try {
	        List<Intersection> intersections = null;
	        
			try {
				Request.initIntersection();
				intersections = Request.getAllIntersections(host);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				return false;
			}
	        //check if this control works as expected
	        if (intersections==null) return false;
	    	LOGGER.trace("Updating intersections");
	
	    	// init of the DB connectio to the ORACLE datastore
	    	String dbHost = config.getGlobal().getDb().getHost();
			String schema = config.getGlobal().getDb().getSchema();
			String db = config.getGlobal().getDb().getDatabase();
			String user = config.getGlobal().getDb().getUser();
			String pwd = config.getGlobal().getDb().getPassword();
			int port = Integer.parseInt(config.getGlobal().getDb().getPort());
			
	    	try {
	    		dataStoreOracle = new OracleDataStoreManager(dbHost,port,db,schema,user,pwd);
			} catch (Exception e1) {
				LOGGER.trace("Problems creating the ORACLE datastore instance, check the parameters");   
				return false;
			}
	    	
	    	
	    	for (Intersection intersection:intersections){
	    		
	        	Status status = intersection.getStatus();
    			String srcLayer = intersection.getSrcLayer();
    			String trgLayer = intersection.getTrgLayer();
    			String srcCode = intersection.getSrcCodeField();
    			String trgCode = intersection.getTrgCodeField();
    			
     	
	        	long id = intersection.getId();
	        	// in case the intersection has been scheduled to be deleted, delete the intersection from the list
	        	// and its intersection from the DB
	        	if (status==Status.TODELETE) {
	        		Request.deleteIntersectionById(host, id);
        			try {
						dataStoreOracle.deleteAll(getName(srcLayer), getName(trgLayer));
					} catch (Exception e) {
						LOGGER.trace("Problem deleting intersection from the database identified by "+srcLayer+","+trgLayer+"\n"+e);
					}
        			// still to implement
	        	}
	        	if (status==Status.TOCOMPUTE) { // if the intersection should be computed
	        		SimpleFeatureCollection resultInt = null;
	        		resultInt = intersection(intersection); // compute the intersection between the layers
	        		
	        		// 
	        		String geometryType = null;
	        		if (resultInt!=null) geometryType = resultInt.getSchema().getGeometryDescriptor().getType().getName().getLocalPart();
	        		
	        		// the intersection can be updated on the db only if the intersection generate a reuslt and it is Multipolygon typed
	        		// else it must be deleted by both the db and by the intersection list
	        		if (resultInt!=null && geometryType.equals("MultiPolygon")) {
	        			// set the intersection to Computing. This is to avoid that a concurrent
	        			// compute again the intersection
	        			intersection.setStatus(Status.COMPUTING); 
	        			Request.updateIntersectionById(host, id, intersection);
		        		LOGGER.trace("CREDENTIAL FOR "+schema+":"+db+" on "+ dbHost+":"+port+"("+user+","+pwd+")");
						try {
							dataStoreOracle.perform(resultInt,getName(srcLayer), getName(trgLayer), srcCode, trgCode);
			        	    intersection.setStatus(Status.COMPUTED);
						} catch (Exception e) {
							// some problems occurred when saving the intersections in the db.
							// in this case we schedule to delete this intersection and will be deleted at next action
							LOGGER.trace("Problem performing Intersection on "+srcLayer+","+trgLayer+","+srcCode+","+trgCode+"\n"+e);
			        	    intersection.setStatus(Status.TODELETE);
						} finally {
			        		Request.updateIntersectionById(host, id, intersection);	
						}
	        		}
	        		else {
	        			LOGGER.trace("Skipping intersection between "+srcLayer+" and "+trgLayer+" because the intersection cannot be computed");
	        			LOGGER.trace("Intersection will be deleted");
		        		Request.deleteIntersectionById(host, id);
		        		Request.updateIntersectionById(host, id, intersection);
	        			try {
							dataStoreOracle.deleteAll(getName(srcLayer), getName(trgLayer));
						} catch (IOException e) {
							LOGGER.trace("Some problems occured deleting intersection from the database identified by "+srcLayer+","+trgLayer+"\n"+e);
						}
	        		}
	        	}
	        }
	        LOGGER.trace("Intersections updates: Successfull");
	        return true;
    	} finally {
    		if (dataStoreOracle!=null) dataStoreOracle.close();
    	}
    }
    
    
    public List<Intersection>getIntersection(String host) throws MalformedURLException{
    	 return Request.getAllIntersections(host);
    }
    
    
    public Config basicChecks(){
        Request.initConfig();
        Config config;
		try {
			LOGGER.trace("Reading config information");
			config = Request.existConfig(host);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        //check if this control works as expected
        if (config==null) {
        	LOGGER.trace("Problems to find config information. Skip execution ...");
        	return null;
        }
        LOGGER.trace("Config information correctly read. Trying to connect to Geoserver on "+config.getGlobal().getGeoserver().getGeoserverUrl());
        // check the datastore and REST manager geoserver connections 
        if (!initConnections(config.getGlobal().getGeoserver())) {
        	LOGGER.trace("Problems to find Geoserver. Skip execution ...");
        	return null;
        }
        LOGGER.trace("Geoserver and Datastore found");
        return config;
    }
    

    
    /**
     * Removes TemplateModelEvents from the queue and put
     */
    public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {
    	host = conf.getPersistencyHost();
        // return
        final Queue<EventObject> ret=new LinkedList<EventObject>();
        LOGGER.trace("Trying to connect to "+host);
        while (events.size() > 0) {
            final EventObject ev;
            try {
                if ((ev = events.remove()) != null) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("ConfigAction.execute(): working on incoming event: "+ev.getSource());
                    }
                   //  FileSystemEvent fileEvent=(FileSystemEvent)ev;
                    
                    // perform basic checks and return the  current config in the DB
                    Config config = basicChecks();
                    if (config!=null) {
                    	// create the figis temporary dir
                    	File tmpDir = TmpDirManager.createTmpDir(tmpDirName);
                        // update the status of the intersections on the basis of the new input
                        boolean areIntersectionsUpdated = executeIntersectionStatements(host, config, false);
                        
                        // delete the tmpDir after execution
                        TmpDirManager.deleteDir(tmpDir);
                    }
                       
                    // add the event to the return
					ret.add(ev);
					
                } else {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("ConfigAction.execute(): Encountered a NULL event: SKIPPING...");
                    }
                    continue;
                }
            } catch (Exception ioe) {
                final String message = "ConfigAction.execute(): Unable to produce the output: "
                        + ioe.getLocalizedMessage();
                ioe.printStackTrace();
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new ActionException(this, message);
            }
        }
        
        return ret;
    }
    
}
