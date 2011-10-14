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

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;

public class TestCron extends TestCase{

	private CronAction cronAction = null;
	private Geoserver geoserver = null;
	private final String host = "http://localhost:8080";
	
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
		
		geoserver = new Geoserver();
		geoserver.setGeoserverUrl("localhost:8080");
		geoserver.setGeoserverUsername("admin");
		geoserver.setGeoserverPassword("geoserver");
		
		boolean value = cronAction.initConnections(geoserver);	
		Request.initIntersection();
	}
	
	
	
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
/*
	@Test
	public void test() {

		
	    Intersection intersection = new Intersection(false, true, false,"restricted", "roads", "cat",
	              "label", "sf:restricted", "areaCRS", Status.TOCOMPUTE);


		SimpleFeatureCollection result = cronAction.intersection(intersection);

		List<AttributeDescriptor> descriptors = result.getSchema().getAttributeDescriptors();
		SimpleFeatureCollection fstCollection = null;
		SimpleFeatureCollection sndCollection = null;
		boolean split = cronAction.split(result, fstCollection, sndCollection);
		
		SimpleFeatureIterator sfi = fstCollection.features();
		while(sfi.hasNext()) {
			SimpleFeature sf = sfi.next();
			for (int i= 0; i<sf.getAttributeCount();i++) {

				System.out.println(fstCollection.getSchema().getAttributeDescriptors().get(i).getLocalName()+" "+sf.getAttribute(i));
			}

			System.out.println("\n");
		}
		System.out.println("END");

		
	}*/

}
