package it.geosolutions.geobatch.figis.cron.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import it.geosolutions.figis.model.Geoserver;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.figis.cron.CronAction;
import it.geosolutions.geobatch.figis.cron.CronConfiguration;

import org.junit.Test;

public class TestCron {

	@Test
	public void test() {
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
		CronAction cronAction = new CronAction(cronConfiguration);
		
	    Intersection intersection = new Intersection(false, false, false,"sf:restricted", "sf:roads", "cat",
	              "label", "sf:restricted", "areaCRS", Status.TOCOMPUTE);
		Geoserver geoserver = new Geoserver();
		geoserver.setGeoserverUrl("localhost:8080");
		geoserver.setGeoserverUsername("admin");
		geoserver.setGeoserverPassword("geoserver");
		cronAction.intersection(intersection, geoserver);

		
	}

}
