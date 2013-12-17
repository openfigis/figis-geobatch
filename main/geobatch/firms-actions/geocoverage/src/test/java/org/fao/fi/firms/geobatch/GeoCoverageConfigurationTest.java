package org.fao.fi.firms.geobatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.geosolutions.fi.firms.geobatch.GeoCoverageConfiguration;
import it.geosolutions.geobatch.registry.AliasRegistry;
import it.geosolutions.geobatch.xstream.Alias;


import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

/**
 * 
 *@author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *        emmanuel.blondel@fao.org
 *
 */
public class GeoCoverageConfigurationTest {
	private AliasRegistry registry = new AliasRegistry();
	private GeoCoverageConfiguration configuration = null;
	private static final XStream xstream = new XStream();
	
	@Before
	public void setUp() {
		Alias alias=new Alias();
		alias.setAliasRegistry(registry);
		alias.setAliases(xstream);
		
		configuration = new GeoCoverageConfiguration("id", "name", "description");
		
		configuration.setGeoserverURL("http://localhost:8080/geoserver");
		configuration.setNamespace("geofirms");
		configuration.setRefAttribute("layer");
		
	}
	
	@Test
	public void testSerialize() {		
		assertNotNull(xstream.toXML(configuration));
	}
	
	@Test
	public void testDeserialize() {		
		Object cfg = xstream.fromXML(xstream.toXML(configuration));
		assertNotNull(cfg);
		assertTrue(cfg instanceof GeoCoverageConfiguration);
		
		GeoCoverageConfiguration config = (GeoCoverageConfiguration) cfg;
		
		assertEquals("http://localhost:8080/geoserver",config.getGeoserverURL());
		assertEquals("geofirms",config.getNamespace());
		assertEquals("layer",config.getRefAttribute());


	}
}
