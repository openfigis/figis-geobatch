package org.fao.fi.firms.geobatch;

import it.geosolutions.fi.firms.geobatch.GeoCoverageAction;
import it.geosolutions.fi.firms.geobatch.GeoCoverageConfiguration;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import java.io.File;
import java.net.URISyntaxException;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

/**
 * 
 *@author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *        emmanuel.blondel@fao.org
 *
 */
public class GeoCoverageActionTest{

	static final Logger LOGGER = Logger.getLogger(GeoCoverageActionTest.class);
	static final String FLOW_XML = "geocoverage.xml";
	
	GeoCoverageConfiguration configuration;
	
	@Before
	public void setUp() throws Exception{
	XStream stream = new XStream();
	stream.alias("GeoCoverageConfiguration",
			GeoCoverageConfiguration.class);
	configuration = (GeoCoverageConfiguration) stream.fromXML(this
			.getResourceFile(FLOW_XML));
	
	}
	
	@Test
	public void testExecuteCsv() throws Exception {
	
		Queue<EventObject> events = new LinkedList<EventObject>();
		events.add(new FileSystemEvent(getResourceFile("gb_firms_coverage.csv"),
										FileSystemEventType.FILE_ADDED));

		GeoCoverageAction action = new GeoCoverageAction(configuration);
		action.setTempDir(new File("/tmp"));
		action.execute(events);

	}
	
	private File getResourceFile(String resource) throws URISyntaxException {
		return new File(this.getClass().getResource("/test-data/inputs/"+resource).toURI());
	}
	
}