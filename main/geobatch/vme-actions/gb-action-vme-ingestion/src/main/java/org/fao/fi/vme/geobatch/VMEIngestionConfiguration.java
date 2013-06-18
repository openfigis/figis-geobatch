package org.fao.fi.vme.geobatch;

import it.geosolutions.geobatch.actions.ds2ds.Ds2dsConfiguration;

/**
 * 
 *@author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *        emmanuel.blondel@fao.org
 *
 */
public class VMEIngestionConfiguration extends Ds2dsConfiguration{

	/**
	 * @param id
	 * @param name
	 * @param description
	 */
	public VMEIngestionConfiguration(String id, String name,
			String description) {
		super(id, name, description);
	}
	
	
	@Override
    public VMEIngestionConfiguration clone() { 
        final VMEIngestionConfiguration configuration = (VMEIngestionConfiguration) super
                .clone();

        return configuration;
    }
	
	

}
