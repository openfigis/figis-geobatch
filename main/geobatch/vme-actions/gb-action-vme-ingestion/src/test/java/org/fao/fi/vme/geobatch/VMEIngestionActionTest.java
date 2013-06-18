package org.fao.fi.vme.geobatch;


import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import java.io.File;
import java.net.URISyntaxException;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.fao.fi.vme.geobatch.VMEIngestionAction;
import org.fao.fi.vme.geobatch.VMEIngestionConfiguration;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *         emmanuel.blondel@fao.org
 * 
 */
public class VMEIngestionActionTest{

	static final Logger LOGGER = Logger.getLogger(VMEIngestionActionTest.class);
	static final String FLOW_XML = "ingestion.xml";
	
	VMEIngestionConfiguration configuration;
	
	@Before
	public void setUp() throws Exception {
	XStream stream = new XStream();
	stream.alias("VMEIngestionConfiguration",
			VMEIngestionConfiguration.class);
	configuration = (VMEIngestionConfiguration) stream.fromXML(this
			.getResourceFile(FLOW_XML));
	
	}
	
	@Test
	public void testExecuteZip() throws Exception {

		Queue<EventObject> events = new LinkedList<EventObject>();
		events.add(new FileSystemEvent(getResourceFile("vme-db.zip"),
										FileSystemEventType.FILE_ADDED));

		VMEIngestionAction action = new VMEIngestionAction(configuration);
		action.setTempDir(new File("/tmp"));
		action.execute(events);

	}
	
	private File getResourceFile(String resource) throws URISyntaxException {
		return new File(this.getClass().getResource("/test-data/inputs/"+resource).toURI());
	}
	
}