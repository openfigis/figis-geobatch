package org.fao.fi.firms.geobatch;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * 
 *@author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *        emmanuel.blondel@fao.org
 *
 */
public class GeoCoverageGeneratorServiceTest {

	private static GeoCoverageConfiguration CONFIGURATION = new GeoCoverageConfiguration("id", "name", "description");
	private GeoCoverageGeneratorService generatorService = new GeoCoverageGeneratorService("GeoCoverageGeneratorService");

	@Test
	public void testConfigurationIsGenerated() {		
		assertTrue(generatorService.canCreateAction(
				CONFIGURATION));
		assertNotNull(generatorService.createAction(CONFIGURATION));
		assertTrue(generatorService.createAction(CONFIGURATION) instanceof GeoCoverageAction);
	}
}
