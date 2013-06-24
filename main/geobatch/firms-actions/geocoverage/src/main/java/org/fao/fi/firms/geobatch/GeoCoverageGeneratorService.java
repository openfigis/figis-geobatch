package org.fao.fi.firms.geobatch;

import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
 *         emmanuel.blondel@fao.org
 *
 */
public class GeoCoverageGeneratorService extends /**AutoregisteringService*/ BaseService implements ActionService<EventObject, GeoCoverageConfiguration>{

	private final static Logger LOGGER = LoggerFactory.getLogger(GeoCoverageGeneratorService.class);
	
	public GeoCoverageGeneratorService(String id){
		super(id);
	}

	public Action<EventObject> createAction(
			GeoCoverageConfiguration configuration) {
		try {
			return new GeoCoverageAction(configuration);
		} catch (Exception e) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error(
						"Error occurred creating scripting Action... "
								+ e.getLocalizedMessage(), e);
		}

		return null;
	}

	public boolean canCreateAction(GeoCoverageConfiguration configuration) {
		return true;
	}
	
}
