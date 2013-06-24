package org.fao.fi.firms.geobatch;

import it.geosolutions.geobatch.registry.AliasRegistrar;
import it.geosolutions.geobatch.registry.AliasRegistry;

/**
 * 
 * @author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *         emmanuel.blondel@fao.org
 * 
 */
public class GeoCoverageAliasRegistrar extends AliasRegistrar {

	public GeoCoverageAliasRegistrar(AliasRegistry registry) {
		LOGGER.info(getClass().getSimpleName() + ": registering alias.");
		
		registry.putAlias("GeoCoverageConfiguration", GeoCoverageConfiguration.class);
		
	}
	
}
