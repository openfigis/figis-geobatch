package org.fao.fi.vme.geobatch;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.geosolutions.fi.vme.geobatch.VMEIngestionConfiguration;
import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
import it.geosolutions.geobatch.registry.AliasRegistry;
import it.geosolutions.geobatch.xstream.Alias;

import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *         emmanuel.blondel@fao.org
 * 
 */
public class VMEIngestionConfigurationTest {
	private AliasRegistry registry = new AliasRegistry();
	private VMEIngestionConfiguration configuration = null;
	private static final XStream xstream = new XStream();
	
	@Before
	public void setUp() {
		Alias alias=new Alias();
		alias.setAliasRegistry(registry);
		alias.setAliases(xstream);
		
		configuration = new VMEIngestionConfiguration("id", "name", "description");

		FeatureConfiguration featureConfig = new FeatureConfiguration();
		featureConfig.setTypeName("typeName1");
		featureConfig.setCrs("EPSG:4326");
		
		configuration.setSourceFeature(featureConfig);
		
	}
	
	@Test
	public void testSerialize() {		
		assertNotNull(xstream.toXML(configuration));
	}
	
	@Test
	public void testDeserialize() {		
		Object cfg = xstream.fromXML(xstream.toXML(configuration));
		assertNotNull(cfg);
		assertTrue(cfg instanceof VMEIngestionConfiguration);
		
		VMEIngestionConfiguration config = (VMEIngestionConfiguration) cfg;
		assertEquals("typeName1",config.getSourceFeature().getTypeName());
		assertEquals("EPSG:4326",config.getSourceFeature().getCrs());


	}
}
