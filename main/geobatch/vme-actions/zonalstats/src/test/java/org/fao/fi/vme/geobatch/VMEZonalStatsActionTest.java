package org.fao.fi.vme.geobatch;

import it.geosolutions.fi.vme.geobatch.VMEZonalStatsAction;
import it.geosolutions.fi.vme.geobatch.VMEZonalStatsConfiguration;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;


import java.io.File;
import java.io.FileInputStream;
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
public class VMEZonalStatsActionTest{

	static final Logger LOGGER = Logger.getLogger(VMEZonalStatsActionTest.class);
	static final String FLOW_XML = "zonalstats.xml";
	
	VMEZonalStatsConfiguration configuration;
	
	@Before
	public void setUp() throws Exception {
	XStream stream = new XStream();
	stream.alias("VMEZonalStatsConfiguration",
			VMEZonalStatsConfiguration.class);
	configuration = (VMEZonalStatsConfiguration) stream.fromXML(this
			.getResourceFile(FLOW_XML));
	
	}
	
	@Test
	public void testExecuteXml() throws Exception {

		configuration.setSourceFeature(FeatureConfiguration.fromXML( new FileInputStream(getResourceFile("vme.xml"))));
			
		Queue<EventObject> events = new LinkedList<EventObject>();
		events.add(new FileSystemEvent(getResourceFile("vme.xml"),
										FileSystemEventType.FILE_ADDED));

		VMEZonalStatsAction action = new VMEZonalStatsAction(configuration);
		action.setTempDir(new File("/tmp"));
		action.execute(events);

	}
	
	private File getResourceFile(String resource) throws URISyntaxException {
		return new File(this.getClass().getResource("/test-data/inputs/"+resource).toURI());
	}
	
}