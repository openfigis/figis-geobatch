package it.geosolutions.geobatch.figis.cron.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.EventObject;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

import it.geosolutions.figis.Request;
import it.geosolutions.figis.model.Geoserver;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;

import it.geosolutions.geobatch.figis.cron.CronAction;
import it.geosolutions.geobatch.figis.cron.CronConfiguration;
import it.geosolutions.geobatch.figis.cron.OracleDataStoreManager;

import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureTypes;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Geometry;

public class TestCron extends TestCase{

	private CronAction cronAction = null;
	private Geoserver geoserver = null;
	private final String host = "http://localhost:9999";
	
/*	@Test
	public void testOracle() {
		OracleDataStoreManager dataStore = new OracleDataStoreManager();
	}*/
	

	@Override
	protected void setUp() throws Exception {
		File inputFile =null;
		try {
			inputFile = File.createTempFile("clstats_in", ".xml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Queue<EventObject> queue = new LinkedBlockingQueue<EventObject>();
        queue.add(new FileSystemEvent(inputFile, FileSystemEventType.FILE_ADDED));
		CronConfiguration cronConfiguration = new CronConfiguration("id", "name", " description");
		cronAction = new CronAction(cronConfiguration);
		
		//IntersectionAction intersectionAction = new IntersectionAction("id", "name", " description");
		
		geoserver = new Geoserver();
		geoserver.setGeoserverUrl("localhost:8080");
		geoserver.setGeoserverUsername("admin");
		geoserver.setGeoserverPassword("geoserver");
		
		boolean value = cronAction.initConnections(geoserver);	
		Request.initIntersection();
	}
	
	/*
	
	public void testUpdate() throws MalformedURLException {
		//delete the old status of the DB
		Request.deleteAllIntersections(host);
		// Intersection table status before creation
	    Intersection intersection1 = new Intersection(false, true, false,"restricted1", "roads1", "cat",
	              "label", "sf:restricted", "areaCRS", Status.TOCOMPUTE);	
	    long result1 = Request.insertIntersection(host, intersection1);	    
	    Intersection intersection2 = new Intersection(false, true, false,"restricted2", "roads2", "cat",
	              "label", "sf:restricted", "areaCRS", Status.TOCOMPUTE);	
	    long result2 = Request.insertIntersection(host, intersection2);		    
	    Intersection intersection3 = new Intersection(false, true, false,"restricted3", "roads3", "cat",
	              "label", "sf:restricted", "areaCRS", Status.TODELETE);
	    long result3 = Request.insertIntersection(host, intersection3);	
	    Intersection intersection4 = new Intersection(false, true, false,"restricted4", "roads4", "cat",
	              "label", "sf:restricted", "areaCRS", Status.COMPUTING);
	    long result4 = Request.insertIntersection(host, intersection4);	
	    
	    // execute commands. the second parameter is set on true because we perform the executeIntersectionStatements in simulate modality
	    cronAction.executeIntersectionStatements(host, true);
	    
	    // take the new intersections status and check results
	    List<Intersection> intersections = cronAction.getIntersection(host);
	    assertTrue(intersections.size()==3);
	    
	    for (Intersection intersection: intersections) {
	    	if (intersection.getId()== result1) assertTrue(intersection.getStatus()==Status.COMPUTED);
	    	if (intersection.getId()== result2) assertTrue(intersection.getStatus()==Status.COMPUTED);
	    	if (intersection.getId()== result4) assertTrue(intersection.getStatus()==Status.COMPUTING);
	    }
	    
		
	}
*/
	@Test
	public void test() throws Exception {
		System.out.println("test");
		
	    Intersection intersection = new Intersection(false, false, false,"sf:restricted", "sf:restricted", "cat",
	              "cat", "sf:restricted", "areaCRS", Status.TOCOMPUTE);


		SimpleFeatureCollection result = cronAction.intersection(intersection);
		List<AttributeDescriptor> descriptors = result.getSchema().getAttributeDescriptors();
		SimpleFeatureIterator sfi = result.features();
		while(sfi.hasNext()) {
			SimpleFeature sf = sfi.next();
			for (int i= 0; i<sf.getAttributeCount();i++) {

				System.out.println(descriptors.get(i).getLocalName()+" "+sf.getAttribute(i));
			}

			//System.out.println("\n");
		}

		 

		System.out.println("ORACLE STA PARTENDO");
		SimpleFeatureType forced = FeatureTypes.transform(result.getSchema(), DefaultGeographicCRS.WGS84);
		OracleDataStoreManager dataStore = new OracleDataStoreManager(result, "sf:restricted", "sf:restricted", "cat",
	              "cat","localhost",1521,"FIDEVQC","FIGIS_GIS","FIGIS_GIS","FIGIS");
		
//		insertIntoList();
//		saveToTemp(fstColleciton);
//		saveToTemp(sndColleciton);
		
	//	System.out.println("RETURN VALUE"+dataStore.saveToTemp(dataStore.orclDataStore, sndCollection, "STATISTICAL_TMP_TABLE"));
		

/*		OracleDataStoreManager dataStore = new OracleDataStoreManager(fstCollection, sndCollection,"localhost",8080,"FIDEVQC","FIGIS_GIS","admin","geoserver");
		 sfi = sndCollection.features();
		while(sfi.hasNext()) {
			SimpleFeature sf = sfi.next();
			for (int i= 0; i<sf.getAttributeCount();i++) {

				System.out.println(sndCollection.getSchema().getAttributeDescriptors().get(i).getLocalName()+" "+sf.getAttribute(i));
			}

			System.out.println("\n");
		}
		System.out.println("END");*/

		
	}
	
/*	@Test
	public void testOracleConnection() {
		SimpleFeatureCollection fstCollection = null;
		SimpleFeatureCollection sndCollection = null;

		try {
			OracleDataStoreManager dataStore = new OracleDataStoreManager(fstCollection, sndCollection,"restricted", "roads", "cat",
		              "label","localhost",1521,"FIDEVQC","FIGIS_GIS","FIGIS_GIS","FIGIS");
			assertTrue(dataStore!=null);

		//	FeatureCollection<SimpleFeatureType, SimpleFeature> fc = dataStore.getFeatures(stringUrl, LayerIntersector.spatialName);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}*/

}
