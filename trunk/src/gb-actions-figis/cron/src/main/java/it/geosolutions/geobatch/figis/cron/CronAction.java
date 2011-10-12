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
import java.net.MalformedURLException;
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
import org.geotools.process.feature.gs.ClipProcess;
import org.geotools.process.feature.gs.IntersectionFeatureCollection;
import org.geotools.process.feature.gs.IntersectionFeatureCollection.IntersectionMode;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
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

    /**
     * configuration
     */
    private final CronConfiguration conf;
    private final String workspace="sf";
    public CronAction(CronConfiguration configuration) {
        super(configuration);
        conf = configuration;
        //TODO initialize your members here
    }
    /********
     * Connect to a geoserver instance
     * @param geoserver contains information for connection
     * @throws IOException in case the URL in the geoserver instance is not available
     */
    private SimpleFeatureCollection geoServerConnection(String srcLayer, Geoserver geoserver) throws IOException {
    	String getCapabilities = "http://"+geoserver.getGeoserverUrl()+"/geoserver/wfs?REQUEST=GetCapabilities";
    	System.out.println("ECCOMI qui"+getCapabilities);
    	Map connectionParameters = new HashMap();
    	connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities );
    	connectionParameters.put("WFSDataStoreFactory:USERNAME", geoserver.getGeoserverUsername() );
    	connectionParameters.put("WFSDataStoreFactory:PASSWORD", geoserver.getGeoserverPassword() );
  	
    	// Step 2 - connection
    	DataStore data = DataStoreFinder.getDataStore( connectionParameters );
    	SimpleFeatureSource sfs = data.getFeatureSource(srcLayer);
    	return sfs.getFeatures();
    }
    
    /************
     * check if the srcLayer exists in the geoserver connection
     * @param srcLayer the layer to return the simplefeaturecollection
     * @param data contains connection data
     * @return the SimpleFeatureCollection of srcLayer on the data server
     * @throws IOException 
     */
    private SimpleFeatureCollection checkAndLoad(String srcLayer, Geoserver geoserver) throws IOException{
    	System.out.println("URL "+geoserver.getGeoserverUrl());
    	System.out.println("Admin "+geoserver.getGeoserverUsername());
    	System.out.println("PWD "+geoserver.getGeoserverPassword());
    	GeoServerRESTReader gsRestReader = new GeoServerRESTReader("http://"+geoserver.getGeoserverUrl()+"/geoserver", geoserver.getGeoserverUsername(), geoserver.getGeoserverPassword());
    	System.out.println("An instance of geoserver is running"+ gsRestReader.existGeoserver());
    	int index = srcLayer.indexOf(":");
    	System.out.println("SRCLAYER "+srcLayer);
    	String dataStore = srcLayer.substring(0, index);
    	System.out.println("WORKSPACE "+dataStore);
    	String layer = srcLayer.substring(index+1, srcLayer.length());
    	System.out.println("DSNAME "+layer);    	
    	System.out.println("datastore "+gsRestReader.getDatastore(workspace, dataStore));
    	RESTDataStore restDataStore = gsRestReader.getDatastore(workspace, dataStore);
    	RESTLayer restLayer = gsRestReader.getLayer(layer);
    	System.out.println("Layer Name "+restLayer.getName());
    	RESTDataStoreList list = gsRestReader.getDatastores(workspace);
        for (NameLinkElem element : list) {
            String name = element.getName();
            System.out.println("-->NAME "+name);    	
        }    	
    	//RESTDataStore restDataStore = gsRestReader.getDatastore(workspace, dataStore);

    	
    	if (restDataStore!=null) {
    		SimpleFeatureCollection sfc = geoServerConnection(layer, geoserver);
    	}
    	return null;
    }

    /***********
     * perform the ie-intersection action
     * @param intersection the data on which to perform the intersection
     * @param geoserver the info where look for data
     * @return true in case no intersection is performed
     */
    public boolean intersection(Intersection intersection, Geoserver geoserver) {
    	
    	//initialize variables
    	String srcLayer = intersection.getSrcLayer();
    	String trgLayer = intersection.getTrgLayer();
    	String srcCodeField =  intersection.getSrcCodeField();
    	String trgCodeField = intersection.getTrgCodeField();
    	String maskLayer = intersection.getMaskLayer();
    	boolean isMask = intersection.isMask();
    	
    	IntersectionMode mode = IntersectionMode.INTERSECTION;
    	if (!intersection.isPreserveTrgGeom()) mode = IntersectionMode.SECOND;
    	
    	//load feature collections from geoserver
    	SimpleFeatureCollection srcCollection =null;
    	SimpleFeatureCollection trgCollection = null;
    	SimpleFeatureCollection maskCollection = null;
		try {
			srcCollection = checkAndLoad(srcLayer, geoserver);
			trgCollection = checkAndLoad(trgLayer, geoserver);
	   		maskCollection = checkAndLoad(maskLayer, geoserver);			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		System.out.println("Sono qui"+isMask);
    	
    	// check if intersection requires masking
    	if (isMask) {
    		ClipProcess clipProcess = new ClipProcess();
    		SimpleFeatureIterator sfi = maskCollection.features();
    		Geometry maskGeometry = null;
    		if (sfi.hasNext()) {
    			maskGeometry =(Geometry)sfi.next().getDefaultGeometry();
    		}
    		while (sfi.hasNext()) {
    			maskGeometry = maskGeometry.union((Geometry)sfi.next().getDefaultGeometry());
    		}
    		srcCollection = clipProcess.execute(srcCollection, maskGeometry);
    		trgCollection = clipProcess.execute(trgCollection, maskGeometry);
    	}
    	
    	// setup for the IntersectionFeatureCollectionProcess
    	List<String> srcAttributes = new ArrayList<String>();
    	srcAttributes.add(srcCodeField);
    	List<String> trgAttributes = new ArrayList<String>();
    	trgAttributes.add(trgCodeField);    	
    	IntersectionFeatureCollection intersectionProcess = new IntersectionFeatureCollection();
    	SimpleFeatureCollection result = intersectionProcess.execute(srcCollection, trgCollection, srcAttributes, trgAttributes, mode, false, false);
    	return true;
    }
    
    
    private boolean publish() {
    	return true;
    }
    
    /**
     * Removes TemplateModelEvents from the queue and put
     */
    public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {
    	final String host = "http://localhot:8080";
        // return
        final Queue<EventObject> ret=new LinkedList<EventObject>();
        
        while (events.size() > 0) {
            final EventObject ev;
            try {
                if ((ev = events.remove()) != null) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("ConfigAction.execute(): working on incoming event: "+ev.getSource());
                    }
                    FileSystemEvent fileEvent=(FileSystemEvent)ev;
                    // DO SOMETHING WITH THE INCOMING EVENT
                    //*****************************************************
                    List<Intersection> intersections = Request.getAllIntersections(host);
                    Config config = Request.existConfig(host);
                    for (Intersection intersection:intersections){
                    	Status status = intersection.getStatus();
                    	long id = intersection.getId();
                    	if (status==Status.TODELETE) {
                    		Request.deleteIntersectionById(host, id);
                    	}
                    	if (status==Status.TOCOMPUTE) {
                    		boolean isComputed = intersection(intersection, config.getGlobal().getGeoserver());
                    		if (isComputed) {
                    			intersection.setStatus(Status.COMPUTED);
                    			Request.updateIntersectionById(host, id, intersection);
                    		}
                    	}
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
                if (LOGGER.isErrorEnabled())
                    LOGGER.error(message);
                throw new ActionException(this, message);
            }
        }
        
        return ret;
    }
    
}
