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
package it.geosolutions.geobatch.figis.cron;

import it.geosolutions.figis.Request;
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.ConfigXStreamMapper;
import it.geosolutions.figis.model.DB;
import it.geosolutions.figis.model.Geoserver;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

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
public class CronAction extends BaseAction<EventObject> {
    private final static Logger LOGGER = LoggerFactory.getLogger(CronAction.class);
    private GeoServerRESTReader gsRestReader= null;
    private WFS_1_0_0_DataStore dataStore = null;
    private Geoserver geoserver = null;
    /**
     * configuration
     */
    private final CronConfiguration conf;
    private final String workspace="sf";
    private String host = null;
    
    public CronAction(CronConfiguration configuration) {
        super(configuration);
        conf = configuration;
        host = conf.getPersistencyHost();
        //TODO initialize your members here
    }
 

    
    
    public void setGeoserver(Geoserver geoserver) {
		this.geoserver = geoserver;
	}




	/********
     * Connect to a geoserver instance
     * @param geoserver contains information for connection
     * @throws IOException in case the URL in the geoserver instance is not available
     */
    private WFS_1_0_0_DataStore geoServerConnection(Geoserver geoserver) throws IOException {
    	try {
    	  // initialize connection parameters
    	   URL url = new URL("http://"+geoserver.getGeoserverUrl()+"/geoserver/ows?service=wfs&version=1.0.0&request=GetCapabilities");
    	   
	   	   Map<Object, Serializable> m = new HashMap<Object, Serializable>();
	   	   m.put(WFSDataStoreFactory.URL.key, url);
	   	   m.put(WFSDataStoreFactory.PASSWORD, geoserver.getGeoserverPassword());
	   	   m.put(WFSDataStoreFactory.USERNAME, geoserver.getGeoserverUsername());
	   	   m.put(WFSDataStoreFactory.TIMEOUT.key, new Integer(10000)); // not debug
//	   	   m.put(WFSDataStoreFactory.TIMEOUT.key, new Integer(1000000)); // for debug
	   	   
	   	   // try WFS connection
	   	   return ((WFS_1_0_0_DataStore) (new WFSDataStoreFactory()).createDataStore(m));
	   	  } catch (IOException e) {
	   		  // in case of connection problems
	   		  return null;
	   	  }
    }
    
