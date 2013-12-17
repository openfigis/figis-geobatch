package org.fao.fi.vme.geobatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.geosolutions.fi.vme.geobatch.VMEZonalStatsConfiguration;
import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
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
public class VMEZonalStatsConfigurationTest {
	private AliasRegistry registry = new AliasRegistry();
	private VMEZonalStatsConfiguration configuration = null;
	private static final XStream xstream = new XStream();
	
	@Before
	public void setUp() {
		Alias alias=new Alias();
		alias.setAliasRegistry(registry);
		alias.setAliases(xstream);
		
		configuration = new VMEZonalStatsConfiguration("id", "name", "description");

		FeatureConfiguration featureConfig = new FeatureConfiguration();
		featureConfig.setTypeName("typeName1");
		featureConfig.setCrs("EPSG:4326");
		
		configuration.setGeoserverURL("http://localhost:8080/geoserver");
		configuration.setWorkspace("workspace");
		configuration.setSourceFeature(featureConfig);
		configuration.setCoverage("coverageName");
		configuration.setGeoIdentifier("gid");
		
	}
	
	@Test
	public void testSerialize() {		
		assertNotNull(xstream.toXML(configuration));
	}
	
	@Test
	public void testDeserialize() {		
		Object cfg = xstream.fromXML(xstream.toXML(configuration));
		assertNotNull(cfg);
		assertTrue(cfg instanceof VMEZonalStatsConfiguration);
		
		VMEZonalStatsConfiguration config = (VMEZonalStatsConfiguration) cfg;

		assertEquals("http://localhost:8080/geoserver",config.getGeoserverURL());
        assertEquals("workspace",config.getWorkspace());
        assertEquals("typeName1",config.getSourceFeature().getTypeName());
		assertEquals("EPSG:4326",config.getSourceFeature().getCrs());
		assertEquals("gid",config.getGeoIdentifier());
		assertEquals("coverageName",config.getCoverage());

	}
}