    /************
     * load layer in a SimpleFeatureCollection  from the global variable dataStore
     * @param layer the layer to return the simplefeaturecollection
     * @return the SimpleFeatureCollection of srcLayer on the data server
     * @throws IOException 
     */
    @SuppressWarnings("finally")
	private SimpleFeatureCollection load(String layerFullName) {
		SimpleFeatureCollection sfc = null;
    	try {
	    	// this part is not used yet because layer names are unique in all the workspaces
	    	int index = layerFullName.indexOf(":");
	    	String workspace = "empty";
	    	String layer = layerFullName;
	    	if (index>0) {
	    		workspace = layerFullName.substring(0, index);
	    		layer = layerFullName.substring(index+1, layerFullName.length());
	    	}
	    	System.out.println("Layer Name "+layer);
	    	RESTLayer restLayer = gsRestReader.getLayer(layer);
	    	if (restLayer!=null) {
				sfc = dataStore.getFeatureSource(layer).getFeatures();
	    		System.out.println("found"+layer+" CRS"+dataStore.getSchema(layer).getCoordinateReferenceSystem());
	    		
	    	}
	    	} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				dataStore.dispose();
				return sfc;
			}
    }

    /***********
     * perform the ie-intersection action
     * @param intersection the data on which to perform the intersection
     * @return true in case no intersection is performed
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
			System.out.println("SRCLAYER "+srcLayer+" TRGLAYER"+trgLayer+" "+isMasked);
			// try to load src collection
			srcCollection = load(srcLayer);
			List<AttributeDescriptor> descriptors = srcCollection.getSchema().getAttributeDescriptors();
			SimpleFeatureIterator sfi =  srcCollection.features();
			while(sfi.hasNext()) {
				SimpleFeature sf = sfi.next();
				for (int i= 0; i<sf.getAttributeCount();i++) {

					System.out.println(descriptors.get(i).getLocalName()+" "+sf.getAttribute(i));
				}

				//System.out.println("\n");
			}
			// check if the src attribute exists and srcCollection is not empty
			if (srcCollection == null || srcCollection.getSchema().getDescriptor(srcCodeField)==null) return null;
			// try to load trg collection 
			
			trgCollection = load(trgLayer);

			List<AttributeDescriptor> descriptors2 = trgCollection.getSchema().getAttributeDescriptors();
			SimpleFeatureIterator sfi2 =  trgCollection.features();
			while(sfi2.hasNext()) {
				SimpleFeature sf = sfi2.next();
				for (int i= 0; i<sf.getAttributeCount();i++) {

					System.out.println(descriptors2.get(i).getLocalName()+" "+sf.getAttribute(i));
				}

				//System.out.println("\n");
			}
			// check if the src attribute exists and trgCollection is not empty
			if (trgCollection==null || trgCollection.getSchema().getDescriptor(trgCodeField)==null) return null;

			// try to load mask collection
   	    	//maskCollection = load(maskLayer);			
		
		} catch(Throwable e) {
			System.out.println("Anything else");
			e.printStackTrace();
		}

    	
    	// check if intersection requires masking
    	if (isMasked) {
    		System.out.println("MASKED TRUE");
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
    	System.out.println("srcCodeField "+srcCodeField);
    	List<String> trgAttributes = new ArrayList<String>();

    	trgAttributes.add(trgCodeField);  
    	System.out.println("trgCodeField "+trgCodeField);
    	// perform the IntersectionFeatureCollection process
    	IntersectionFeatureCollection intersectionProcess = new IntersectionFeatureCollection();
    	SimpleFeatureCollection result = intersectionProcess.execute(srcCollection, trgCollection, srcAttributes, trgAttributes, mode, true, true);
		List<AttributeDescriptor> descriptors = result.getSchema().getAttributeDescriptors();
		try {
		SimpleFeatureIterator sfi = (SimpleFeatureIterator) result.features();
		while(sfi.hasNext()) {
			SimpleFeature sf = sfi.next();
			for (int i= 0; i<sf.getAttributeCount();i++) {

				System.out.println(descriptors.get(i).getLocalName()+" "+sf.getAttribute(i));
			}

			//System.out.println("\n");
		}
		} catch(Throwable e) {
			System.out.println("sono qui");
			e.printStackTrace();
		}
    	return result;
    }
    
    

    
    public boolean initConnections(Geoserver geoserver) {
    	try {
	        //check if this control works as expected
    		gsRestReader = new GeoServerRESTReader("http://"+geoserver.getGeoserverUrl()+"/geoserver", geoserver.getGeoserverUsername(), geoserver.getGeoserverPassword());
	        
	        if (!gsRestReader.existGeoserver()) return false;
	       //check if this control works as expected
	        dataStore = geoServerConnection(geoserver);
	        if (dataStore==null) return false;
    	} catch(Exception e) {
    		return false;
    	}
        return true;
    }
    
    /************
     * execute the intersections and update their status on the basis of the content od the intersections table
     * if status is DELETE then the intersection will be removed
     * if status is TOCOMPUTE then the intersection will be executed, stored and published and then the STATUS will be updated to COMPUTED
     * otherwise no changes are applied to the intersection
     * @param host where address the request of delete, update
     * @param intersections the intersections to parse
     * @return true everything goes well
     */
    public boolean executeIntersectionStatements(String host, Config config, boolean simulate){

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
        for (Intersection intersection:intersections){

        	Status status = intersection.getStatus();
        	LOGGER.trace("INTERSECTION "+intersection+"-->"+status);        	
        	long id = intersection.getId();
        	if (status==Status.TODELETE) {
        		Request.deleteIntersectionById(host, id);
        		// to delete the result of the intersection
        	}
        	if (status==Status.TOCOMPUTE) {
        		SimpleFeatureCollection resultInt = null;
        		resultInt = intersection(intersection);
        /*		SimpleFeatureIterator sfi = result.features();
        		System.out.println("prima");
        		while (sfi.hasNext()) {
        			System.out.println("object "+sfi.next());
        		}*/
        		// in case no problems happen during the intersection process call

        		if (resultInt!=null) {
	        		try {
		        	    intersection.setStatus(Status.COMPUTED);
		        		Request.updateIntersectionById(host, id, intersection);
	        			String dbHost = config.getGlobal().getDb().getHost();
	        			String schema = config.getGlobal().getDb().getSchema();
	        			String db = config.getGlobal().getDb().getDatabase();
	        			String user = config.getGlobal().getDb().getUser();
	        			String pwd = config.getGlobal().getDb().getPassword();
	        			int port = Integer.parseInt(config.getGlobal().getDb().getPort());
	        			
	        			String srcLayer = intersection.getSrcLayer();
	        			String trgLayer = intersection.getTrgLayer();
	        			String srcCode = intersection.getSrcCodeField();
	        			String trgCode = intersection.getTrgCodeField();
	        			LOGGER.trace("CREDENTIAL FOR "+schema+":"+db+" on "+ dbHost+":"+port+"("+user+","+pwd+")");
						OracleDataStoreManager dataStore = new OracleDataStoreManager(resultInt,srcLayer, trgLayer, srcCode, trgCode,
									dbHost,port,db,schema,user,pwd);
						
								//	"localhost",1521,"FIDEVQC","FIGIS_GIS","FIGIS_GIS","FIGIS");
					} catch (Exception e) {
							LOGGER.trace("Problems performing the intersection"+intersection);
					}

        		}
        	}
        	// otherwise delete the intersection instance 
        	else {
        		Request.deleteIntersectionById(host, id);
        			// to delete the result of the intersection
        	}
        	
        	// this check it is necessary only to show all the cases
        	// if (status==Status.COMPUTING || status==Status.COMPUTED || status==Status.NOVALUE) continue;
        }
        LOGGER.trace("Intersections updated");
        return true;
    	
    }
    
    
    public List<Intersection>getIntersection(String host) throws MalformedURLException{
    	 return Request.getAllIntersections(host);
    }
    
    
    /**
     * Removes TemplateModelEvents from the queue and put
     */
    public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {
    	
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
                    
                    // basic checks
                    LOGGER.trace("Reading config information");
                    Request.initConfig();
                    Config config = Request.existConfig(host);
                    //check if this control works as expected
                    if (config==null) {
                    	LOGGER.trace("Problems to find config information. Skip execution ...");
                    	continue;
                    }
                    LOGGER.trace("Config information correctly read. Trying to connect to Geoserver on "+config.getGlobal().getGeoserver().getGeoserverUrl());
                    // check the datastore and REST manager geoserver connections 
                    if (!initConnections(config.getGlobal().getGeoserver())) {
                    	LOGGER.trace("Problems to find Geoserver. Skip execution ...");
                    	continue;
                    }
                    LOGGER.trace("Geoserver and Datastore found");
                    // update the status of the intersections on the basis of the new input
                    boolean areIntersectionsUpdated = executeIntersectionStatements(host, config, false);
                    
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
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new ActionException(this, message);
            }
        }
        
        return ret;
    }
    
}
